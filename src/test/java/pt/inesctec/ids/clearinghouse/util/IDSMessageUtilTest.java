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
package pt.inesctec.ids.clearinghouse.util;

import de.fraunhofer.iais.eis.Message;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.MultipartMessageBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

class IDSMessageUtilTest {

    private TestUtil testUtil;
    private Map<String, Object> multipartMap;
    private Message headerMessage;
    private String payload;

    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();
        headerMessage=testUtil.headerMessage();
        payload= testUtil.payload();

        multipartMap =new HashMap<>();
        multipartMap.put(HEADER, headerMessage);
        multipartMap.put(PAYLOAD, payload);
    }

    @Test
    void extractHeaderFromMultipartMessage() {

        assertEquals(IDSMessageUtil.extractHeaderFromMultipartMessage(multipartMap), multipartMap.get(HEADER).toString());
    }

    @Test
    void extractPayloadFromMultipartMessage() {
        assertEquals(IDSMessageUtil.extractPayloadFromMultipartMessage(multipartMap), multipartMap.get(PAYLOAD).toString());

    }

    @Test
    void buildByteResponse() {
        // Prepare test
        HttpEntity entity=MultipartMessageBuilder.buildMessage(headerMessage, payload);

        byte[] response=IDSMessageUtil.buildByteResponse(entity);
        System.err.println(response.toString());
        System.err.println(IOUtils.toString(response));
        assertThat(IOUtils.toString(response)).isNotNull();

    }
}