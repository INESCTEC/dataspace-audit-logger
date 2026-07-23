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

import de.fraunhofer.iais.eis.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pt.inesctec.ids.clearinghouse.dto.LogMessageDto;
import pt.inesctec.ids.clearinghouse.dto.QueryDto;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.util.DapsUtil;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class QueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    @Autowired private MongoService mongoService;


    /**
     * Find all ClearingHouseMessage on DB from a specific agreement
     *
     * @param agreementID the agreement for searching
     * */
    public List<ClearingHouseMessage> findAllByAgreement(String agreementID) {
        return mongoService.findAllMessagesByAgreement_ExcludeProcesses(agreementID);
    }


    /**
     * Builds the response to send to the user
     * Create the list (array) of log messages to return
     * @param messagesFromAgreement list of messages stored on the DB from one specific agreement
     * @param queryMessage IDS message
     * @param connector the CH Connector to build a LogMessage to return
     * @param dat
     *
     * @return QueryDTO
     * */
    public QueryDto buildQueryPayload(List<ClearingHouseMessage> messagesFromAgreement, QueryMessage queryMessage,
                                      Connector connector, DynamicAttributeToken dat)  {

        LOGGER.debug("Building list to return ...");
        // Build Log messages
        List<LogMessageDto> clearingHouseMessageList =new ArrayList<>();
        messagesFromAgreement.forEach(mns ->{
            // Builds the idsa LogMessage
            LogMessage originalLog= MessageUtil.buildLogMessages(queryMessage.getSenderAgent().toString(), connector, dat);

            // Transforms the message into the expected output for the LogMessages. Adds the payload
            LogMessageDto logMessageDto=new LogMessageDto(originalLog, mns.getPayload());

            //System.out.println(logMessageDto.toJson());
            clearingHouseMessageList.add(logMessageDto);

        });

        QueryDto queryDto=new QueryDto(clearingHouseMessageList);
        LOGGER.debug("Building list to return ... OK");

        return queryDto;
    }

    /**
     * Checks if a User can access an agreement
     *
     * @param agreementID agreement to access
     * @param requesterDat dat from a user that wants access to the agreement
     * @return if a user can access or not
     * */
    public boolean checkPermissions(DynamicAttributeToken requesterDat, String agreementID) {

        AtomicBoolean hasPermission= new AtomicBoolean(false);
        // Get all processes from that agreement. It Should only have 1 line
        List<Processes> processesFromAgreement=mongoService.findProcessFromAgreement(agreementID);

        List<String> ownersInList=new ArrayList<>();

        // List should only have 1 record
        // Get the owners of this process
        processesFromAgreement.forEach(process ->{
            //ownersInList.addAll(LogUtil.extractOwnersFromPayload(process.getPayload()));
            // Add owners if they exist
            if(process.getOwners()!=null){
                ownersInList.addAll(process.getOwners());
            }
        });


        // I need to find out if the client ID belongs to the list of owners.
        // Gets the DAT from the client that issued the QueryMessage
        String client=DapsUtil.getClientFromDAT(requesterDat);
        LOGGER.debug("The client is: " + client);

        LOGGER.debug("The owners of this process are: "+ownersInList.toString());

        LOGGER.info("Checking if user '"+client+"' is authorized to access '"+agreementID+"' ...");


        //For each owner, check if it can access the agreement
        ownersInList.forEach(owner ->{
            if(client.equals(owner)) {
                hasPermission.set(true);
                LOGGER.info("... OK Access Granted");
            }
        });

        // Logs that User cannot access the agreement
        if(!hasPermission.get()){
            LOGGER.info(" ... NOK User cannot access");
        }
        return hasPermission.get();
    }
}
