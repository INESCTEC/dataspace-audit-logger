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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.fraunhofer.iais.eis.*;
import ids.messaging.core.daps.DapsTokenProvider;
import lombok.Setter;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.dto.UsageNotificationDto;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.dto.Receipt;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;
import pt.inesctec.ids.clearinghouse.util.DapsUtil;
import pt.inesctec.ids.clearinghouse.util.LogUtil;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.CONTRACTS;

@Service
public class LoggingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingService.class);

    @Setter private String collection;

    @Autowired private MongoService mongoService;
    @Autowired private SignService signService;

    private static boolean hasOwners(String owners){
        List<String> ownersInList=new ArrayList<>();

        // Regular expression to match strings enclosed in double quotes
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(owners);

        // Find all matches and add them to the result list
        while (matcher.find()) {
            ownersInList.add(matcher.group(1));
        }

        // Remove the first record (it is "owners")
        return ownersInList.contains("owners");
    }

    private static boolean checkIfIsLoggingPayload(String payload){
        return payload.contains("issuerConnector") &&
                        payload.contains("accessed") &&
                        payload.contains("target");

    }

    /**
     * Takes the payload of the Controller and determines the collection to store the message
     *
     * @param payload the request payload of the Controller
     * @return collection name
     * */
    public String determineCollectionName(String payload){
        LOGGER.debug("Determining collection ...");
        // The documentation says that only the finalized contract should be stored

        // If it has owners return PROCESS
        if(hasOwners(payload)){
            LOGGER.debug("... is 'Processes'.");
            return PROCESS;
        }

        // If it is a logging notification return LOGGING
        if(checkIfIsLoggingPayload(payload)){
            LOGGER.debug("... is Logging the data usage.");
            return LOGGING;
        }

        // If it is a Artefact Request/Response Message return MESSAGE
        if( MessageUtil.checkDeserialize(payload, ArtifactRequestMessage.class)
                || MessageUtil.checkDeserialize(payload, ArtifactResponseMessage.class)
        ){
            LOGGER.debug("... is 'Messages'.");
            return MESSAGES;
        }

        // If it is a Contract Agreement Message return CONTRACTS
        if( MessageUtil.checkDeserialize(payload, ContractAgreement.class)){
            LOGGER.debug("... is 'Contracts'.");
            return CONTRACTS;
        }

        throw new InvalidValueException("Invalid Message. The Clearing House cannot save the sent message type.");
    }

    /**
     * Handles the message and build the ResponseMessage
     *
     * @param messageHeader request header, i.e., RequestMessage or LogMessage
     * @param payload request payload, i.e., list of owners or ids message
     * @param id agreementID
     * @param connector CH connector
     * @param provider CH DapsTokenProvider
     * @return a response message
     * */
    @Transactional
    public ResponseMessage handleMessage(Message messageHeader, String payload, UUID id, Connector connector, DapsTokenProvider provider){
        this.collection=determineCollectionName(payload);

        Message headerToReturn=MessageUtil.buildReturnHeader(messageHeader, connector, provider);
        String payloadToReturn=null;

        switch (this.collection){
            case PROCESS -> {
                saveOnDatabase_Process(messageHeader, payload, id);
                payloadToReturn=id.toString();
            }
            case MESSAGES, CONTRACTS, LOGGING -> {
                ClearingHouseMessage savedMessage=saveOnDatabase_ClearingHouseMessage(messageHeader, payload, id);
                payloadToReturn=signLog(savedMessage, messageHeader);
            }
        }

        // Create MessageResponse
        return MessageUtil.createResponseMessage(headerToReturn, payloadToReturn);
    }

    protected String signLog(ClearingHouseMessage savedMessage, Message messageWithClientID){
        // Generates the signed receipt
        Receipt signedReceipt=signService.sign(savedMessage, DapsUtil.getClientFromDAT(messageWithClientID.getSecurityToken()));

        // Build payload to return
        return LogUtil.buildLogPayload(signedReceipt.getSignedData());
    }


    private ClearingHouseMessage saveOnDatabase_ClearingHouseMessage(Message messageHeader, String payload, UUID id) {
        LOGGER.debug("Saving message into collection '"+ collection + "' ...");

        ClearingHouseMessage messageToSave= new ClearingHouseMessageBuilder()
                .header(messageHeader.toRdf())
                .payload(payload)
                .agreementID(id.toString())
                .build();
        ClearingHouseMessage savedMessage= (ClearingHouseMessage) mongoService.insert(messageToSave, collection);
        LOGGER.debug("Saving message into collection '"+ collection + "' ... OK");
        return savedMessage;
    }

    private Processes saveOnDatabase_Process(Message messageHeader, String payload, UUID id) {
        LOGGER.debug("Saving message into collection '"+ collection + "' ...");

        Processes messageToSave= new ProcessesBuilder().agreementID(id.toString()).header(messageHeader.toRdf()).owners(LogUtil.extractOwnersFromPayload(payload)).build();

        mongoService.insert(messageToSave, PROCESS);
        LOGGER.debug("Saving message into collection '"+ collection + "' ... OK");
        return messageToSave;
    }

    public Processes createNonExistingProcess(String payload, UUID id) {
        LOGGER.debug("Saving message into collection '"+ PROCESS + "' ...");

        // There is no header since the were no RequestMessage
        Processes messageToSave= new ProcessesBuilder().agreementID(id.toString()).owners(List.of(payload)).build();

        mongoService.insert(messageToSave, PROCESS);
        LOGGER.debug("Saving message into collection '"+ PROCESS + "' ... OK");
        return messageToSave;
    }
}
