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

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Date;
import java.util.Map;

import static io.jsonwebtoken.SignatureAlgorithm.PS512;

/**
 * Class responsible for building the Receipt to return after creating a Process
 * */
@Getter //@Setter
@ToString
public class Transaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

    @Id
    public String id;

    private String header;
    private String payload;
    private String agreementID;
    private Date timestamp;
    private String client_id;

    public Transaction(String id, String header, String payload, String agreementID, Date timestamp, String client_id) {
        this.id = id;
        this.header = header;
        this.payload = payload;
        this.agreementID = agreementID;
        this.timestamp = timestamp;
        this.client_id = client_id;
    }

    public Receipt sign(PrivateKey privateKey) {
        LOGGER.debug("Signing message to return ...");

        /*Signature signature = Signature.getInstance("SHA256withRSA");
        String signed=IdsMessageUtils.sign(signature, "payload", privateKey);
        System.out.println(signed);*/

        io.jsonwebtoken.Header header=Jwts.header().setType("JWT");
        header.setType("JWT");

        String signedData = Jwts.builder()
                .setHeader((Map<String, Object>) Jwts.header().setType("JWT"))
                .setPayload(this.toJson())
                .signWith(privateKey, PS512)
                .compact();

        LOGGER.debug("Sending signed data ... ");
        LOGGER.debug(signedData);

        LOGGER.debug("Signing message to return ... OK");
        return new Receipt(signedData);
    }


    public String toJson(){
        Serializer serializer=new Serializer();
        try {
            String json=serializer.serialize(this);
            return json;
        }  catch (IOException e) {
            throw new InvalidValueException("Transaction", "Exception: "+e.getMessage());
        }
    }

    public String toDecodedString(){
        Serializer serializer=new Serializer();
        try {
            String json=serializer.serializePlainJson(this);
            System.out.println(json);
            return json;
        }  catch (IOException e) {
            throw new InvalidValueException("Transaction", "Exception: "+e.getMessage());
        }
    }

}
