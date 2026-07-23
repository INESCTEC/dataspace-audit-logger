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
package pt.inesctec.ids.clearinghouse.converter;

import org.apache.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.util.CompareUtil;

import java.util.HashMap;
import java.util.Map;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ResponseMessageConverterTest {

    ClearingHouseMessageConverter clearingHouseMessageConverter;
    TestUtil testUtil;
    ResponseMessage expectedResponseMessage;

    @BeforeEach
    void setUp() {
        clearingHouseMessageConverter =new ClearingHouseMessageConverter();
        testUtil=new TestUtil();

        expectedResponseMessage =new ResponseMessage(testUtil.headerMessage(), testUtil.payload());
        System.err.println("Before convert: "+ expectedResponseMessage.getHeader().getIssued());
    }

    @Test
    void convert()  {
        ResponseMessage responseMessage = clearingHouseMessageConverter.convert(testUtil.httpEntity());
        System.err.println("After convert "+ responseMessage.getHeader().getIssued());

        assertThat(responseMessage).isEqualTo(expectedResponseMessage);
        //assertEquals(clearingHouseMessage, expectedClearingHouseMessage);
    }

    @Test
    void convert2Map() {
        HttpEntity httpEntity=testUtil.httpEntity();
        Map<String, Object> expectedMessageResponse=new HashMap<>();
        expectedMessageResponse.put(HEADER, testUtil.headerMessage());
        expectedMessageResponse.put(PAYLOAD, testUtil.payload());

        Map<String, Object> messageResponse = clearingHouseMessageConverter.convert2Map(httpEntity);
        //System.err.println(expectedMessageResponse.getHeader().getIssued());

        assertThat(CompareUtil.maps(messageResponse, expectedMessageResponse)).isTrue();

    }



}