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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Expose;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.util.IdsMessageUtils;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.springframework.data.annotation.Id;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;

import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import pt.inesctec.ids.clearinghouse.util.MongoUtil;
import pt.inesctec.ids.clearinghouse.util.MyJacksonObjectMapper;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

/**
 * Class responsible for storing the Clearing House messages from collections 'Messages' and 'Contracts'
 * */
@Getter
@Setter
@Document
public class ClearingHouseMessage extends DBAbstract implements Serializable {

    @BsonProperty @Expose @JsonProperty
    private String payload;

    public ClearingHouseMessage(String agreementID, String header, String payload) {
        super(agreementID, header);
        this.payload = payload;
    }

    public String toJson(){
        try {
            String json=this.serialize(this);
            return json;
        } catch (IOException e) {
            throw new InvalidValueException("Clearing House Message", "Exception: "+e.getMessage());
        }
    }

    @Override
    public  Map<String, Object>  toMap(){
        /*Map<String, Object> map=new HashMap<>();
        map.put("_id", this._id);
        map.put(HEADER, this.getHeader());
        map.put(PAYLOAD, this.payload);
        map.put("agreementID", this.getAgreementID());
        map.put("timestamp", this.getTimestamp());*/
        MyJacksonObjectMapper myObjectMapper=new MyJacksonObjectMapper();
        Map<String, Object> map = myObjectMapper
                .convertValue(this, new TypeReference<>() {});
        return map;
    }

    @Override
    public String toHashString() {
        String json=this.toMap().toString();
        return json;
    }


    @Override
    public String toString() {
        return "ClearingHouseMessage{" +
                super.toString()+
                ", payload='" + payload + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClearingHouseMessage that)) return false;
        if (!super.equals(o)) return false;
        return getPayload().equals(that.getPayload()) && Objects.equals(getHash(), that.getHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getPayload(), getHash());
    }

    public String getPayload(){
        return this.payload;
    }
}

