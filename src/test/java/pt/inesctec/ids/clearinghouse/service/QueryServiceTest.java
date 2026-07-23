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

import de.fraunhofer.iais.eis.Message;
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
import pt.inesctec.ids.clearinghouse.dto.QueryDto;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.util.DapsUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.MESSAGES;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.PROCESS;


@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class QueryServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryServiceTest.class);

    @Autowired private QueryService queryService;
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
        /*mongoService.remove(processMessage, PROCESS);
        mongoService.remove(logMessage, MESSAGES);*/
    }

    @Test
    void buildQueryPayload() {
        Processes processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("12345")
                .build();
        ClearingHouseMessage logMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("12345")
                .build();
        List<ClearingHouseMessage> messagesFromAgreement =new ArrayList<>();
        // The process message is not suppose to show when quering the database
        //messagesFromAgreement.add(processMessage);
        messagesFromAgreement.add(logMessage);

        QueryDto query= queryService.buildQueryPayload(messagesFromAgreement, testUtil.queryMessage(), testUtil.createConnectorToTest(), DapsUtil.getDat(provider));
        System.out.println(query);
        // The returned documents do not have the Proces document
        assertThat(query.getDocuments().size()).isEqualTo(1);
        assertThat(query.getOrder()).isEqualTo("desc");
        assertThat(query.getCharset()).isEqualTo(StandardCharsets.UTF_8);
    }

    @Test
    void checkPermissions() {

        boolean hasAccess;
        String agreementID=processMessage.getAgreementID();

        // Checks if an UNAUTHORIZE user can access
        hasAccess= queryService.checkPermissions(DapsUtil.getDat(provider), agreementID);
        assertFalse(hasAccess);

        // Checks if an AUTHORIZE user can access
        hasAccess= queryService.checkPermissions(headerFromProcessMessage.getSecurityToken(), agreementID);
        assertTrue(hasAccess);
    }

    @Test
    void findAllByAgreement() {
        Processes processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("12345")
                .build();
        ClearingHouseMessage logMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("0654525")
                .build();
        ClearingHouseMessage logMessage2=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("0654525")
                .build();
        List<DBAbstract> messagesFromAgreement =new ArrayList<>();
        messagesFromAgreement.add(processMessage);
        messagesFromAgreement.add(logMessage);


        // Lists all messages from an agreement that does not exist
        // BD is empty
        LOGGER.info("Get all messages from an unknown agreement ... ");
        List<ClearingHouseMessage> messagesOnDBList= queryService.findAllByAgreement("12345");
        assertThat(messagesOnDBList.size()).isEqualTo(0);
        LOGGER.info("Get all messages from an unknown agreement ... OK");


        // Lists all messages from an agreement that only has 1 value
        LOGGER.info("Get all messages from an agreement with only 1 process ... ");
        // Populate Database
        mongoService.insert(processMessage, PROCESS);
        messagesOnDBList= queryService.findAllByAgreement(processMessage.getAgreementID());
        assertThat(messagesOnDBList.size()).isEqualTo(0); // It is 0 because there is only 1 message and it is a Process, which do not appear on the Query endpoint
        LOGGER.info("Get all messages from an agreement with only 1 process ... OK");


        LOGGER.info("Get all messages from an agreement with only 1 process and multiple agreemnets on DB ... ");
        // Insert data from multiple agreements
        mongoService.insert(logMessage, MESSAGES);
        mongoService.insert(logMessage2, MESSAGES);
        mongoService.insert(logMessage, PROCESS);
        messagesOnDBList= queryService.findAllByAgreement(processMessage.getAgreementID());
        assertThat(messagesOnDBList.size()).isEqualTo(0); // It is 0 because there is the agreement only has 1 message and it is a Process, which do not appear on the Query endpoint
        LOGGER.info("Get all messages from an agreement with only 1 process and multiple agreemnets on DB ... OK");


    }

}