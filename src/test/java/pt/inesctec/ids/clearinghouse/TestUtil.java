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
package pt.inesctec.ids.clearinghouse;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Component;
import pt.inesctec.ids.clearinghouse.builder.MultipartMessageBuilder;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Component
public class TestUtil {

    XMLGregorianCalendar xmlGregorianCalendar;


    public TestUtil() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        try {
            Date date = simpleDateFormat.parse("2024-04-18T12:09:33");
            ZonedDateTime time=ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
            this.xmlGregorianCalendar = DatatypeFactory
                    .newInstance()
                    .newXMLGregorianCalendar(
                            GregorianCalendar
                                    .from(time));

        } catch (ParseException | DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpEntity httpEntity(){
        return MultipartMessageBuilder.buildMessage(headerMessage(), payload());
    }

    public Message headerMessage()  {
        return new MessageProcessedNotificationMessageBuilder(URI.create("https://message/id/01"))
                ._securityToken_(new DynamicAttributeTokenBuilder(URI.create("https://securityToken/dat/id/01"))
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_("eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ..-GzjRQ")
                        .build())
                ._correlationMessage_(URI.create("https://correlationMessage/logMessage/id/01"))
                ._issued_(xmlGregorianCalendar)
                ._issuerConnector_(URI.create("https://issuerConnector/id/clearing-house"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/clearing-house"))
                .build();
    }

    public String payload(){
        return "payload_token";
    }

    public LogMessage logMessage()  {
        return new LogMessageBuilder(URI.create("https://message/id/01"))
                ._securityToken_(this.datToken())
                ._correlationMessage_(URI.create("https://correlationMessage/logMessage/id/01"))
                ._issued_(xmlGregorianCalendar)
                ._issuerConnector_(URI.create("https://issuerConnector/id/connector-01"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/connector-01"))
                .build();
    }

    public ArtifactRequestMessage artefactRequestMessage()  {
        return new ArtifactRequestMessageBuilder(URI.create("https://message/id/01"))
                ._securityToken_(new DynamicAttributeTokenBuilder(URI.create("https://securityToken/dat/id/01"))
                        ._tokenFormat_(TokenFormat.JWT)
                        ._tokenValue_("eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ..-GzjRQ")
                        .build())
                ._correlationMessage_(URI.create("https://correlationMessage/logMessage/id/01"))
                ._issued_(xmlGregorianCalendar)
                ._issuerConnector_(URI.create("https://issuerConnector/id/connector-01"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/connector-01"))
                ._requestedArtifact_(URI.create("https://localhost:8080/connector-01/api/artifacts/500617cc-25db-4672-8b1a-2781ac498bc0"))
                ._transferContract_(URI.create("https://localhost:8080/connector-01/api/agreements/500617cc-25db-4672-8b1a-2781ac498bc0"))
                ._recipientConnector_(URI.create("https:/localhost:8080/connector-01/api/ids/data"))
                .build();
    }

    public RequestMessage requestMessage()  {
        return new RequestMessageBuilder(URI.create("https://message/id/01"))
                ._securityToken_(this.datToken())
                ._correlationMessage_(URI.create("https://correlationMessage/logMessage/id/01"))
                ._issued_(xmlGregorianCalendar)
                ._issuerConnector_(URI.create("https://issuerConnector/id/connector-01"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/connector-01"))
                .build();
    }

    public String owners(){
        return "{\"owners\":[\"BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69:E6:25:47:BA:09:8D:98:E5:DF:79:3F:09:89:F4:4D:83\",\"6B:E8:9A:00:F2:65:49:2F:82:C3:81:96:75:DB:84:00:3F:1F:AB:42:keyid:27:A8:D6:69:E6:25:47:BA:09:8D:98:E5:DF:79:3F:09:89:F4:4D:83\"]}";
    }

    public  List<String> owners_List(){
        return List.of("BB:A5:B9:04:25:00:1C:56:1B:18:07:5A:2E:EE:1E:34:68:BE:CE:D3:keyid:27:A8:D6:69:E6:25:47:BA:09:8D:98:E5:DF:79:3F:09:89:F4:4D:83",
                "6B:E8:9A:00:F2:65:49:2F:82:C3:81:96:75:DB:84:00:3F:1F:AB:42:keyid:27:A8:D6:69:E6:25:47:BA:09:8D:98:E5:DF:79:3F:09:89:F4:4D:83");
    }

    // Token from Producer localhost
    public DynamicAttributeToken datToken()  {
        return new DynamicAttributeTokenBuilder(URI.create("https://securityToken/dat/id/01"))
                ._tokenFormat_(TokenFormat.JWT)
                ._tokenValue_("eyJ0eXAiOiJhdCtqd3QiLCJraWQiOiI2MGJlZmYxYWQ2NjJlMzhmZDg5OTY2MzkyODZlM2MyNGUyYjM2NmU1MmY4N2Q4ODI1MWI4NTQwOTZlZjc4YzM5IiwiYWxnIjoiUlMyNTYifQ.eyJzY29wZSI6Imlkc2M6SURTX0NPTk5FQ1RPUl9BVFRSSUJVVEVTX0FMTCIsImF1ZCI6WyJpZHNjOklEU19DT05ORUNUT1JTX0FMTCJdLCJpc3MiOiJodHRwczovL3ZjZXNlMTkuaW5lc2N0ZWMucHQvYXV0aCIsInN1YiI6IkJCOkE1OkI5OjA0OjI1OjAwOjFDOjU2OjFCOjE4OjA3OjVBOjJFOkVFOjFFOjM0OjY4OkJFOkNFOkQzOmtleWlkOjI3OkE4OkQ2OjY5OkU2OjI1OjQ3OkJBOjA5OjhEOjk4OkU1OkRGOjc5OjNGOjA5Ojg5OkY0OjREOjgzIiwibmJmIjoxNzEzMTc1NTU5LCJpYXQiOjE3MTMxNzU1NTksImp0aSI6ImUzODljOWZmLWVhZDktNDg4OC05NzNlLWY1MzFmYTVjNjQwNCIsImV4cCI6MTcxMzE3OTE1OSwiY2xpZW50X2lkIjoiQkI6QTU6Qjk6MDQ6MjU6MDA6MUM6NTY6MUI6MTg6MDc6NUE6MkU6RUU6MUU6MzQ6Njg6QkU6Q0U6RDM6a2V5aWQ6Mjc6QTg6RDY6Njk6RTY6MjU6NDc6QkE6MDk6OEQ6OTg6RTU6REY6Nzk6M0Y6MDk6ODk6RjQ6NEQ6ODMiLCJzZWN1cml0eVByb2ZpbGUiOiJpZHNjOkJBU0VfU0VDVVJJVFlfUFJPRklMRSIsInJlZmVycmluZ0Nvbm5lY3RvciI6Imh0dHBzOi8vY29uZmFjdHMuY29tL2lkcy9jb25uZWN0b3IvaWQvcHJvZHVjZXIiLCJAdHlwZSI6ImlkczpEYXRQYXlsb2FkIiwiQGNvbnRleHQiOiJodHRwczovL3czaWQub3JnL2lkc2EvY29udGV4dHMvY29udGV4dC5qc29ubGQiLCJ0cmFuc3BvcnRDZXJ0c1NoYTI1NiI6IjY3NTYxMmIyNmI4YTQyZmEyMjFkYTE2NThmM2Q5Mjg5Y2UzOThmMGM2OGNlMzRhODFjYTYwOWY4MWY3ZGQ1MjAifQ.HlJpQjDlLpN9FmgirmTRuPZtjBObrFXKLWbtVdBmRoDIuC8YZuOHK4j_q80ztk-Jxo2t9rDAACCIt9uNWzKXu3Ajj28YhYY_AWct8iStwW4p09JbjcVhshO_cujDD1Yq987iT-RjbDGMT7YPTYU3Bt1bOuPuAMvEquq0kqf7zgR6ZPwpfb2LUMmvgmcixa7bOLZCIGoEOl8duiKKMXcr_2GJ-Za-eYpyrBpimrSWfwP9eCJiN0dQuVydy4JBIpUcLaY09As6xrFcDu0O5c5n69eeGrLFDrhQ9tVj7Xf-B2NSb-DBH4DjzFya-4WZyEFZX0qtAQcrmudsT7502Vf_EQ")
                .build();
    }

    public QueryMessage queryMessage()  {
        return new QueryMessageBuilder(URI.create("https://query/message/id/01"))
                ._securityToken_(this.datToken())
                ._issued_(xmlGregorianCalendar)
                ._issuerConnector_(URI.create("https://issuerConnector/id/connector-01"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/connector-01"))
                ._recipientAgent_(URI.create("http://localhost:8083/api/ids/data"))
                .build();
    }

    public de.fraunhofer.iais.eis.Connector createConnectorToTest(){
        return new BaseConnectorBuilder(URI.create("https://test-ch.com/id/test/connector-producer"))
                ._hasDefaultEndpoint_(
                        new ConnectorEndpointBuilder(URI.create("https://test-ch.com/id/test/connectorEndpoint/dhiasdcna"))
                                ._accessURL_(URI.create("https://host.docker.internal:8080/test/connector/api/ids/data")).build())
                ._inboundModelVersion_("4.2.6")
                ._outboundModelVersion_("4.2.6")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._maintainer_(URI.create("https://www.inesctec.pt/"))
                ._curator_(URI.create("https://www.inesctec.pt/"))
                ._description_(new TypedLiteral("Description"))
                ._title_(new TypedLiteral("Title"))
                ._publicKey_(new PublicKeyBuilder(URI.create("https://test-ch.com/id/test/publicKey/dhiasdcna"))
                        ._keyValue_("KeyValueExample".getBytes())
                        ._keyType_(KeyType.RSA)
                        .build())
                ._resourceCatalog_(new ResourceCatalogBuilder(URI.create("https://test-ch.com/id/test/catalog/api/connector-producer"))
                        .build())
                .build();
    }

    public ArtifactResponseMessage artefactResponseMessage()  {
        return new ArtifactResponseMessageBuilder(URI.create("https://artifact/response/message/id/01"))
                ._securityToken_(this.datToken())
                ._correlationMessage_(URI.create("https://message/id/01"))
                ._issued_(xmlGregorianCalendar)
                ._issuerConnector_(URI.create("https://issuerConnector/id/connector-02"))
                ._modelVersion_("4.1.0")
                ._senderAgent_(URI.create("https://senderAgent/id/connector-02"))
                ._transferContract_(URI.create("https://localhost:8080/connector-01/api/agreements/500617cc-25db-4672-8b1a-2781ac498bc0"))
                ._recipientConnector_(URI.create("https:/localhost:8080/connector-02/api/ids/data"))
                .build();
    }

    public ContractAgreement contractAgreement()  {
        XMLGregorianCalendar endDate=xmlGregorianCalendar;
        endDate.setYear(endDate.getYear()+5);

        return new ContractAgreementBuilder(URI.create("https://contract/agreement/id/01"))
                ._contractStart_(xmlGregorianCalendar)
                ._contractEnd_(endDate)
                ._contractDate_(xmlGregorianCalendar)
                ._consumer_(URI.create("https://issuerConnector/id/connector-02"))
                ._permission_(new PermissionBuilder()
                        ._assignee_(URI.create("https://issuerConnector/id/connector-02"))
                        ._assigner_(URI.create("https://issuerConnector/id/connector-01"))
                        ._title_(new TypedLiteral("Permission"))
                        ._action_(Action.USE)
                        .build())
                .build();
    }

    public Date getDate(){
        return new GregorianCalendar(2024, Calendar.MARCH, 13, 12, 25, 15).getTime();
    }

    public String getUsageLogging(){
        return "{issuerConnector=https://confacts.com/ids/connector/id/subcontracted, accessed=2024-05-23T15:32:18.073276574Z, target=https://subcontracted-connector:8080/subcontracted/api/artifacts/bf9ee34c-6402-4ae4-962c-9e4877003daa}";
    }

}
