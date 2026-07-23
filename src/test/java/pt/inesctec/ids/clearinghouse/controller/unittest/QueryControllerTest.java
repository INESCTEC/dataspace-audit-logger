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
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.ResultMessage;
import org.apache.commons.io.IOUtils;
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
import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.controller.LoggingController;
import pt.inesctec.ids.clearinghouse.controller.QueryController;
import pt.inesctec.ids.clearinghouse.converter.ClearingHouseMessageConverter;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.dto.QueryDto;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.service.MongoService;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;


import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class QueryControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingController.class);
    @Autowired
    QueryController controller;

    @Autowired
    MongoService mongoService;

    MockHttpServletRequest request;
    MockPart headerPart, payloadPart;
    @Mock
    Map<String, String> headersMap;

    TestUtil testUtil;
    ClearingHouseMessageConverter converter=new ClearingHouseMessageConverter();
    JsonConverter jsonConverter=new JsonConverter();

    @BeforeEach
    void setUp() {
        request= new MockHttpServletRequest();
        testUtil=new TestUtil();

        populateDatabase();
    }

    @AfterEach
    void tearDown() {
        mongoService.removeAll();
    }

    @Test
    void queryProcess_UnauthorizedUser() throws Exception {
        // It will fail because this logMessage does not have a Process
        String agreementID="015fd3fb-36fb-4bfd-8cac-2f49aeafb328";
        String query="";
        QueryMessage queryMessage=testUtil.queryMessage();

        // Build parameters
        request.setParameter(HEADER, queryMessage.toRdf());
        request.setParameter(PAYLOAD, query);

        // Checks how many documents exist for that agreement, before test
        assertThat(mongoService.findAllMessagesByAgreement(agreementID).size()).isEqualTo(1);

        // Send message
        LOGGER.info("Calling queryProcess() method ...");
        ResponseEntity<?> responseEntity= controller.queryProcess(UUID.fromString(agreementID), request, headersMap, queryMessage.toRdf(), query);
        LOGGER.info("Calling queryProcess() method ... OK");

        System.out.println(responseEntity.toString());
        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
    }

    @Test
    void queryProcess_IdealScenario() throws Exception {

        String agreementID="20966786-4524-43e0-a5ea-6b18fe586630";
        String query="";
        QueryMessage queryMessage=testUtil.queryMessage();

        // Build parameters
        request.setParameter(HEADER, queryMessage.toRdf());
        request.setParameter(PAYLOAD, query);

        // Checks how many documents exist for that agreement, before test
        assertThat(mongoService.findAllMessagesByAgreement(agreementID).size()).isEqualTo(4);

        // Send message
        LOGGER.info("Calling queryProcess() method ...");
        ResponseEntity<?> responseEntity= controller.queryProcess(UUID.fromString(agreementID), request, headersMap, queryMessage.toRdf(), query);
        LOGGER.info("Calling queryProcess() method ... OK");

        System.out.println(responseEntity.toString());
        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());


        String responseBody= IOUtils.toString((byte[])responseEntity.getBody());

        // Extract payload to obtain the response
        Map<String, Object> responseMap=ClearingHouseMessageConverter.parseMultipartContent(responseBody);
        System.out.println(responseMap.keySet());

        Message responseHeader=MessageUtil.deserialize(responseMap.get(HEADER).toString(), Message.class);

        System.out.println(responseMap.get(PAYLOAD).toString());


        QueryDto responsePayload= jsonConverter.string2Object(QueryDto.class, responseMap.get(PAYLOAD).toString());
        assertThat(responseHeader).isInstanceOf(ResultMessage.class);
        assertThat(responsePayload).isInstanceOf(QueryDto.class);
        // Documents is less 1 because the process is not sent
        assertThat(responsePayload.getDocuments().size()).isEqualTo(3);


    }


    @Test
    void queryProcess_OnlyProcess() throws Exception {

        String agreementID="2a99e7d7-123f-41ff-b97e-48e52fc2a1a5";
        String query="";
        QueryMessage queryMessage=testUtil.queryMessage();

        // Build parameters
        request.setParameter(HEADER, queryMessage.toRdf());
        request.setParameter(PAYLOAD, query);

        // Checks how many documents exist for that agreement, before test
        assertThat(mongoService.findAllMessagesByAgreement(agreementID).size()).isEqualTo(1);

        // Send message
        LOGGER.info("Calling queryProcess() method ...");
        ResponseEntity<?> responseEntity= controller.queryProcess(UUID.fromString(agreementID), request, headersMap, queryMessage.toRdf(), query);
        LOGGER.info("Calling queryProcess() method ... OK");

        System.out.println(responseEntity.toString());
        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());


        String responseBody= IOUtils.toString((byte[])responseEntity.getBody());

        // Extract payload to obtain the response
        Map<String, Object> responseMap=ClearingHouseMessageConverter.parseMultipartContent(responseBody);
        System.out.println(responseMap.toString());

        Message responseHeader=MessageUtil.deserialize(responseMap.get(HEADER).toString(), Message.class);
        System.out.println(responseMap.get(PAYLOAD).toString());


        QueryDto responsePayload= jsonConverter.string2Object(QueryDto.class, responseMap.get(PAYLOAD).toString());
        assertThat(responseHeader).isInstanceOf(ResultMessage.class);
        assertThat(responsePayload).isInstanceOf(QueryDto.class);
        // Documents is less 1 because the process is not sent
        assertThat(responsePayload.getDocuments().size()).isEqualTo(0);


    }

    @Test
    void queryProcess_ProcessWithOnlyAOwner() throws Exception {

        String agreementID="a48fdf17-ce1a-4fca-bd5f-82e4c307f96f";
        String query="";
        QueryMessage queryMessage=testUtil.queryMessage();

        // Build parameters
        request.setParameter(HEADER, queryMessage.toRdf());
        request.setParameter(PAYLOAD, query);

        // Checks how many documents exist for that agreement, before test
        assertThat(mongoService.findAllMessagesByAgreement(agreementID).size()).isEqualTo(2);

        // Send message
        LOGGER.info("Calling queryProcess() method ...");
        ResponseEntity<?> responseEntity= controller.queryProcess(UUID.fromString(agreementID), request, headersMap, queryMessage.toRdf(), query);
        LOGGER.info("Calling queryProcess() method ... OK");

        System.out.println(responseEntity.toString());
        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());


        String responseBody= IOUtils.toString((byte[])responseEntity.getBody());

        // Extract payload to obtain the response
        Map<String, Object> responseMap=ClearingHouseMessageConverter.parseMultipartContent(responseBody);
        //System.out.println(responseMap.keySet());

        Message responseHeader=MessageUtil.deserialize(responseMap.get(HEADER).toString(), Message.class);

        System.out.println(responseMap.get(PAYLOAD).toString());


        QueryDto responsePayload= jsonConverter.string2Object(QueryDto.class, responseMap.get(PAYLOAD).toString());
        assertThat(responseHeader).isInstanceOf(ResultMessage.class);
        assertThat(responsePayload).isInstanceOf(QueryDto.class);
        // Documents is less 1 because the process is not sent
        assertThat(responsePayload.getDocuments().size()).isEqualTo(1);


    }

    @Test
    void queryProcess_WithUsageLogging() throws Exception {
        // Add Logging Message to Database
        ClearingHouseMessage usageLoggingMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.getUsageLogging())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        mongoService.insert(usageLoggingMessage, LOGGING);

        String agreementID="20966786-4524-43e0-a5ea-6b18fe586630";
        String query="";
        QueryMessage queryMessage=testUtil.queryMessage();

        // Build parameters
        request.setParameter(HEADER, queryMessage.toRdf());
        request.setParameter(PAYLOAD, query);

        // Checks how many documents exist for that agreement, before test
        assertThat(mongoService.findAllMessagesByAgreement(agreementID).size()).isEqualTo(5);

        // Send message
        LOGGER.info("Calling queryProcess() method ...");
        ResponseEntity<?> responseEntity= controller.queryProcess(UUID.fromString(agreementID), request, headersMap, queryMessage.toRdf(), query);
        LOGGER.info("Calling queryProcess() method ... OK");

        System.out.println(responseEntity.toString());
        System.out.println(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());


        String responseBody= IOUtils.toString((byte[])responseEntity.getBody());

        // Extract payload to obtain the response
        Map<String, Object> responseMap=ClearingHouseMessageConverter.parseMultipartContent(responseBody);
        System.out.println(responseMap.keySet());

        Message responseHeader=MessageUtil.deserialize(responseMap.get(HEADER).toString(), Message.class);

        System.out.println(responseMap.get(PAYLOAD).toString());


        QueryDto responsePayload= jsonConverter.string2Object(QueryDto.class, responseMap.get(PAYLOAD).toString());
        assertThat(responseHeader).isInstanceOf(ResultMessage.class);
        assertThat(responsePayload).isInstanceOf(QueryDto.class);
        // Documents is less 1 because the process is not sent
        assertThat(responsePayload.getDocuments().size()).isEqualTo(4);


    }

    private void populateDatabase(){
        // Populate Database

        // Ideal scenario - Agreement ID: 20966786-4524-43e0-a5ea-6b18fe586630
        Processes processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        ClearingHouseMessage contractAgreement=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.contractAgreement().toRdf())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        ClearingHouseMessage artefactRequest=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        ClearingHouseMessage artefactResponse=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactResponseMessage().toRdf())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        mongoService.insert(processMessage, PROCESS);
        mongoService.insert(contractAgreement, CONTRACTS);
        mongoService.insert(artefactRequest, MESSAGES);
        mongoService.insert(artefactResponse, MESSAGES);

        // Only Process
        Processes processMessage2=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("2a99e7d7-123f-41ff-b97e-48e52fc2a1a5")
                .build();
        mongoService.insert(processMessage2, PROCESS);


        // Test the Unauthorize user. Because I did not insert a Process with owners for this agreement
        ClearingHouseMessage logMessageUsedToTestUnauthorize=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("015fd3fb-36fb-4bfd-8cac-2f49aeafb328")
                .build();
        mongoService.insert(logMessageUsedToTestUnauthorize, MESSAGES);


        // Test the case of a log not having a process. If using the Controller, a process with only a owner would be created. Owner is test producer
        processMessage2=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(List.of("BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69:E6:25:47:BA:09:8D:98:E5:DF:79:3F:09:89:F4:4D:83"))
                .agreementID("a48fdf17-ce1a-4fca-bd5f-82e4c307f96f")
                .build();
        artefactRequest=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("a48fdf17-ce1a-4fca-bd5f-82e4c307f96f")
                .build();
        mongoService.insert(processMessage2, PROCESS);
        mongoService.insert(artefactRequest, MESSAGES);


    }
}