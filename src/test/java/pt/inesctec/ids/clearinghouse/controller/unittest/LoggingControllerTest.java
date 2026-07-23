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
package pt.inesctec.ids.clearinghouse.controller.unittest;

import de.fraunhofer.iais.eis.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.TestPropertySource;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.controller.LoggingController;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.service.MongoService;
import pt.inesctec.ids.clearinghouse.util.DapsUtil;
import pt.inesctec.ids.clearinghouse.util.LogUtil;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class LoggingControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingController.class);
    @Autowired
    LoggingController loggingController;
    @Autowired
    MongoService mongoService;
    TestUtil testUtil;

    MockHttpServletRequest request;
    MockPart headerPart, payloadPart;
    @Mock Map<String, String> headersMap;

    @BeforeEach
    void setUp() {
        request= new MockHttpServletRequest();
        testUtil=new TestUtil();
        mongoService.removeAll();
    }

    @AfterEach
    void tearDown() {
        mongoService.removeAll();
    }

    @Test
    void addLog_ProcessDoesNotExist() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";
        // Build parameters
        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.artefactRequestMessage().toRdf();
        request.setParameter(HEADER, headerToSend.toRdf());
        request.setParameter(PAYLOAD, payloadToSend);

        // Checks how many documents exist for that agreement, before test
        assertThat(listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(0);

        // Send message
        LOGGER.info("Calling addLog() method ...");
        ResponseEntity<?> responseEntity= loggingController.addLog(UUID.fromString(agreementID), request, headersMap, headerToSend.toRdf(), payloadToSend);
        LOGGER.info("Calling addLog() method ... OK");

        System.out.println(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        // Checks how many documents were inserted
        assertThat(listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(2);

        // Checks the content of the automatically created PROCESS
        List<DBAbstract> processMessagesFromAgreement=mongoService.findAllMessagesByAgreement(agreementID, PROCESS);
        System.out.println(processMessagesFromAgreement);
        assertThat(processMessagesFromAgreement.size()).isEqualTo(1);
        assertThat(processMessagesFromAgreement.get(0).getAgreementID()).isEqualTo(agreementID);
        assertThat(((Processes)processMessagesFromAgreement.get(0)).getOwners()).isEqualTo(List.of(DapsUtil.getClientFromDAT(headerToSend.getSecurityToken())));
        assertThat(processMessagesFromAgreement.get(0).getTimestamp()).isNotNull();
        assertThat(processMessagesFromAgreement.get(0).get_id()).isNotNull();
        assertThat(processMessagesFromAgreement.get(0).getHeader()).isNull();


    }

    @Test
    void addLog_Message() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";

        LOGGER.info("BEFORE TEST: Add Process ...");

        // Prepare test -> Adds the Process for that Agreement
        DBAbstract process=new Processes(agreementID, testUtil.requestMessage().toRdf(), testUtil.owners_List());

        mongoService.insert(process, PROCESS);
        LOGGER.info("BEFORE TEST: Add Process ... OK");

        // Build parameters
        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.artefactRequestMessage().toRdf();
        request.setParameter(HEADER, headerToSend.toRdf());
        request.setParameter(PAYLOAD, payloadToSend);

        // Checks how many documents exist for that agreement, before test. It should be 1
        assertThat(this.listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(1);

        LOGGER.info("Calling addLog() method ...");
        // Send message
        ResponseEntity<?> responseEntity= loggingController.addLog(UUID.fromString(agreementID), request, headersMap, headerToSend.toRdf(), payloadToSend);
        LOGGER.info("Calling addLog() method ... OK");

        System.out.println(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        // Checks how many documents exist now
        assertThat(listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(2);

        // Checks the content of the inserted Log
        List<DBAbstract> artefactMessagesFromAgreement=mongoService.findAllMessagesByAgreement(agreementID, MESSAGES);
        System.out.println(artefactMessagesFromAgreement);
        assertThat(artefactMessagesFromAgreement.size()).isEqualTo(1);
        assertThat(artefactMessagesFromAgreement.get(0).getAgreementID()).isEqualTo(agreementID);
        assertThat(((ClearingHouseMessage)artefactMessagesFromAgreement.get(0)).getPayload()).isEqualTo(payloadToSend);
        assertThat(((ClearingHouseMessage)artefactMessagesFromAgreement.get(0)).getHash()).isNotNull();
        assertThat(artefactMessagesFromAgreement.get(0).getTimestamp()).isNotNull();
        assertThat(artefactMessagesFromAgreement.get(0).get_id()).isNotNull();
        assertThat(artefactMessagesFromAgreement.get(0).getHeader()).isEqualTo(headerToSend.toRdf());
    }

    @Test
    void addLog_Contract() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";

        // Prepare test -> Adds the Process for that Agreement
        DBAbstract process=new Processes(agreementID, testUtil.requestMessage().toRdf(), testUtil.owners_List());
        mongoService.insert(process, PROCESS);

        // Build parameters
        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.contractAgreement().toRdf();
        request.setParameter(HEADER, headerToSend.toRdf());
        request.setParameter(PAYLOAD, payloadToSend);

        // Checks how many documents exist for that agreement, before test. It should be 1
        assertThat(this.listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(1);

        LOGGER.info("Calling addLog() method ...");
        // Send message
        ResponseEntity<?> responseEntity= loggingController.addLog(UUID.fromString(agreementID), request, headersMap, headerToSend.toRdf(), payloadToSend);
        LOGGER.info("Calling addLog() method ... OK");

        System.out.println(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        // Checks how many documents exist now
        assertThat(listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(2);

        // Checks the content of the inserted Log
        List<DBAbstract> contractMessagesFromAgreement=mongoService.findAllMessagesByAgreement(agreementID, CONTRACTS);
        System.out.println(contractMessagesFromAgreement);
        assertThat(contractMessagesFromAgreement.size()).isEqualTo(1);
        assertThat(contractMessagesFromAgreement.get(0).getAgreementID()).isEqualTo(agreementID);
        assertThat(((ClearingHouseMessage)contractMessagesFromAgreement.get(0)).getPayload()).isEqualTo(payloadToSend);
        assertThat(((ClearingHouseMessage)contractMessagesFromAgreement.get(0)).getHash()).isNotNull();
        assertThat(contractMessagesFromAgreement.get(0).getTimestamp()).isNotNull();
        assertThat(contractMessagesFromAgreement.get(0).get_id()).isNotNull();
        assertThat(contractMessagesFromAgreement.get(0).getHeader()).isEqualTo(headerToSend.toRdf());
    }

    @Test
    void addProcess() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";
        // Build parameters
        Message headerToSend=testUtil.requestMessage();
        String payloadToSend=testUtil.owners();
        /*request.setParameter(HEADER, headerToSend.toRdf());
        request.setParameter(PAYLOAD, payloadToSend);*/

        // Initialize Parts
        headerPart=new MockPart(HEADER, headerToSend.toRdf().getBytes());
        payloadPart=new MockPart(PAYLOAD, payloadToSend.getBytes());
        // Add parts
        request.addPart(headerPart);
        request.addPart(payloadPart);

        // Checks how many documents exist for that agreement, before test. It should be 0
        assertThat(this.listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(0);

        LOGGER.info("Calling addProcess() method ...");
        // Send message
        ResponseEntity<?> responseEntity= loggingController.addProcess(UUID.fromString(agreementID), request, headersMap);
        LOGGER.info("Calling addProcess() method ... OK");

        System.out.println(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());


        // Checks how many documents exist now
        List<DBAbstract> processMessagesFromAgreement=this.listAllMessagesFromAgreementID(agreementID);
        assertThat(processMessagesFromAgreement.size()).isEqualTo(1);

        // Checks the content of the inserted Log
        assertThat(processMessagesFromAgreement.get(0).getAgreementID()).isEqualTo(agreementID);
        assertThat(((Processes)processMessagesFromAgreement.get(0)).getOwners()).isEqualTo(testUtil.owners_List());
        assertThat(processMessagesFromAgreement.get(0).getTimestamp()).isNotNull();
        assertThat(processMessagesFromAgreement.get(0).get_id()).isNotNull();
        assertThat(processMessagesFromAgreement.get(0).getHeader()).isEqualTo(headerToSend.toRdf());
    }

    @Test
    void addProcess_NullPayload() {
        String agreementID="895fd3fb-36fb-4bfd-8cac-2f49aeafb327";
        // Build parameters
        Message headerToSend=testUtil.requestMessage();
        /*request.setParameter(HEADER, headerToSend.toRdf());
        request.setParameter(PAYLOAD, payloadToSend);*/

        // Initialize Parts
        headerPart=new MockPart(HEADER, headerToSend.toRdf().getBytes());
        payloadPart=new MockPart(PAYLOAD, null);
        // Add parts
        request.addPart(headerPart);
        request.addPart(payloadPart);

        // Checks how many documents exist for that agreement, before test. It should be 0
        assertThat(this.listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(0);

        LOGGER.info("Calling addProcess() method ...");
        // Send message
        Exception exception = assertThrows(IllegalArgumentException.class, () -> loggingController.addProcess(UUID.fromString(agreementID), request, headersMap));
        assertThat(exception.getMessage()).isEqualTo("Value cannot be null.");
        LOGGER.info("Calling addProcess() method ... OK");

    }

    @Test
    void addProcess_WrongMessageType() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";
        // Build parameters -> The payload is wrong. It should throw an error
        Message headerToSend=testUtil.requestMessage();
        String payloadToSend=testUtil.payload();

        // Initialize Parts
        headerPart=new MockPart(HEADER, headerToSend.toRdf().getBytes());
        payloadPart=new MockPart(PAYLOAD, payloadToSend.getBytes());
        // Add parts
        request.addPart(headerPart);
        request.addPart(payloadPart);


        LOGGER.info("Calling addProcess() method ...");
        // Send message -> It should fail
        Exception ex=assertThrows(InvalidValueException.class, () -> loggingController.addProcess(UUID.fromString(agreementID), request, headersMap));
        LOGGER.info("Calling addProcess() method ... NOK");

        System.out.println(ex);
        assertThat(ex.getMessage()).isEqualTo("Invalid Message. The Clearing House cannot save the sent message type.");
    }

    @Test
    void addLog_WrongMessageType() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";
        // Build parameters -> The payload is wrong. It should throw an error
        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.payload();

        // Initialize Parts
        headerPart=new MockPart(HEADER, headerToSend.toRdf().getBytes());
        payloadPart=new MockPart(PAYLOAD, payloadToSend.getBytes());
        // Add parts
        request.addPart(headerPart);
        request.addPart(payloadPart);

        LOGGER.info("Calling addProcess() method ...");
        // Send message -> It should fail
        Exception ex=assertThrows(InvalidValueException.class, () -> loggingController.addLog(UUID.fromString(agreementID), request, headersMap, headerToSend.toRdf(), payloadToSend));
        LOGGER.info("Calling addProcess() method ... NOK");

        System.out.println(ex);
        assertThat(ex.getMessage()).isEqualTo("Invalid Message. The Clearing House cannot save the sent message type.");
    }

    @Test
    void addUsageLogging() {
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";

        // Prepare test -> Adds the Process and a COntract Agreement for that Agreement
        DBAbstract process=new Processes(agreementID, testUtil.requestMessage().toRdf(), testUtil.owners_List());
        mongoService.insert(process, PROCESS);

        DBAbstract contractAgreement=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.contractAgreement().toRdf())
                .agreementID(agreementID)
                .build();
        mongoService.insert(contractAgreement, CONTRACTS);


        // Build parameters
        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.getUsageLogging();
        request.setParameter(HEADER, headerToSend.toRdf());
        request.setParameter(PAYLOAD, payloadToSend);

        // Checks how many documents exist for that agreement, before test. It should be 1
        assertThat(this.listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(2);

        LOGGER.info("Calling addLog() method ...");
        // Send message
        ResponseEntity<?> responseEntity= loggingController.addLog(UUID.fromString(agreementID), request, headersMap, headerToSend.toRdf(), payloadToSend);
        LOGGER.info("Calling addLog() method ... OK");

        System.out.println(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        // Checks how many documents exist now
        assertThat(listAllMessagesFromAgreementID(agreementID).size()).isEqualTo(3);

        // Checks the content of the inserted Log
        List<DBAbstract> loggingMessagesFromAgreement=mongoService.findAllMessagesByAgreement(agreementID, LOGGING);
        System.out.println(loggingMessagesFromAgreement);
        assertThat(loggingMessagesFromAgreement.size()).isEqualTo(1);
        assertThat(loggingMessagesFromAgreement.get(0).getAgreementID()).isEqualTo(agreementID);
        assertThat(((ClearingHouseMessage)loggingMessagesFromAgreement.get(0)).getPayload()).isEqualTo(payloadToSend);
        assertThat(((ClearingHouseMessage)loggingMessagesFromAgreement.get(0)).getHash()).isNotNull();
        assertThat(loggingMessagesFromAgreement.get(0).getTimestamp()).isNotNull();
        assertThat(loggingMessagesFromAgreement.get(0).get_id()).isNotNull();
        assertThat(loggingMessagesFromAgreement.get(0).getHeader()).isEqualTo(headerToSend.toRdf());

    }

    private List<DBAbstract> listAllMessagesFromAgreementID(String agreement){
        List<DBAbstract> messagesFromAgreement=mongoService.findAllMessagesByAgreement(agreement);
        System.out.println(messagesFromAgreement);
        return messagesFromAgreement;
    }
}