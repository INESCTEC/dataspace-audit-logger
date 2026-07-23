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
package pt.inesctec.ids.clearinghouse.dto;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.fraunhofer.iais.eis.Message;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.MultipartMessageBuilder;
import pt.inesctec.ids.clearinghouse.converter.ClearingHouseMessageConverter;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

@SpringBootTest
class ResponseMessageTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseMessage.class);
    TestUtil testUtil;
    ClearingHouseMessageConverter converter;

    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();
        converter=new ClearingHouseMessageConverter();
    }

    @Test
    void constructor_Map() {
        Message headerMessage=testUtil.headerMessage();
        String payload= testUtil.payload();

        Map<String, Object> multipartMap =new HashMap<>();
        multipartMap.put(HEADER, headerMessage);
        multipartMap.put(PAYLOAD, payload);

        ResponseMessage response=new ResponseMessage(multipartMap);
        /*System.out.println("Date Before creating message: "+headerMessage.getIssued());
        System.out.println("Date After creating message: "+response.getHeader().getIssued());*/

        // If comparing the strings the date will fail because of the Z on the deserialized date
        assertEquals(response.getHeader(), headerMessage);
        assertEquals(response.getPayload(), payload);
    }

    @Test
    void constructor_Default() {
        Message headerMessage=testUtil.headerMessage();
        String payload= testUtil.payload();

        ResponseMessage response=new ResponseMessage(headerMessage, payload);
        /*System.out.println("Date Before creating message: "+headerMessage.getIssued());
        System.out.println("Date After creating message: "+response.getHeader().getIssued());*/
        assertEquals(response.getHeader(), headerMessage);
        assertEquals(response.getPayload(), payload);
    }

    @Test
    void constructor_PayloadInBytes() {
        Message headerMessage=testUtil.headerMessage();
        String payload= testUtil.payload();

        ResponseMessage response=new ResponseMessage(headerMessage, payload.getBytes());
        assertEquals(response.getHeader(), headerMessage);
        assertEquals(response.getPayload(), payload);
    }

    /*@Test
    void serialize() {
    }*/

    @Test
    void buildMultipart() {
        // Prepare header and Payload
        LOGGER.info("Setting up 'header' and 'payload' ... ");
        Message headerMessage=testUtil.headerMessage();
        String payload= testUtil.payload();
        LOGGER.info("Setting up 'header' and 'payload' ... OK");

        LOGGER.info("Setting up 'builder' ...");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // Set builder values
        builder.setMode(HttpMultipartMode.STRICT);
        //builder.setMode(HttpMultipartMode.LEGACY);
        builder.setBoundary(UUID.randomUUID().toString());
        builder.setCharset(StandardCharsets.UTF_8);
        builder.setContentType(ContentType.APPLICATION_JSON);

        // Add header
        final StringBody headerAsStringBody = new StringBody(headerMessage.toRdf(), ContentType.MULTIPART_FORM_DATA);
        final StringBody payloadAsStringBody = new StringBody(payload, ContentType.MULTIPART_FORM_DATA);

        builder.addPart(HEADER, headerAsStringBody);
        builder.addPart(PAYLOAD, payloadAsStringBody);
        LOGGER.info("Setting up 'builder' ... OK");

        LOGGER.info("Setting up 'HttpEntity' ...");
        HttpEntity expectedHttpEntity=builder.build();
        LOGGER.info("Setting up 'HttpEntity' ... OK");


        // Test
        HttpEntity httpEntity= MultipartMessageBuilder.buildMessage(headerMessage, payload);
        assertNotNull(httpEntity);
        assertEquals(converter.convert(httpEntity), converter.convert(expectedHttpEntity));
    }

    @Test
    void toJson() throws IOException {
        // Prepare header and Payload
        LOGGER.info("Setting up 'header' and 'payload' ... ");
        Message headerMessage=testUtil.headerMessage();
        String payload= testUtil.payload();
        LOGGER.info("Setting up 'header' and 'payload' ... OK");

        LOGGER.info("Setting up 'ClearingHouseMessage' ...");
        ResponseMessage responseMessage =new ResponseMessage(headerMessage, payload);
        LOGGER.info("Setting up 'ClearingHouseMessage' ... OK");

        LOGGER.info("Executing test ...");
        String expectedString="{\n" +
                "  \"header\" : {\n" +
                "    \"@context\" : {\n" +
                "      \"ids\" : \"https://w3id.org/idsa/core/\",\n" +
                "      \"idsc\" : \"https://w3id.org/idsa/code/\"\n" +
                "    },\n" +
                "    \"@type\" : \"ids:MessageProcessedNotificationMessage\",\n" +
                "    \"@id\" : \"https://message/id/01\",\n" +
                "    \"ids:issued\" : {\n" +
                "      \"@value\" : \""+headerMessage.getIssued()+"\",\n" +
                "      \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\n" +
                "    },\n" +
                "    \"ids:modelVersion\" : \"4.1.0\",\n" +
                "    \"ids:correlationMessage\" : {\n" +
                "      \"@id\" : \"https://correlationMessage/logMessage/id/01\"\n" +
                "    },\n" +
                "    \"ids:issuerConnector\" : {\n" +
                "      \"@id\" : \"https://issuerConnector/id/clearing-house\"\n" +
                "    },\n" +
                "    \"ids:senderAgent\" : {\n" +
                "      \"@id\" : \"https://senderAgent/id/clearing-house\"\n" +
                "    },\n" +
                "    \"ids:securityToken\" : {\n" +
                "      \"@type\" : \"ids:DynamicAttributeToken\",\n" +
                "      \"@id\" : \"https://securityToken/dat/id/01\",\n" +
                "      \"ids:tokenValue\" : \"eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ..-GzjRQ\",\n" +
                "      \"ids:tokenFormat\" : {\n" +
                "        \"@id\" : \"https://w3id.org/idsa/code/JWT\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"payload\" : \"payload_token\"\n" +
                "}";

        String jsonResponse=responseMessage.toJson();
        System.out.println(jsonResponse);
        JsonElement jsonResponse_parsed = JsonParser.parseString(jsonResponse);
        JsonElement expectedString_parsed = JsonParser.parseString(expectedString);
        assertEquals(jsonResponse_parsed, expectedString_parsed);
        LOGGER.info("Executing test ... OK");
    }
}