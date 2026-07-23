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
package pt.inesctec.ids.clearinghouse.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Expose;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;
import pt.inesctec.ids.clearinghouse.util.MyJacksonObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class responsible for storing the Processes
 * */
@Getter
@Setter
@Document(collection = "process")
public class Processes extends DBAbstract {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processes.class);

    @BsonProperty @Expose @JsonProperty
    private List<String> owners;


    public Processes(String agreementID, String header, List<String> owners) {
        super(agreementID, header);
        this.owners = owners;
    }

    public String toJson(){
        try {
            String json=this.serialize(this);
            return json;
        }  catch (IOException e) {
            throw new InvalidValueException("Process", "Exception: "+e.getMessage());
        }
    }

    @Override
    public Map<String, Object> toMap(){
        MyJacksonObjectMapper myObjectMapper=new MyJacksonObjectMapper();
        Map<String, Object> map = myObjectMapper
                .convertValue(this, new TypeReference<>() {});
        return map;
    }

    @Override
    public String toHashString() {
        /*JSONObject jsonObject = new JSONObject(toMap());
        String orgJsonData = jsonObject.toString(2);*/
        String json=this.toMap().toString();
        return json;
    }

    @Override
    public String toString() {
        return "Processes{" +
                super.toString() +
                ", owners=" + owners +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Processes processes)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getOwners(), processes.getOwners());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOwners());
    }
}
