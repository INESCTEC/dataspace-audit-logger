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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.inesctec.ids.clearinghouse.builder.TransactionBuilder;
import pt.inesctec.ids.clearinghouse.dto.Transaction;
import pt.inesctec.ids.clearinghouse.dto.Receipt;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;


import java.security.*;
import java.util.Base64;


@Service
public class SignService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignService.class);

    @Autowired PrivateKey privateKey;


    Transaction transaction;

    /**
     * Builds a Transaction
     *
     * @param savedMessage message saved on database
     * @param client_ID the clientID that called the CH
     * @return a Transaction
     * */
    private Transaction builtTransaction(ClearingHouseMessage savedMessage, String client_ID){
        this.transaction= new TransactionBuilder()
                .id(savedMessage.get_id())
                .header(savedMessage.getHeader())
                .payload(savedMessage.getPayload())
                .timestamp(savedMessage.getTimestamp())
                .client_id(client_ID)
                .agreementID(savedMessage.getAgreementID())
                .build();
        return this.transaction;
    }


    /**
     * Signs a transaction
     *
     * @param savedMessage message saved on database
     * @param client_ID the clientID that called the CH
     * @return sign receipt
     * */
    public Receipt sign(ClearingHouseMessage savedMessage, String client_ID){

        builtTransaction(savedMessage, client_ID);

        Receipt signedData = this.transaction.sign(privateKey);
        return signedData;
    }

    /**
     * Decoded a signed Receipt
     *
     * @param receipt the signed receipt
     * @return payload of the decoded receipt
     * */
    public String decode(Receipt receipt){

        String[] chunks = receipt.getSignedData().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));
        return payload;
    }


}
