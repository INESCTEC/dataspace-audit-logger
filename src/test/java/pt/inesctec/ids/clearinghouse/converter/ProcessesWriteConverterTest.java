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
import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;
import pt.inesctec.ids.clearinghouse.model.Processes;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

class ProcessesWriteConverterTest {

    TestUtil testUtil=new TestUtil();
    ProcessesWriteConverter converter=new ProcessesWriteConverter();

    @Test
    void convert() {
        // Initialize Processes
        Processes process=new ProcessesBuilder()
                .owners(testUtil.owners_List())
                .header(testUtil.requestMessage().toRdf())
                .agreementID("1235-7896-294")
                .build();
        process.setTimestamp( testUtil.getDate());
        process.set_id("123456");
        System.out.println(process.toJson());


        // Converts RequestClearingHouseMessage into document
        Document document=converter.convert(process);
        System.out.println(document.toJson());

        // Assert that all values are correct
        assertThat(document.get("_id")).isEqualTo(process.get_id());
        assertThat(document.get(HEADER)).isEqualTo(process.getHeader());
        assertThat(document.get("owners")).isEqualTo(process.getOwners());
        assertThat(document.get("timestamp")).isEqualTo(process.getTimestamp());
        assertThat(document.get("agreementID")).isEqualTo(process.getAgreementID());
    }
}