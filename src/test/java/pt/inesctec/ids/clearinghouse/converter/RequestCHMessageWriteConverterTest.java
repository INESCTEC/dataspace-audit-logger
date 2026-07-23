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
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

class RequestCHMessageWriteConverterTest {

    TestUtil testUtil=new TestUtil();
    RequestCHMessageWriteConverter converter=new RequestCHMessageWriteConverter();

    @Test
    void convert() {

        // Initialize ClearingHouseMessage
        ClearingHouseMessage clearingHouseMessage=new ClearingHouseMessageBuilder()
                .payload(testUtil.artefactRequestMessage().toRdf())
                .header(testUtil.logMessage().toRdf())
                .agreementID("1235-7896-294")
                .build();
        clearingHouseMessage.setTimestamp(testUtil.getDate());
        clearingHouseMessage.set_id("123456");
        System.out.println(clearingHouseMessage.toJson());


        // Converts RequestClearingHouseMessage into document
        Document document=converter.convert(clearingHouseMessage);
        System.out.println(document.toJson());

        // Assert that all values are correct
        assertThat(clearingHouseMessage.get_id()).isEqualTo(document.get("_id"));
        assertThat(clearingHouseMessage.getHeader()).isEqualTo(document.get(HEADER));
        assertThat(clearingHouseMessage.getPayload()).isEqualTo(document.get(PAYLOAD));
        assertThat(clearingHouseMessage.getTimestamp()).isEqualTo(new Date(document.get("timestamp").toString()));
        assertThat(clearingHouseMessage.getAgreementID()).isEqualTo(document.get("agreementID"));
        assertThat(clearingHouseMessage.getHash()).isEqualTo(document.get("hash"));


    }

    @Test
    void convert_WithHash() {

        // Initialize RequestClearingHouseMessage
        ClearingHouseMessage clearingHouseMessage=new ClearingHouseMessageBuilder()
                .payload(testUtil.artefactRequestMessage().toRdf())
                .header(testUtil.logMessage().toRdf())
                .agreementID("1235-7896-294")
                .build();
        clearingHouseMessage.setTimestamp( testUtil.getDate());
        clearingHouseMessage.set_id("123456");
        clearingHouseMessage.setHash("hash");
        System.out.println(clearingHouseMessage.toJson());


        // Converts RequestClearingHouseMessage into document
        Document document=converter.convert(clearingHouseMessage);
        System.out.println(document.toJson());

        // Assert that all values are correct
        assertThat(clearingHouseMessage.get_id()).isEqualTo(document.get("_id"));
        assertThat(clearingHouseMessage.getHeader()).isEqualTo(document.get(HEADER));
        assertThat(clearingHouseMessage.getPayload()).isEqualTo(document.get(PAYLOAD));
        assertThat(clearingHouseMessage.getTimestamp()).isEqualTo(new Date(document.get("timestamp").toString()));
        assertThat(clearingHouseMessage.getAgreementID()).isEqualTo(document.get("agreementID"));
        assertThat(clearingHouseMessage.getHash()).isEqualTo(document.get("hash"));


    }
}