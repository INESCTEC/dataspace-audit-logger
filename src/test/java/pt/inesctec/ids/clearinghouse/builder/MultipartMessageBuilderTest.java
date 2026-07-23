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
package pt.inesctec.ids.clearinghouse.builder;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import ids.messaging.util.IdsMessageUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.inesctec.ids.clearinghouse.converter.ClearingHouseMessageConverter;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

class MultipartMessageBuilderTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultipartMessageBuilderTest.class);

    private HttpEntity expectedHttpEntity;
    private Message header;
    private String payload;
    private ClearingHouseMessageConverter clearingHouseMessageConverter;

    private void prepareTest(boolean hasPayload) {
        clearingHouseMessageConverter =new ClearingHouseMessageConverter();
        // Prepare header and Payload
        LOGGER.info("Setting up 'header' and 'payload' ... ");
        header=new MessageProcessedNotificationMessageBuilder(URI.create("https://message/id/01"))
                ._securityToken_(new DynamicAttributeTokenBuilder(URI.create("https://securityToken/dat/id/01"))
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_("eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ..-GzjRQ")
                        .build())
                ._correlationMessage_(URI.create("https://correlationMessage/logMessage/id/01"))
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(URI.create("https://issuerConnector/id/clearing-house"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/clearing-house"))
                .build();
        payload="payload";
        LOGGER.info("Setting up 'header' and 'payload' ... OK");

        LOGGER.info("Setting up 'builder' ...");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        // Set builder values
        builder.setMode(HttpMultipartMode.STRICT);
        builder.setBoundary(UUID.randomUUID().toString());
        builder.setCharset(StandardCharsets.UTF_8);
        builder.setContentType(ContentType.APPLICATION_JSON);

        // Add header
        final StringBody headerAsStringBody = new StringBody(header.toRdf(), ContentType.MULTIPART_FORM_DATA);

        builder.addPart(HEADER, headerAsStringBody);

        LOGGER.info("Setting up 'builder' ... OK");

        // If it is true, adds a Payload to the HttpEntity
        if(hasPayload){
            final StringBody payloadAsStringBody = new StringBody(payload, ContentType.MULTIPART_FORM_DATA);

            LOGGER.info("Adding 'payload' to builder ...");
            builder.addPart(PAYLOAD, payloadAsStringBody);
            LOGGER.info("Adding 'payload' to builder ... OK");
        }

        LOGGER.info("Setting up 'HttpEntity' ...");
        expectedHttpEntity=builder.build();
        LOGGER.info("Setting up 'HttpEntity' ... OK");
    }

    @Test
    void buildMessage_HeaderAndPayload() throws IOException {
        prepareTest(true);
        HttpEntity httpEntity=MultipartMessageBuilder.buildMessage(header, payload);
        assertNotNull(httpEntity);
        assertEquals(clearingHouseMessageConverter.convert(httpEntity), clearingHouseMessageConverter.convert(expectedHttpEntity));
    }

    @Test
    void buildMessage_Header() throws IOException {
        prepareTest(false);

        HttpEntity httpEntity=MultipartMessageBuilder.buildMessage(header);

        System.out.println(EntityUtils.toString(httpEntity));
        assertNotNull(httpEntity);
        assertEquals(clearingHouseMessageConverter.convert(httpEntity),
                clearingHouseMessageConverter.convert(expectedHttpEntity));
    }
}