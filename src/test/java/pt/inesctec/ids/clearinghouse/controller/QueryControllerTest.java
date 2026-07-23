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
package pt.inesctec.ids.clearinghouse.controller;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.ResultMessage;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.converter.ClearingHouseMessageConverter;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.dto.QueryDto;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.service.MongoService;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.MESSAGES;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueryControllerTest {

    @Value(value="${server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MongoService mongoService;
    @Autowired private MongoTemplate mongoTemplate;

    TestUtil testUtil;
    UUID agreementID;
    JsonConverter jsonConverter=new JsonConverter();

    ClearingHouseMessage contractAgreement, artefactRequest, logMessageUnauthorize;
    Processes processMessage;


    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();
        agreementID=UUID.fromString("20966786-4524-43e0-a5ea-6b18fe586630");
        //mongoService.removeAll();

        populateDatabase();
    }

    @AfterEach
    void tearDown() {
        mongoService.remove(processMessage, PROCESS);
        mongoService.remove(contractAgreement, CONTRACTS);
        mongoService.remove(artefactRequest, MESSAGES);
        mongoService.remove(logMessageUnauthorize, MESSAGES);
    }


    @Test
    void queryProcess() throws Exception {
        // Prepare test
        Message headerToSend=testUtil.queryMessage();

        MultiValueMap<String, String> parts = new LinkedMultiValueMap<>();
        parts.add(HEADER, headerToSend.toRdf());
        parts.add(PAYLOAD, "\"\"");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        System.err.println(headers);
        //headers.forEach(System.err::printf);

        Query query = new Query();
        query.addCriteria(Criteria.where("agreementID").is(agreementID.toString()));


        // Test
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parts, headers);
        System.out.println("Starting test...");

        ResponseEntity<?> response = this.restTemplate
                .postForEntity("http://localhost:" + 8083 + "/messages/query/{id}",
                        requestEntity,
                        byte[].class,
                        agreementID
                );
        System.out.println("Starting test... FINISHED");

        System.out.println(response.toString());

        // Get response values
        HttpStatus responseStatus=response.getStatusCode();
        String responseBody= IOUtils.toString((byte[])response.getBody());

        // Handle the response status
        System.out.println(response.getStatusCode());
        assertThat(responseStatus).isEqualTo(HttpStatus.CREATED);


        // Handle the response body
        //System.out.println(responseBody);
        Map<String, Object> responseMap= ClearingHouseMessageConverter.parseMultipartContent(responseBody);

        // Deserialize the Message header
        Message responseHeader= MessageUtil.deserialize(responseMap.get(HEADER).toString(), Message.class);

        // Convert the Body (Payload) into the QueryDTO
        QueryDto responsePayload= jsonConverter.string2Object(QueryDto.class, responseMap.get(PAYLOAD).toString());

        // Test
        assertThat(responseHeader).isInstanceOf(ResultMessage.class);
        assertThat(responsePayload).isInstanceOf(QueryDto.class);
        // Documents is less 1 because the process is not sent
        assertThat(responsePayload.getDocuments().size()).isEqualTo(2);

    }

    @Test
    void queryProcess_Unauthorize() {
        // Prepare test
        Message headerToSend=testUtil.queryMessage();

        MultiValueMap<String, String> parts = new LinkedMultiValueMap<>();
        parts.add(HEADER, headerToSend.toRdf());
        parts.add(PAYLOAD, "\"\"");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        System.err.println(headers);
        //headers.forEach(System.err::printf);

        Query query = new Query();
        query.addCriteria(Criteria.where("agreementID").is(agreementID.toString()));


        // Test
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parts, headers);
        System.out.println("Starting test...");

        ResponseEntity<?> response = this.restTemplate
                .postForEntity("http://localhost:" + 8083 + "/messages/query/{id}",
                        requestEntity,
                        byte[].class,
                        logMessageUnauthorize.getAgreementID()
                );
        System.out.println("Starting test... FINISHED");

        System.out.println(response.toString());

        // Get response values
        HttpStatus responseStatus=response.getStatusCode();
        String responseBody= IOUtils.toString((byte[])response.getBody());

        // Handle the response status
        System.out.println(response.getStatusCode());
        assertThat(responseStatus).isEqualTo(HttpStatus.UNAUTHORIZED);

        // Handle the response body
        System.out.println(responseBody);
        Map<String, Object> responseMap= ClearingHouseMessageConverter.parseMultipartContent(responseBody);

        // Deserialize the Message header
        Message responseHeader= MessageUtil.deserialize(responseMap.get(HEADER).toString(), Message.class);
        assertThat(responseHeader).isInstanceOf(RejectionMessage.class);

    }

    private void populateDatabase(){
        // Populate Database

        // Ideal scenario - Agreement ID: 20966786-4524-43e0-a5ea-6b18fe586630
        processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        contractAgreement=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.contractAgreement().toRdf())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        artefactRequest=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("20966786-4524-43e0-a5ea-6b18fe586630")
                .build();
        mongoService.insert(processMessage, PROCESS);
        mongoService.insert(contractAgreement, CONTRACTS);
        mongoService.insert(artefactRequest, MESSAGES);

        // Only LogMessage. Since there is no process the user does not have permissions
        logMessageUnauthorize=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("015fd3fb-36fb-4bfd-8cac-2f49aeafb328")
                .build();
        mongoService.insert(logMessageUnauthorize, MESSAGES);


    }
}