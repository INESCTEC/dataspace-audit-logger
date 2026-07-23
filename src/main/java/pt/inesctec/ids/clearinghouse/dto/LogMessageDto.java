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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.fraunhofer.iais.eis.LogMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONObject;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;

import javax.naming.Context;
import javax.naming.NamingException;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.*;

@Getter @Setter @ToString
@JsonPropertyOrder({"context", "type", "id","modelVersion", "issued", "issuerConnector","senderAgent", "payload", "payload_type"})
@NoArgsConstructor
public class LogMessageDto {
    @JsonProperty("@context")
    private Map<String, Object> context;

    @JsonProperty("@type")
    private String type;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("ids:modelVersion")
    private String modelVersion;

    @JsonProperty("ids:issued")
    private String issued;

    @JsonProperty("ids:issuerConnector")
    private String issuerConnector;

    @JsonProperty("ids:senderAgent")
    private String senderAgent;

    private String payload;

    private String payload_type;

    public LogMessageDto(String id, String modelVersion, String issued, String issuerConnector, String senderAgent, String payload, String payload_type) {
        addContext();
        addType();
        this.id = id;
        this.modelVersion = modelVersion;
        this.issued = issued;
        this.issuerConnector = issuerConnector;
        this.senderAgent = senderAgent;
        this.payload = payload;
        this.payload_type = payload_type;
    }

    public LogMessageDto(LogMessage logMessage, String payload) {
        JSONObject jsonObject = new JSONObject(logMessage.toRdf());
        JSONObject contextJson = jsonObject.optJSONObject("@context");
        //System.out.println("contextJson" + contextJson.toString(2));

        addContext();
        addType();
        this.id = logMessage.getId().toString();
        this.modelVersion = logMessage.getModelVersion();
        this.issued = logMessage.getIssued().toString();
        this.issuerConnector = logMessage.getIssuerConnector().toString();
        this.senderAgent = logMessage.getSenderAgent().toString();
        this.payload = payload;
        this.payload_type="application/ld+json";

    }

    private void addContext(){
        this.context=new HashMap<>();
        this.context.put("ids", IDS);
        this.context.put("idsc", IDSC);
    }

    private void addType(){
        this.type=LOG_MESSAGE;
    }

    public String toJson(){
        Serializer serializer=new Serializer();
        try {
            String json=serializer.serialize(this);
            return json;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
