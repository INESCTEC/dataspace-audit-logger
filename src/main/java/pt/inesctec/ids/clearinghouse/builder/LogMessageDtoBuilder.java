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

import pt.inesctec.ids.clearinghouse.dto.LogMessageDto;

import javax.naming.Context;
import javax.xml.datatype.XMLGregorianCalendar;

public class LogMessageDtoBuilder {
    private String id;
    private String modelVersion;
    private String issued;
    private String issuerConnector;
    private String senderAgent;
    private String payload;
    private String payloadType;

    public LogMessageDtoBuilder id(String id) {
        this.id = id;
        return this;
    }

    public LogMessageDtoBuilder modelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public LogMessageDtoBuilder issued(String issued) {
        this.issued = issued;
        return this;
    }

    public LogMessageDtoBuilder issued(XMLGregorianCalendar issued) {
        this.issued = String.valueOf(issued);
        return this;
    }

    public LogMessageDtoBuilder issuerConnector(String issuerConnector) {
        this.issuerConnector = issuerConnector;
        return this;
    }

    public LogMessageDtoBuilder senderAgent(String senderAgent) {
        this.senderAgent = senderAgent;
        return this;
    }

    public LogMessageDtoBuilder payload(String payload) {
        this.payload = payload;
        return this;
    }

    public LogMessageDtoBuilder payload_type(String payloadType) {
        this.payloadType = payloadType;
        return this;
    }

    public LogMessageDto build() {
        return new LogMessageDto( id, modelVersion, issued, issuerConnector, senderAgent, payload, payloadType);
    }
}