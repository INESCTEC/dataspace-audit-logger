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

import com.mongodb.client.result.DeleteResult;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.converter.ClearingHouseMessageConverter;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.service.MongoService;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.MESSAGES;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.PROCESS;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoggingControllerTest {

    @Value(value="${server.port}")
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private MongoService mongoService;
    @Autowired private MongoTemplate mongoTemplate;

    TestUtil testUtil;
    UUID agreementID;


    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();
        agreementID=UUID.fromString("015fd3fb-36fb-4bfd-8cac-2f49aeafb328");
    }

    @AfterEach
    void tearDown() {
    }

    @Order(1)
    @Test
    void addProcess() {
        // Prepare test
        Message headerToSend=testUtil.requestMessage();
        String payloadToSend_String=testUtil.owners();
        List<String> payloadToSend_List=testUtil.owners_List();

        MultiValueMap<String, String> parts = new LinkedMultiValueMap<>();
        parts.add(HEADER, headerToSend.toRdf());
        parts.add(PAYLOAD, payloadToSend_String.toString());

        Query query = new Query();
        query.addCriteria(Criteria.where("agreementID").is(agreementID.toString()).and("owners").is(payloadToSend_List));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //System.err.println(headers);
        headers.forEach(System.err::printf);

        // Save number of processses o DB before test
        Integer numberOfProcessesOnDB=mongoTemplate.find(query, Processes.class, PROCESS).size();
        System.out.println("Number of entries on collection 'Processes' is "+numberOfProcessesOnDB);

        // Test
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parts, headers);
        ResponseEntity<?> response = this.restTemplate
                .postForEntity("http://localhost:" + 8083 + "/process/{id}",
                        requestEntity,
                        byte[].class,
                        agreementID
                );

        HttpStatus responseStatus=response.getStatusCode();
        //String responseBody= IOUtils.toString((byte[])response.getBody());

        System.out.println(response.getStatusCode());
        //System.out.println(responseBody);

        // Verify if test passed
        ClearingHouseMessageConverter converter=new ClearingHouseMessageConverter();
        Map<String, Object> messageResponse = converter.convert2Map((byte[])response.getBody());
        System.err.println(messageResponse.get(HEADER).toString());
        Message deserializedHeader= MessageUtil.deserialize(messageResponse.get(HEADER).toString(), MessageProcessedNotificationMessage.class);

        assertThat(responseStatus).isEqualTo(HttpStatus.CREATED);
        assertThat(messageResponse.get(PAYLOAD)).isEqualTo(agreementID.toString());
        assertThat(deserializedHeader).isInstanceOf(MessageProcessedNotificationMessage.class);

        // Checks that the Message was inserted into the Database
        System.out.println(query);
        List<?> queryResponseList=mongoTemplate.find(query, Processes.class, PROCESS);
        //queryResponseList.forEach(System.out::println);
        System.out.println("AFTER TEST: Number of entries on collection 'Processes' is "+queryResponseList.size());
        // It is 2 because there is RequestMessage from the Producer and other from the Consumer
        assertThat(queryResponseList.size()).isEqualTo(numberOfProcessesOnDB+1);


        queryResponseList.forEach(messageInserted ->{
            DeleteResult removedMessages=mongoTemplate.remove(messageInserted, PROCESS);
            assertThat(removedMessages.getDeletedCount()).isEqualTo(1);

        });
    }

    @Test    @Order(2)
    void addLog_ProcessAlreadyExists(){
        // Add a Process for that agreement -> Preparing environment for the test
        Processes processMessage=new Processes(agreementID.toString(), testUtil.requestMessage().toRdf(), testUtil.owners_List());
        System.out.println(processMessage.getTimestamp().getClass());
        mongoService.insert(processMessage, PROCESS);

        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.artefactRequestMessage().toRdf();

        MultiValueMap<String, String> parts = new LinkedMultiValueMap<>();
        parts.add(HEADER, headerToSend.toRdf());
        parts.add(PAYLOAD, payloadToSend);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //System.err.println(headers);
        headers.forEach(System.err::printf);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parts, headers);


        ResponseEntity<?> response = this.restTemplate
                .postForEntity("http://localhost:" + 8083 + "/messages/log/{id}",
                        requestEntity,
                        byte[].class,
                        agreementID
                );

        HttpStatus responseStatus=response.getStatusCode();
        String responseBody= IOUtils.toString((byte[])response.getBody());

        System.out.println(response.getStatusCode());
        System.out.println(responseBody);

        ClearingHouseMessageConverter clearingHouseMessageConverter=new ClearingHouseMessageConverter();
        Map<String, Object> messageResponse = clearingHouseMessageConverter.convert2Map((byte[])response.getBody());
        System.err.println("Message Response: \n"+messageResponse);
        Message deserializedHeader=MessageUtil.deserialize(messageResponse.get(HEADER).toString(), Message.class);

        assertThat(responseStatus).isEqualTo(HttpStatus.CREATED);
        //assertThat(messageResponse.get(PAYLOAD)).isEqualTo(LogUtil.buildLogPayload("eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzUxMiJ9.ewogICJpZCIgOiAiNjYyNjhiNDU3YWFiYWQwZTg0MzdmZDg3IiwKICAiaGVhZGVyIiA6ICJ7XG4gIFwiQGNvbnRleHRcIiA6IHtcbiAgICBcImlkc1wiIDogXCJodHRwczovL3czaWQub3JnL2lkc2EvY29yZS9cIixcbiAgICBcImlkc2NcIiA6IFwiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvZGUvXCJcbiAgfSxcbiAgXCJAdHlwZVwiIDogXCJpZHM6TG9nTWVzc2FnZVwiLFxuICBcIkBpZFwiIDogXCJodHRwczovL21lc3NhZ2UvaWQvMDFcIixcbiAgXCJpZHM6bW9kZWxWZXJzaW9uXCIgOiBcIjQuMS4wXCIsXG4gIFwiaWRzOmlzc3VlZFwiIDoge1xuICAgIFwiQHZhbHVlXCIgOiBcIjIwMjQtMDQtMThUMTI6MDk6MzMuMDAwWlwiLFxuICAgIFwiQHR5cGVcIiA6IFwiaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjZGF0ZVRpbWVTdGFtcFwiXG4gIH0sXG4gIFwiaWRzOmNvcnJlbGF0aW9uTWVzc2FnZVwiIDoge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vY29ycmVsYXRpb25NZXNzYWdlL2xvZ01lc3NhZ2UvaWQvMDFcIlxuICB9LFxuICBcImlkczppc3N1ZXJDb25uZWN0b3JcIiA6IHtcbiAgICBcIkBpZFwiIDogXCJodHRwczovL2lzc3VlckNvbm5lY3Rvci9pZC9jb25uZWN0b3ItMDFcIlxuICB9LFxuICBcImlkczpzZW5kZXJBZ2VudFwiIDoge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vc2VuZGVyQWdlbnQvaWQvY29ubmVjdG9yLTAxXCJcbiAgfSxcbiAgXCJpZHM6c2VjdXJpdHlUb2tlblwiIDoge1xuICAgIFwiQHR5cGVcIiA6IFwiaWRzOkR5bmFtaWNBdHRyaWJ1dGVUb2tlblwiLFxuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vc2VjdXJpdHlUb2tlbi9kYXQvaWQvMDFcIixcbiAgICBcImlkczp0b2tlblZhbHVlXCIgOiBcImV5SjBlWEFpT2lKaGRDdHFkM1FpTENKcmFXUWlPaUkyTUdKbFptWXhZV1EyTmpKbE16aG1aRGc1T1RZMk16a3lPRFpsTTJNeU5HVXlZak0yTm1VMU1tWTROMlE0T0RJMU1XSTROVFF3T1RabFpqYzRZek01SWl3aVlXeG5Jam9pVWxNeU5UWWlmUS5leUp6WTI5d1pTSTZJbWxrYzJNNlNVUlRYME5QVGs1RlExUlBVbDlCVkZSU1NVSlZWRVZUWDBGTVRDSXNJbUYxWkNJNld5SnBaSE5qT2tsRVUxOURUMDVPUlVOVVQxSlRYMEZNVENKZExDSnBjM01pT2lKb2RIUndjem92TDNaalpYTmxNVGt1YVc1bGMyTjBaV011Y0hRdllYVjBhQ0lzSW5OMVlpSTZJa0pDT2tFMU9rSTVPakEwT2pJMU9qQXdPakZET2pVMk9qRkNPakU0T2pBM09qVkJPakpGT2tWRk9qRkZPak0wT2pZNE9rSkZPa05GT2tRek9tdGxlV2xrT2pJM09rRTRPa1EyT2pZNU9rVTJPakkxT2pRM09rSkJPakE1T2poRU9qazRPa1UxT2tSR09qYzVPak5HT2pBNU9qZzVPa1kwT2pSRU9qZ3pJaXdpYm1KbUlqb3hOekV6TVRjMU5UVTVMQ0pwWVhRaU9qRTNNVE14TnpVMU5Ua3NJbXAwYVNJNkltVXpPRGxqT1dabUxXVmhaRGt0TkRnNE9DMDVOek5sTFdZMU16Rm1ZVFZqTmpRd05DSXNJbVY0Y0NJNk1UY3hNekUzT1RFMU9Td2lZMnhwWlc1MFgybGtJam9pUWtJNlFUVTZRams2TURRNk1qVTZNREE2TVVNNk5UWTZNVUk2TVRnNk1EYzZOVUU2TWtVNlJVVTZNVVU2TXpRNk5qZzZRa1U2UTBVNlJETTZhMlY1YVdRNk1qYzZRVGc2UkRZNk5qazZSVFk2TWpVNk5EYzZRa0U2TURrNk9FUTZPVGc2UlRVNlJFWTZOems2TTBZNk1EazZPRGs2UmpRNk5FUTZPRE1pTENKelpXTjFjbWwwZVZCeWIyWnBiR1VpT2lKcFpITmpPa0pCVTBWZlUwVkRWVkpKVkZsZlVGSlBSa2xNUlNJc0luSmxabVZ5Y21sdVowTnZibTVsWTNSdmNpSTZJbWgwZEhCek9pOHZZMjl1Wm1GamRITXVZMjl0TDJsa2N5OWpiMjV1WldOMGIzSXZhV1F2Y0hKdlpIVmpaWElpTENKQWRIbHdaU0k2SW1sa2N6cEVZWFJRWVhsc2IyRmtJaXdpUUdOdmJuUmxlSFFpT2lKb2RIUndjem92TDNjemFXUXViM0puTDJsa2MyRXZZMjl1ZEdWNGRITXZZMjl1ZEdWNGRDNXFjMjl1YkdRaUxDSjBjbUZ1YzNCdmNuUkRaWEowYzFOb1lUSTFOaUk2SWpZM05UWXhNbUl5Tm1JNFlUUXlabUV5TWpGa1lURTJOVGhtTTJRNU1qZzVZMlV6T1RobU1HTTJPR05sTXpSaE9ERmpZVFl3T1dZNE1XWTNaR1ExTWpBaWZRLkhsSnBRakRsTHBOOUZtZ2lybVRSdVBadGpCT2JyRlhLTFdidFZkQm1Sb0RJdUM4WVp1T0hLNGpfcTgwenRrLUp4bzJ0OXJEQUFDQ0l0OXVOV3pLWHUzQWpqMjhZaFlZX0FXY3Q4aVN0d1c0cDA5SmJqY1Zoc2hPX2N1akREMVlxOTg3aVQtUmpiREdNVDdZUFRZVTNCdDFiT3VQdUFNdkVxdXEwa3FmN3pnUjZaUHdwZmIyTFVNbXZnbWNpeGE3Yk9MWkNJR29FT2w4ZHVpS0tNWGNyXzJHSi1aYS1lWXB5ckJwaW1yU1dmd1A5ZUNKaU4wZFF1VnlkeTRKQklwVWNMYVkwOUFzNnhyRmNEdTBPNWM1bjY5ZWVHckxGRHJoUTl0Vmo3WGYtQjJOU2ItREJINERqekZ5YS00V1p5RUZaWDBxdEFRY3JtdWRzVDc1MDJWZl9FUVwiLFxuICAgIFwiaWRzOnRva2VuRm9ybWF0XCIgOiB7XG4gICAgICBcIkBpZFwiIDogXCJodHRwczovL3czaWQub3JnL2lkc2EvY29kZS9KV1RcIlxuICAgIH1cbiAgfVxufSIsCiAgInBheWxvYWQiIDogIntcbiAgXCJAY29udGV4dFwiIDoge1xuICAgIFwiaWRzXCIgOiBcImh0dHBzOi8vdzNpZC5vcmcvaWRzYS9jb3JlL1wiLFxuICAgIFwiaWRzY1wiIDogXCJodHRwczovL3czaWQub3JnL2lkc2EvY29kZS9cIlxuICB9LFxuICBcIkB0eXBlXCIgOiBcImlkczpBcnRpZmFjdFJlcXVlc3RNZXNzYWdlXCIsXG4gIFwiQGlkXCIgOiBcImh0dHBzOi8vbWVzc2FnZS9pZC8wMVwiLFxuICBcImlkczpzZWN1cml0eVRva2VuXCIgOiB7XG4gICAgXCJAdHlwZVwiIDogXCJpZHM6RHluYW1pY0F0dHJpYnV0ZVRva2VuXCIsXG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9zZWN1cml0eVRva2VuL2RhdC9pZC8wMVwiLFxuICAgIFwiaWRzOnRva2VuVmFsdWVcIiA6IFwiZXlKMGVYQWlPaUpoZEN0cWQzUWlMQ0pyYVdRaU9pSTJNR0psWm1ZeFlXUTJOakpsTXpobVpEZzVPVFkyTXpreU9EWmxNMk15TkdVeVlqTTJObVUxTW1ZNE4yUTRPREkxTVdJNE5UUXdPVFpsWmpjNFl6TTVJaXdpWVd4bklqb2lVbE15TlRZaWZRLi4tR3pqUlFcIixcbiAgICBcImlkczp0b2tlbkZvcm1hdFwiIDoge1xuICAgICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvZGUvSldUXCJcbiAgICB9XG4gIH0sXG4gIFwiaWRzOnJlY2lwaWVudENvbm5lY3RvclwiIDogWyB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6L2xvY2FsaG9zdDo4MDgwL2Nvbm5lY3Rvci0wMS9hcGkvaWRzL2RhdGFcIlxuICB9IF0sXG4gIFwiaWRzOmlzc3VlZFwiIDoge1xuICAgIFwiQHZhbHVlXCIgOiBcIjIwMjQtMDQtMThUMTI6MDk6MzMuMDAwWlwiLFxuICAgIFwiQHR5cGVcIiA6IFwiaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEjZGF0ZVRpbWVTdGFtcFwiXG4gIH0sXG4gIFwiaWRzOnNlbmRlckFnZW50XCIgOiB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9zZW5kZXJBZ2VudC9pZC9jb25uZWN0b3ItMDFcIlxuICB9LFxuICBcImlkczptb2RlbFZlcnNpb25cIiA6IFwiNC4xLjBcIixcbiAgXCJpZHM6aXNzdWVyQ29ubmVjdG9yXCIgOiB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9pc3N1ZXJDb25uZWN0b3IvaWQvY29ubmVjdG9yLTAxXCJcbiAgfSxcbiAgXCJpZHM6Y29ycmVsYXRpb25NZXNzYWdlXCIgOiB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9jb3JyZWxhdGlvbk1lc3NhZ2UvbG9nTWVzc2FnZS9pZC8wMVwiXG4gIH0sXG4gIFwiaWRzOnRyYW5zZmVyQ29udHJhY3RcIiA6IHtcbiAgICBcIkBpZFwiIDogXCJodHRwczovL2xvY2FsaG9zdDo4MDgwL2Nvbm5lY3Rvci0wMS9hcGkvYWdyZWVtZW50cy81MDA2MTdjYy0yNWRiLTQ2NzItOGIxYS0yNzgxYWM0OThiYzBcIlxuICB9LFxuICBcImlkczpyZXF1ZXN0ZWRBcnRpZmFjdFwiIDoge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vbG9jYWxob3N0OjgwODAvY29ubmVjdG9yLTAxL2FwaS9hcnRpZmFjdHMvNTAwNjE3Y2MtMjVkYi00NjcyLThiMWEtMjc4MWFjNDk4YmMwXCJcbiAgfVxufSIsCiAgInRpbWVzdGFtcCIgOiAxNzEzODAyMDUzNjM1LAogICJjbGllbnRfaWQiIDogIkJCOkE1OkI5OjA0OjI1OjAwOjFDOjU2OjFCOjE4OjA3OjVBOjJFOkVFOjFFOjM0OjY4OkJFOkNFOkQzOmtleWlkOjI3OkE4OkQ2OjY5OkU2OjI1OjQ3OkJBOjA5OjhEOjk4OkU1OkRGOjc5OjNGOjA5Ojg5OkY0OjREOjgzIiwKICAic2lnblNlcnZpY2UiIDogeyB9Cn0.UmA82eTjfEtvSsAelyJ7vsI1LCTuxWqX-3JDBc459QRE9EU70nZHmK_QnT86V62W3yROb9KbURzH15oaa16ep1WI9Lxp1T7W1RmwUAAB7VLG_l66Diaj3TCXNsIPmc0vK5vmIBEwSiE3GxAb2bFk2_g8vHTKSA_DLO5mdUAIoAxtTSxG0B1Ubo8-nRP--HFmxwgWGmSk8EAfpoexSBkwOiyMe_ewmAFC75p30CSc2DShtjiTNGWWO5aY4pHRfdxQY955WPa6qwebLcnp7ujpOff_dQOW6MwbRDdRpO9mpki9hgUxieqnDWmxEJ-B6WM477SjUXMWoCO1IrZAoZ3t3-SGlyRY41YPEtYI-THdnJcaTxF_ChFMr4RsBAza15h-BndepXMbW41RKHcyvSGm8gGk-TXMigTteN1lAFE97Wwa0JhAZ_aa1VCFSYL40NNzael7ImU1iJbHViEQGcycw_xl_650WretkVgKWjMvQwOzxKriTTdBoWo0NkOzZWJwmbUJg2lj_lXC1Sb347K4Byia-oBG8iyCFVbrWaa2c1AaMKu6qr_cUXqmLNleo4edj4NAGF0QDIIQ3kjr6EEZ9C1GaZFr48kTMTWMCEKjjS7jxnFPk-w50Lfgp92srnVy1Uqz_svzsnrNuQCLuLv3HQ99voZqOGzI_Z7XenZ8bhk"));
        assertThat(deserializedHeader).isInstanceOf(MessageProcessedNotificationMessage.class);

        // Checks that the Message was inserted into the Database
        Query query = new Query();
        query.addCriteria(Criteria.where("agreementID").is(agreementID.toString()));
        /*query.addCriteria(Criteria.where(PAYLOAD).is(parts.get(PAYLOAD)));
        query.addCriteria(Criteria.where(HEADER).is(parts.get(HEADER).toString()));*/
        System.out.println(query);
        List<?> queryResponseList= mongoTemplate.find(query, ClearingHouseMessage.class, MESSAGES);


        // Remove the Message inserted
        queryResponseList.forEach(messageInserted ->{
            DeleteResult removedMessages=mongoTemplate.remove(messageInserted, MESSAGES);
            assertThat(removedMessages.getDeletedCount()).isEqualTo(1);
        });

        // Remove the Process message inserted
        DeleteResult removedMessages=mongoTemplate.remove(processMessage, PROCESS);
        assertThat(removedMessages.getDeletedCount()).isEqualTo(1);
    }

    @Test
    void addLog_ThereAreNoProcessesForTheAgreement() {
        Message headerToSend=testUtil.logMessage();
        String payloadToSend=testUtil.artefactRequestMessage().toRdf();
        agreementID=UUID.fromString("48569156-36fb-4bfd-8cac-2f49aeafb328");

        MultiValueMap<String, String> parts = new LinkedMultiValueMap<>();
        parts.add(HEADER, headerToSend.toRdf());
        parts.add(PAYLOAD, payloadToSend);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        //System.err.println(headers);
        headers.forEach(System.err::printf);

        Query query = new Query();
        query.addCriteria(Criteria.where("agreementID").is(agreementID.toString()));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parts, headers);


        // Before inserting, check if the agreement is stored on the database
        List<?> queryResponseList= mongoTemplate.find(query, ClearingHouseMessage.class);
        System.out.println(queryResponseList.size());
        assertThat(queryResponseList.size()).isEqualTo(0);


        // Sends request
        ResponseEntity<?> response = this.restTemplate
                .postForEntity("http://localhost:" + 8083 + "/messages/log/{id}",
                        requestEntity,
                        byte[].class,
                        agreementID
                );

        HttpStatus responseStatus=response.getStatusCode();
        String responseBody= IOUtils.toString((byte[])response.getBody());
        System.out.println(response.getStatusCode());
        System.out.println(responseBody);

        ClearingHouseMessageConverter clearingHouseMessageConverter=new ClearingHouseMessageConverter();
        Map<String, Object> messageResponse = clearingHouseMessageConverter.convert2Map((byte[])response.getBody());
        System.err.println("Message Response: \n"+messageResponse);
        Message deserializedHeader=MessageUtil.deserialize(messageResponse.get(HEADER).toString(), Message.class);

        assertThat(responseStatus).isEqualTo(HttpStatus.CREATED);
        assertThat(deserializedHeader).isInstanceOf(MessageProcessedNotificationMessage.class);


        // Checks that the code inserted a Process
        List<ClearingHouseMessage> processFromAgreement= mongoTemplate.find(query, ClearingHouseMessage.class, PROCESS);
        System.out.println(processFromAgreement.size());
        processFromAgreement.forEach(System.err::println);
        assertThat(processFromAgreement.size()).isEqualTo(1);

        // Checks that the Message was inserted into the Database
        System.out.println(query);
        queryResponseList= mongoTemplate.find(query, ClearingHouseMessage.class, MESSAGES);


        queryResponseList.forEach(messageInserted ->{
            DeleteResult removedMessages=mongoTemplate.remove(messageInserted, MESSAGES);
            assertThat(removedMessages.getDeletedCount()).isEqualTo(1);

        });
        // Remove the Process message inserted
        processFromAgreement.forEach(process ->{
            DeleteResult removedMessages=mongoTemplate.remove(process, PROCESS);
            assertThat(removedMessages.getDeletedCount()).isEqualTo(1);
        });

    }

}
