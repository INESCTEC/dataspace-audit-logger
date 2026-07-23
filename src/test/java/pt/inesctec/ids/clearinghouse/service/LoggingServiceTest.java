/* Copyright (C) [2024-2026], INESC TEC
 *
 * dataspace-audit-logger
 * Implements an audit logger and Clearing House in accordance with thethe International Data Space Reference Architecture.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR 
 * CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations 
 * under the License.
 *
 * You can reach INESC TEC Technology Technology Transfer Office (TTO) at tech-transfer@inesctec.pt, or
 * Campus da Faculdade de Engenharia da Universidade do Porto
 * Rua Dr. Roberto Frias
 * 4200-465 Porto
 * Portugal 
 */
package pt.inesctec.ids.clearinghouse.service;

import de.fraunhofer.iais.eis.*;
import ids.messaging.core.daps.DapsTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;

import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;



@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class LoggingServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingServiceTest.class);

    @Autowired private LoggingService loggingService;
    @Autowired private MongoService mongoService;
    @Autowired private DapsTokenProvider provider;

    private TestUtil testUtil;
    private ClearingHouseMessage logMessage;
    private Message headerFromLogMessage;
    private Message headerFromProcessMessage;
    private Processes processMessage;

    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();

        headerFromLogMessage=testUtil.logMessage();
        headerFromProcessMessage=testUtil.requestMessage();
        // Populate Database
        logMessage=new ClearingHouseMessageBuilder()
                .header(headerFromLogMessage.toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("015fd3fb-36fb-4bfd-8cac-2f49aeafb328")
                .build();
        processMessage=new ProcessesBuilder()
                .header(headerFromProcessMessage.toRdf())
                .owners(testUtil.owners_List())
                .agreementID("278655-36fb-4bfd-8cac-75478")
                .build();
        mongoService.insert(processMessage, PROCESS);
        mongoService.insert(logMessage, MESSAGES);
    }

    @AfterEach
    void tearDown() {
        mongoService.removeAll();
    }

    @Test
    void determineCollectionName(){

        // Test an ArtefacRequestMessage
        assertThat(loggingService.determineCollectionName(testUtil.artefactRequestMessage().toRdf()))
                .isEqualTo(MESSAGES);

        System.out.println("Fist assert passed");
        // Test an ArtefacResponseMessage
        assertThat(loggingService.determineCollectionName(testUtil.artefactResponseMessage().toRdf()))
                .isEqualTo(MESSAGES);
        System.out.println("Second assert passed");

        // Test a Contract Agreement
        assertThat(loggingService.determineCollectionName(testUtil.contractAgreement().toRdf()))
                .isEqualTo(CONTRACTS);
        System.out.println("3 assert passed");

        // Test a Process
        assertThat(loggingService.determineCollectionName(testUtil.owners()))
                .isEqualTo(PROCESS);
        System.out.println("4 assert passed");

        // Test a Data Usage Notification
        assertThat(loggingService.determineCollectionName(testUtil.getUsageLogging()))
                .isEqualTo(LOGGING);
        System.out.println("5 assert passed");

        // Test an Invalid Payload
        assertThrows(InvalidValueException.class, () -> loggingService.determineCollectionName(testUtil.payload()));
    }

    @Test
    void handleMessage_Process(){
        Connector connector=testUtil.createConnectorToTest();

        // Test with a PROCESS message
        Message header=testUtil.requestMessage();
        String payload=testUtil.owners();
        UUID agreementID=UUID.fromString("817b6e21-8e54-4f4d-ac28-4b1de6e38313");

        // Create the expected ResponseMessage
        ResponseMessage expectedResponseMessage=
                new ResponseMessage(MessageUtil.buildReturnHeader(header, connector, provider), agreementID.toString());

        ResponseMessage responseMessage=loggingService.handleMessage(header, payload, agreementID, connector, provider);
        // Check if the Response Message is the expected one
        assertThat(responseMessage.getHeader()).isInstanceOf(MessageProcessedNotificationMessage.class);
        assertThat(responseMessage.getPayload()).isEqualTo(expectedResponseMessage.getPayload());
        assertThat(responseMessage.getCharset()).isEqualTo(expectedResponseMessage.getCharset());

        // Check if log/process was logged
        List<DBAbstract> messagesOnDB_byAgreement= mongoService.findAllMessagesByAgreement(agreementID.toString());
        DBAbstract expectedMessage= new ProcessesBuilder()
                .header(header.toRdf())
                .owners(testUtil.owners_List())
                .agreementID(agreementID.toString())
                .build();
        System.out.println(messagesOnDB_byAgreement.size());
        assertThat(messagesOnDB_byAgreement.size()).isEqualTo(1);


        assertThat(messagesOnDB_byAgreement.get(0))
                .isInstanceOf(Processes.class);

        assertThat(messagesOnDB_byAgreement.get(0).get_id())
                .isNotNull(); // As the ID is inserted automatically when inserting on database, the expectedMessage does not have an ID

        assertThat(messagesOnDB_byAgreement.get(0).getAgreementID())
                .isEqualTo(expectedMessage.getAgreementID());

        assertThat(messagesOnDB_byAgreement.get(0).getHeader()).
                isEqualTo(expectedMessage.getHeader());

        assertThat(messagesOnDB_byAgreement.get(0).getTimestamp())
                .isNotNull(); // As the timestamp is creaed automatically when instantiating an DBObject, the expectedMessage does not have an exact replica

        assertThat(messagesOnDB_byAgreement.get(0).getHash())
                .isNotNull(); // As the hash is added automatically when inserting on database, the expectedMessage does not have a hash

        assertThat(((Processes)messagesOnDB_byAgreement.get(0)).getOwners())
                .isEqualTo(((Processes)expectedMessage).getOwners());


    }

    @Test
    void handleMessage_Message(){
        Connector connector=testUtil.createConnectorToTest();

        // Test with a Artefact Response message
        Message header=testUtil.logMessage();
        String payload=testUtil.artefactResponseMessage().toString();
        UUID agreementID=UUID.fromString("817b6e21-8e54-4f4d-ac28-4b1de6e38313");


        // Create the expected ResponseMessage
        ResponseMessage expectedResponseMessage=
                new ResponseMessage(MessageUtil.buildReturnHeader(header, connector, provider), "expectedSignedToken");

        // Run test
        ResponseMessage responseMessage=loggingService.handleMessage(header, payload, agreementID, connector, provider);

        // Check test output
        assertThat(responseMessage.getHeader()).isInstanceOf(MessageProcessedNotificationMessage.class);
        assertThat(responseMessage.getPayload()).isNotNull(); // The token will never be equal
        assertThat(responseMessage.getCharset()).isEqualTo(expectedResponseMessage.getCharset());

        // Check if log/process was logged
        List<DBAbstract> messagesOnDB_byAgreement= mongoService.findAllMessagesByAgreement(agreementID.toString());
        messagesOnDB_byAgreement.forEach(System.err::println);
        DBAbstract expectedMessage= new ClearingHouseMessageBuilder()
                .header(header.toRdf())
                .payload(payload)
                .agreementID(agreementID.toString())
                .build();
        assertThat(messagesOnDB_byAgreement.size()).isEqualTo(1);

        assertThat(messagesOnDB_byAgreement.get(0))
                .isInstanceOf(ClearingHouseMessage.class);

        assertThat(messagesOnDB_byAgreement.get(0).get_id())
                .isNotNull(); // As the ID is inserted automatically when inserting on database, the expectedMessage does not have an ID

        assertThat(messagesOnDB_byAgreement.get(0).getAgreementID())
                .isEqualTo(expectedMessage.getAgreementID());

        assertThat(messagesOnDB_byAgreement.get(0).getHeader()).
                isEqualTo(expectedMessage.getHeader());

        assertThat(messagesOnDB_byAgreement.get(0).getTimestamp())
                .isNotNull(); // As the timestamp is creaed automatically when instantiating an DBObject, the expectedMessage does not have an exact replica

        assertThat(messagesOnDB_byAgreement.get(0).getHash())
                .isNotNull(); // As the hash is added automatically when inserting on database, the expectedMessage does not have a hash

        assertThat(((ClearingHouseMessage)messagesOnDB_byAgreement.get(0)).getPayload())
                .isEqualTo(((ClearingHouseMessage)expectedMessage).getPayload());

    }

    @Test
    void signLog(){
        // Initialize RequestClearingHouseMessage
        ClearingHouseMessage messageToSign=new ClearingHouseMessageBuilder()
                .payload(testUtil.artefactRequestMessage().toRdf())
                .header(testUtil.logMessage().toRdf())
                .agreementID("1235-7896-294")
                .build();
        messageToSign.setTimestamp(testUtil.getDate());
        messageToSign.set_id("123456");
        System.out.println(messageToSign.toJson());

        String logPayload=loggingService.signLog(messageToSign, testUtil.requestMessage());
        System.err.println(logPayload);
        assertThat(logPayload).isNotNull();
        assertThat(logPayload).startsWith("{\"data\":\"");
    }







}