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

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.util.MongoUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class RequestCHMessageReadConverterTest {

    TestUtil testUtil=new TestUtil();
    RequestCHMessageReadConverter converter=new RequestCHMessageReadConverter();

    @Test
    void convert() {

        // Create a MAP to initialize the document
        Map<String, Object> map=new HashMap<>();
        map.put("_id", "123456");
        map.put(HEADER, testUtil.logMessage().toRdf());
        map.put(PAYLOAD, testUtil.artefactRequestMessage().toRdf());
        map.put("agreementID", "1235-7896-294");
        map.put("timestamp", testUtil.getDate());
        Document document=new Document(map);

        System.out.println(document.toJson());

        // Converts document into RequestClearingHouseMessage
        ClearingHouseMessage convertedMessage=converter.convert(document);
        System.out.println(convertedMessage.toJson());

        // Assert that all values are correct
        assertThat(convertedMessage.get_id()).isEqualTo(document.get("_id"));
        assertThat(convertedMessage.getHeader()).isEqualTo(document.get(HEADER));
        assertThat(convertedMessage.getPayload()).isEqualTo(document.get(PAYLOAD));
        assertThat(convertedMessage.getTimestamp()).isEqualTo(new Date(document.get("timestamp").toString()));
        assertThat(convertedMessage.getAgreementID()).isEqualTo(document.get("agreementID"));
        assertThat(convertedMessage.getHash()).isEqualTo(document.get("hash"));

    }

    @Test
    void convert_HashIsNull() {

        // Create a MAP to initialize the document
        Map<String, Object> map=new HashMap<>();
        map.put("_id", "123456");
        map.put(HEADER, testUtil.logMessage().toRdf());
        map.put(PAYLOAD, testUtil.artefactRequestMessage().toRdf());
        map.put("agreementID", "1235-7896-294");
        map.put("timestamp", testUtil.getDate());
        map.put("hash", MongoUtil.hash("test"));
        Document document=new Document(map);

        System.out.println(document.toJson());

        // Converts document into RequestClearingHouseMessage
        ClearingHouseMessage convertedMessage=converter.convert(document);
        System.out.println(convertedMessage.toJson());

        // Assert that all values are correct
        assertThat(convertedMessage.get_id()).isEqualTo(document.get("_id"));
        assertThat(convertedMessage.getHeader()).isEqualTo(document.get(HEADER));
        assertThat(convertedMessage.getPayload()).isEqualTo(document.get(PAYLOAD));
        assertThat(convertedMessage.getTimestamp()).isEqualTo(new Date(document.get("timestamp").toString()));
        assertThat(convertedMessage.getAgreementID()).isEqualTo(document.get("agreementID"));
        assertThat(convertedMessage.getHash()).isEqualTo(document.get("hash"));

    }
}