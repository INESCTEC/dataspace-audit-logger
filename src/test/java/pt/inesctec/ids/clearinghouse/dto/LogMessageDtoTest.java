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

import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.inesctec.ids.clearinghouse.TestUtil;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.*;

class LogMessageDtoTest {

    TestUtil testUtil=new TestUtil();
    LogMessageDto logMessageDto;

    public Map<String, Object> context;
    public String type;
    public String id;
    public String modelVersion;
    public String issued;
    public String issuerConnector;
    public String senderAgent;
    public String payload;
    public String payload_type;

    @BeforeEach
    void setUp() {
        id="https://id";
        modelVersion="4.0.1";
        issued="https://issued/id/01";
        issuerConnector="https://issuerConnector/id/01";
        senderAgent="https://senderAgent/id/01";
        payload= testUtil.artefactRequestMessage().toRdf();
        payload_type="application/ld+json";

        context=new HashMap<>();
        context.put("ids", IDS);
        context.put("idsc", IDSC);

        type=LOG_MESSAGE;

        logMessageDto=
                new LogMessageDto(id, modelVersion, issued, issuerConnector, senderAgent, payload, payload_type);
    }

    @Test
    void constructor() {
        LogMessageDto logMessageDto=
                new LogMessageDto(id, modelVersion, issued, issuerConnector, senderAgent, payload, payload_type);

        assertThat(logMessageDto.getContext()).isEqualTo(context);
        assertThat(logMessageDto.getType()).isEqualTo(type);
        assertThat(logMessageDto.getId()).isEqualTo(id);
        assertThat(logMessageDto.getModelVersion()).isEqualTo(modelVersion);
        assertThat(logMessageDto.getIssued()).isEqualTo(issued);
        assertThat(logMessageDto.getIssuerConnector()).isEqualTo(issuerConnector);
        assertThat(logMessageDto.getSenderAgent()).isEqualTo(senderAgent);
        assertThat(logMessageDto.getPayload()).isEqualTo(payload);
        assertThat(logMessageDto.getPayload_type()).isEqualTo(payload_type);
    }

    @Test
    void constructorWithLogMessage() throws DatatypeConfigurationException, ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = simpleDateFormat.parse("2024-04-18T12:09:33");
        ZonedDateTime time=ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
        XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory
                .newInstance()
                .newXMLGregorianCalendar(
                        GregorianCalendar
                                .from(time));

        Message logMessage=testUtil.logMessage();
        LogMessageDto logMessageDto=
                new LogMessageDto((LogMessage) logMessage, payload);

        assertThat(logMessageDto.getContext()).isEqualTo(context);
        assertThat(logMessageDto.getType()).isEqualTo(type);
        assertThat(logMessageDto.getId()).isEqualTo("https://message/id/01");
        assertThat(logMessageDto.getModelVersion()).isEqualTo("4.1.0");
        assertThat(logMessageDto.getIssued()).isEqualTo(xmlGregorianCalendar.toString());
        assertThat(logMessageDto.getIssuerConnector()).isEqualTo("https://issuerConnector/id/connector-01");
        assertThat(logMessageDto.getSenderAgent()).isEqualTo("https://senderAgent/id/connector-01");
        assertThat(logMessageDto.getPayload()).isEqualTo(payload);
        assertThat(logMessageDto.getPayload_type()).isEqualTo(payload_type);
    }

    @Test
    void toJson() {
        System.out.println(logMessageDto.toJson());

    }

    @Test
    void getContext() {
        assertThat(logMessageDto.getContext()).isEqualTo(context);
    }

    @Test
    void getType() {
        assertThat(logMessageDto.getType()).isEqualTo(type);

    }

    @Test
    void getId() {
        assertThat(logMessageDto.getId()).isEqualTo(id);

    }

    @Test
    void getModelVersion() {
        assertThat(logMessageDto.getModelVersion()).isEqualTo(modelVersion);

    }

    @Test
    void getIssued() {
        assertThat(logMessageDto.getIssued()).isEqualTo(issued);

    }

    @Test
    void getIssuerConnector() {
        assertThat(logMessageDto.getIssuerConnector()).isEqualTo(issuerConnector);

    }

    @Test
    void getSenderAgent() {
        assertThat(logMessageDto.getSenderAgent()).isEqualTo(senderAgent);

    }

    @Test
    void getPayload() {
        assertThat(logMessageDto.getPayload()).isEqualTo(payload);

    }

    @Test
    void getPayload_type() {
        assertThat(logMessageDto.getPayload_type()).isEqualTo(payload_type);

    }
}