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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.TransactionBuilder;
import pt.inesctec.ids.clearinghouse.service.SignService;

import java.security.PrivateKey;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class TransactionTest {

    @Autowired private PrivateKey privateKey;
    @Autowired private SignService signService;
    private Transaction transaction;
    private TestUtil testUtil;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();
        transaction=new TransactionBuilder()
                .id("12346")
                .header("header")
                .payload("payload")
                .timestamp(testUtil.getDate())
                .client_id("BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69")
                .agreementID("12348-88965-25")
                .build();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void sign() throws JsonMappingException, JsonProcessingException {
        // Get decoded expected result
        String expectedReceiptDecoded="{\n" +
                "  \"id\" : \"12346\",\n" +
                "  \"header\" : \"header\",\n" +
                "  \"payload\" : \"payload\",\n" +
                "  \"agreementID\" : \"12348-88965-25\",\n" +
                "  \"timestamp\" : 1710332715000,\n" +
                "  \"client_id\" : \"BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69\"\n" +
                "}";


        Receipt receipt=transaction.sign(privateKey);
        assertThat(receipt.getSignedData()).isNotNull();

        // I need to decode to compare
        String decodedString=signService.decode(receipt);

        System.out.println(decodedString);

        // I need to decode the token because it is never the same
        assertThat(mapper.readTree(expectedReceiptDecoded)).isEqualTo(mapper.readTree(decodedString));
    }

    @Test
    void toJson() throws JsonMappingException, JsonProcessingException {
        String expectedJSON="{\n" +
                "  \"id\" : \"12346\",\n" +
                "  \"header\" : \"header\",\n" +
                "  \"payload\" : \"payload\",\n" +
                "  \"agreementID\" : \"12348-88965-25\",\n" +
                "  \"timestamp\" : 1710332715000,\n" +
                "  \"client_id\" : \"BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69\"\n" +
                "}";

        assertThat(mapper.readTree(expectedJSON)).isEqualTo(mapper.readTree(transaction.toJson()));
    }

    @Test
    void toDecodedString() throws JsonMappingException, JsonProcessingException {
        String expectedJSON="{\n" +
                "  \"id\" : \"12346\",\n" +
                "  \"header\" : \"header\",\n" +
                "  \"payload\" : \"payload\",\n" +
                "  \"agreementID\" : \"12348-88965-25\",\n" +
                "  \"timestamp\" : 1710332715000,\n" +
                "  \"client_id\" : \"BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69\"\n" +
                "}";

        assertThat(mapper.readTree(expectedJSON)).isEqualTo(mapper.readTree(transaction.toDecodedString()));
    }
}