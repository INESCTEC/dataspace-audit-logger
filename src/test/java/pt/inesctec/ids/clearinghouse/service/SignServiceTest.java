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
package pt.inesctec.ids.clearinghouse.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.builder.TransactionBuilder;
import pt.inesctec.ids.clearinghouse.dto.Transaction;
import pt.inesctec.ids.clearinghouse.dto.Receipt;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.util.DapsUtil;

import java.security.*;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class SignServiceTest {

    @Autowired SignService signService;
    @Autowired
    PrivateKey privateKey;
    TestUtil testUtil=new TestUtil();

    @Test
    void sign() {
        // Prepare test
        ClearingHouseMessage message=new ClearingHouseMessageBuilder()
                .agreementID("1123-7589-12354")
                .header(testUtil.headerMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf()).build();
        // ID set by MongoDB
        message.set_id("123456789");
        message.setTimestamp(testUtil.getDate());
        String client_ID=DapsUtil.getClientFromDAT(testUtil.datToken());

        Transaction expectedTransaction=new TransactionBuilder()
                .id(message.get_id())
                .header(message.getHeader())
                .payload(message.getPayload())
                .timestamp(message.getTimestamp())
                .client_id(client_ID)
                .agreementID(message.getAgreementID())
                .build();
        Receipt receipt=signService.sign(message, client_ID);
        System.out.println(receipt);

        // I need to decode the token because it is never the same
        assertThat(signService.decode(receipt)).isEqualTo(expectedTransaction.toDecodedString());
        System.out.println(signService.decode(receipt));
    }

    @Test
    void decode() {
        // Prepare test
        String receiptToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJQUzUxMiJ9.ewogICJpZCIgOiAiMTIzNDU2Nzg5IiwKICAiaGVhZGVyIiA6ICJ7XG4gIFwiQGNvbnRleHRcIiA6IHtcbiAgICBcImlkc1wiIDogXCJodHRwczovL3czaWQub3JnL2lkc2EvY29yZS9cIixcbiAgICBcImlkc2NcIiA6IFwiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvZGUvXCJcbiAgfSxcbiAgXCJAdHlwZVwiIDogXCJpZHM6TWVzc2FnZVByb2Nlc3NlZE5vdGlmaWNhdGlvbk1lc3NhZ2VcIixcbiAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9tZXNzYWdlL2lkLzAxXCIsXG4gIFwiaWRzOmNvcnJlbGF0aW9uTWVzc2FnZVwiIDoge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vY29ycmVsYXRpb25NZXNzYWdlL2xvZ01lc3NhZ2UvaWQvMDFcIlxuICB9LFxuICBcImlkczptb2RlbFZlcnNpb25cIiA6IFwiNC4xLjBcIixcbiAgXCJpZHM6aXNzdWVkXCIgOiB7XG4gICAgXCJAdmFsdWVcIiA6IFwiMjAyNC0wNC0xOFQxMTowOTozMy4wMDBaXCIsXG4gICAgXCJAdHlwZVwiIDogXCJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSNkYXRlVGltZVN0YW1wXCJcbiAgfSxcbiAgXCJpZHM6aXNzdWVyQ29ubmVjdG9yXCIgOiB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9pc3N1ZXJDb25uZWN0b3IvaWQvY2xlYXJpbmctaG91c2VcIlxuICB9LFxuICBcImlkczpzZW5kZXJBZ2VudFwiIDoge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vc2VuZGVyQWdlbnQvaWQvY2xlYXJpbmctaG91c2VcIlxuICB9LFxuICBcImlkczpzZWN1cml0eVRva2VuXCIgOiB7XG4gICAgXCJAdHlwZVwiIDogXCJpZHM6RHluYW1pY0F0dHJpYnV0ZVRva2VuXCIsXG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9zZWN1cml0eVRva2VuL2RhdC9pZC8wMVwiLFxuICAgIFwiaWRzOnRva2VuVmFsdWVcIiA6IFwiZXlKMGVYQWlPaUpoZEN0cWQzUWlMQ0pyYVdRaU9pSTJNR0psWm1ZeFlXUTJOakpsTXpobVpEZzVPVFkyTXpreU9EWmxNMk15TkdVeVlqTTJObVUxTW1ZNE4yUTRPREkxTVdJNE5UUXdPVFpsWmpjNFl6TTVJaXdpWVd4bklqb2lVbE15TlRZaWZRLi4tR3pqUlFcIixcbiAgICBcImlkczp0b2tlbkZvcm1hdFwiIDoge1xuICAgICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvZGUvSldUXCJcbiAgICB9XG4gIH1cbn0iLAogICJwYXlsb2FkIiA6ICJ7XG4gIFwiQGNvbnRleHRcIiA6IHtcbiAgICBcImlkc1wiIDogXCJodHRwczovL3czaWQub3JnL2lkc2EvY29yZS9cIixcbiAgICBcImlkc2NcIiA6IFwiaHR0cHM6Ly93M2lkLm9yZy9pZHNhL2NvZGUvXCJcbiAgfSxcbiAgXCJAdHlwZVwiIDogXCJpZHM6QXJ0aWZhY3RSZXF1ZXN0TWVzc2FnZVwiLFxuICBcIkBpZFwiIDogXCJodHRwczovL21lc3NhZ2UvaWQvMDFcIixcbiAgXCJpZHM6Y29ycmVsYXRpb25NZXNzYWdlXCIgOiB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9jb3JyZWxhdGlvbk1lc3NhZ2UvbG9nTWVzc2FnZS9pZC8wMVwiXG4gIH0sXG4gIFwiaWRzOm1vZGVsVmVyc2lvblwiIDogXCI0LjEuMFwiLFxuICBcImlkczppc3N1ZWRcIiA6IHtcbiAgICBcIkB2YWx1ZVwiIDogXCIyMDI0LTA0LTE4VDExOjA5OjMzLjAwMFpcIixcbiAgICBcIkB0eXBlXCIgOiBcImh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hI2RhdGVUaW1lU3RhbXBcIlxuICB9LFxuICBcImlkczppc3N1ZXJDb25uZWN0b3JcIiA6IHtcbiAgICBcIkBpZFwiIDogXCJodHRwczovL2lzc3VlckNvbm5lY3Rvci9pZC9jb25uZWN0b3ItMDFcIlxuICB9LFxuICBcImlkczpyZWNpcGllbnRDb25uZWN0b3JcIiA6IFsge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi9sb2NhbGhvc3Q6ODA4MC9jb25uZWN0b3ItMDEvYXBpL2lkcy9kYXRhXCJcbiAgfSBdLFxuICBcImlkczpzZW5kZXJBZ2VudFwiIDoge1xuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vc2VuZGVyQWdlbnQvaWQvY29ubmVjdG9yLTAxXCJcbiAgfSxcbiAgXCJpZHM6c2VjdXJpdHlUb2tlblwiIDoge1xuICAgIFwiQHR5cGVcIiA6IFwiaWRzOkR5bmFtaWNBdHRyaWJ1dGVUb2tlblwiLFxuICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vc2VjdXJpdHlUb2tlbi9kYXQvaWQvMDFcIixcbiAgICBcImlkczp0b2tlblZhbHVlXCIgOiBcImV5SjBlWEFpT2lKaGRDdHFkM1FpTENKcmFXUWlPaUkyTUdKbFptWXhZV1EyTmpKbE16aG1aRGc1T1RZMk16a3lPRFpsTTJNeU5HVXlZak0yTm1VMU1tWTROMlE0T0RJMU1XSTROVFF3T1RabFpqYzRZek01SWl3aVlXeG5Jam9pVWxNeU5UWWlmUS4uLUd6alJRXCIsXG4gICAgXCJpZHM6dG9rZW5Gb3JtYXRcIiA6IHtcbiAgICAgIFwiQGlkXCIgOiBcImh0dHBzOi8vdzNpZC5vcmcvaWRzYS9jb2RlL0pXVFwiXG4gICAgfVxuICB9LFxuICBcImlkczp0cmFuc2ZlckNvbnRyYWN0XCIgOiB7XG4gICAgXCJAaWRcIiA6IFwiaHR0cHM6Ly9sb2NhbGhvc3Q6ODA4MC9jb25uZWN0b3ItMDEvYXBpL2FncmVlbWVudHMvNTAwNjE3Y2MtMjVkYi00NjcyLThiMWEtMjc4MWFjNDk4YmMwXCJcbiAgfSxcbiAgXCJpZHM6cmVxdWVzdGVkQXJ0aWZhY3RcIiA6IHtcbiAgICBcIkBpZFwiIDogXCJodHRwczovL2xvY2FsaG9zdDo4MDgwL2Nvbm5lY3Rvci0wMS9hcGkvYXJ0aWZhY3RzLzUwMDYxN2NjLTI1ZGItNDY3Mi04YjFhLTI3ODFhYzQ5OGJjMFwiXG4gIH1cbn0iLAogICJhZ3JlZW1lbnRJRCIgOiAiMTEyMy03NTg5LTEyMzU0IiwKICAidGltZXN0YW1wIiA6IDE3MTM4MDQ1OTQ3NDksCiAgImNsaWVudF9pZCIgOiAiQkI6QTU6Qjk6MDQ6MjU6MDA6MUM6NTY6MUI6MTg6MDc6NUE6MkU6RUU6MUU6MzQ6Njg6QkU6Q0U6RDM6a2V5aWQ6Mjc6QTg6RDY6Njk6RTY6MjU6NDc6QkE6MDk6OEQ6OTg6RTU6REY6Nzk6M0Y6MDk6ODk6RjQ6NEQ6ODMiCn0.sSA0vOjeB7ShXjD3GaHwd2PbUDm3Iu0ZKErXzuOe1lpp567RUFRM5fHRT28rid-neJSoLoYcHNJxOIEN-MUO5KRm4WW8wcZEWqnV6v55GRTcsWb-9hzD15plfddCLYROfKL0tcw0VhyxEI7gE1xSt1OCdUoSCUQ6agiW9W-wT7t4UhZ-7RZLFbzFEzKcsCbDGccbWEoe7WHJtR9xJbWEO9y8CMfUt6p6Kx_Jgv9O_BO4W5oc7Plc-QyiHg7iYfZ4avOmORh-EcBr7-mJXDV_0A29cDEHWvSC4NcrXE1MBam-gNh0ru1tFpyQ3MzdQOif03flJQJS7HK-lz2Sk9KwotfX8zzWc5OmpPZDxrPNtnNYVtR9skWLomWHsKkVDFCrPl03P-gCm_H1CO_w8mEbHeKXobmnP2U6nNsD2LWqwJUp-QGRBcFEwkxn6xFBlhP0Ho0cNYj8o7FxQlkoWInSROJ5_jTpMlBpg3ElTbQzRJ0uz_ktef1VrSgf5a_LnNlfWdRVF9LktztARTngyciTkut38-HjGTH-zOPBhm3nPpyLsH8UhW8GPIt7L-iqmmxcfDRJP-Li8oSeQgx-H_BEIcTeo4NKh1xV0Jay3YEHZK8rOkp22IQRK_1sn_R8yoiBA_nZCfiFQnwJM2S_QdODsbjs-eWY1AOiDPG3cUrmLCo";
        Receipt receipt=new Receipt(receiptToken);

        String expectedResult=signService.decode(receipt);

        String expectedString="{\n" +
                "  \"id\" : \"123456789\",\n" +
                "  \"header\" : \"{\\n  \\\"@context\\\" : {\\n    \\\"ids\\\" : \\\"https://w3id.org/idsa/core/\\\",\\n    \\\"idsc\\\" : \\\"https://w3id.org/idsa/code/\\\"\\n  },\\n  \\\"@type\\\" : \\\"ids:MessageProcessedNotificationMessage\\\",\\n  \\\"@id\\\" : \\\"https://message/id/01\\\",\\n  \\\"ids:correlationMessage\\\" : {\\n    \\\"@id\\\" : \\\"https://correlationMessage/logMessage/id/01\\\"\\n  },\\n  \\\"ids:modelVersion\\\" : \\\"4.1.0\\\",\\n  \\\"ids:issued\\\" : {\\n    \\\"@value\\\" : \\\"2024-04-18T11:09:33.000Z\\\",\\n    \\\"@type\\\" : \\\"http://www.w3.org/2001/XMLSchema#dateTimeStamp\\\"\\n  },\\n  \\\"ids:issuerConnector\\\" : {\\n    \\\"@id\\\" : \\\"https://issuerConnector/id/clearing-house\\\"\\n  },\\n  \\\"ids:senderAgent\\\" : {\\n    \\\"@id\\\" : \\\"https://senderAgent/id/clearing-house\\\"\\n  },\\n  \\\"ids:securityToken\\\" : {\\n    \\\"@type\\\" : \\\"ids:DynamicAttributeToken\\\",\\n    \\\"@id\\\" : \\\"https://securityToken/dat/id/01\\\",\\n    \\\"ids:tokenValue\\\" : \\\"eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ..-GzjRQ\\\",\\n    \\\"ids:tokenFormat\\\" : {\\n      \\\"@id\\\" : \\\"https://w3id.org/idsa/code/JWT\\\"\\n    }\\n  }\\n}\",\n" +
                "  \"payload\" : \"{\\n  \\\"@context\\\" : {\\n    \\\"ids\\\" : \\\"https://w3id.org/idsa/core/\\\",\\n    \\\"idsc\\\" : \\\"https://w3id.org/idsa/code/\\\"\\n  },\\n  \\\"@type\\\" : \\\"ids:ArtifactRequestMessage\\\",\\n  \\\"@id\\\" : \\\"https://message/id/01\\\",\\n  \\\"ids:correlationMessage\\\" : {\\n    \\\"@id\\\" : \\\"https://correlationMessage/logMessage/id/01\\\"\\n  },\\n  \\\"ids:modelVersion\\\" : \\\"4.1.0\\\",\\n  \\\"ids:issued\\\" : {\\n    \\\"@value\\\" : \\\"2024-04-18T11:09:33.000Z\\\",\\n    \\\"@type\\\" : \\\"http://www.w3.org/2001/XMLSchema#dateTimeStamp\\\"\\n  },\\n  \\\"ids:issuerConnector\\\" : {\\n    \\\"@id\\\" : \\\"https://issuerConnector/id/connector-01\\\"\\n  },\\n  \\\"ids:recipientConnector\\\" : [ {\\n    \\\"@id\\\" : \\\"https:/localhost:8080/connector-01/api/ids/data\\\"\\n  } ],\\n  \\\"ids:senderAgent\\\" : {\\n    \\\"@id\\\" : \\\"https://senderAgent/id/connector-01\\\"\\n  },\\n  \\\"ids:securityToken\\\" : {\\n    \\\"@type\\\" : \\\"ids:DynamicAttributeToken\\\",\\n    \\\"@id\\\" : \\\"https://securityToken/dat/id/01\\\",\\n    \\\"ids:tokenValue\\\" : \\\"eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ..-GzjRQ\\\",\\n    \\\"ids:tokenFormat\\\" : {\\n      \\\"@id\\\" : \\\"https://w3id.org/idsa/code/JWT\\\"\\n    }\\n  },\\n  \\\"ids:transferContract\\\" : {\\n    \\\"@id\\\" : \\\"https://localhost:8080/connector-01/api/agreements/500617cc-25db-4672-8b1a-2781ac498bc0\\\"\\n  },\\n  \\\"ids:requestedArtifact\\\" : {\\n    \\\"@id\\\" : \\\"https://localhost:8080/connector-01/api/artifacts/500617cc-25db-4672-8b1a-2781ac498bc0\\\"\\n  }\\n}\",\n" +
                "  \"agreementID\" : \"1123-7589-12354\",\n" +
                "  \"timestamp\" : 1713804594749,\n" +
                "  \"client_id\" : \"BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69:E6:25:47:BA:09:8D:98:E5:DF:79:3F:09:89:F4:4D:83\"\n" +
                "}";
        assertThat(expectedResult).isEqualTo(expectedString);


    }

}
