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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.annotations.Expose;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;


import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import java.util.Map;
import java.util.Objects;

/**
 * Abstract Class
 * */
@Getter
@Setter
public abstract class DBAbstract extends Serializer implements Serializable{
    private static final Logger LOGGER = LoggerFactory.getLogger(DBAbstract.class);

    @Id
    @BsonProperty(value = "_id")
    public String _id;

    @BsonProperty @Expose @JsonProperty // Index is added programatically since otherwise it is only created on the Process (because the Clearing House message is used to create 2 collections)
    private String agreementID;

    @BsonProperty @Expose @JsonProperty
    private Date timestamp;

    @BsonProperty @Expose @JsonProperty
    private String header;

    // Hash value of the previous log entry, chaining together all log entries.
    // Any change to a previous log entry would require rehashing all following log entries.
    @BsonProperty private String hash;


    public DBAbstract() {
        this.timestamp=new Date();
    }

    public DBAbstract(String agreementID, String header) {
        this.agreementID = agreementID;
        this.timestamp = new Date();
        this.header = header;
    }

    public String toJson(){
        try {
            String json=this.serialize(this);
            return json;
        }  catch ( IOException e) {
            throw new InvalidValueException("DBAbstract", "Exception: "+e.getMessage());
        }
    }

    public abstract Map<String, Object> toMap();

    public String toHashString() {
        /*JSONObject jsonObject = new JSONObject(toMap());
        String orgJsonData = jsonObject.toString(2);*/
        try {
            JsonConverter jsonConverter=new JsonConverter();
            return jsonConverter.object2Json(this).toPrettyString();
        } catch (Exception e) {
            throw new InvalidValueException("DBAbstract", "Exception: "+e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "_id='" + _id + '\'' +
                ", agreementID='" + agreementID + '\'' +
                ", timestamp=" + timestamp +
                ", hash=" + hash +
                ", header='" + header +'\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DBAbstract that)) return false;
        return Objects.equals(get_id(), that.get_id()) && Objects.equals(getAgreementID(), that.getAgreementID()) && Objects.equals(getTimestamp(), that.getTimestamp()) && Objects.equals(getHeader(), that.getHeader()) && Objects.equals(getHash(), that.getHash());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get_id(), getAgreementID(), getTimestamp(), getHeader(), getHash());
    }
}
