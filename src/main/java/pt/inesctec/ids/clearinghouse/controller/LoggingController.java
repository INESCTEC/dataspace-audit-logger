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
package pt.inesctec.ids.clearinghouse.controller;

import de.fraunhofer.iais.eis.*;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.DapsTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;
import pt.inesctec.ids.clearinghouse.service.LoggingService;
import pt.inesctec.ids.clearinghouse.service.MongoService;
import pt.inesctec.ids.clearinghouse.service.SignService;
import pt.inesctec.ids.clearinghouse.util.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
@ApiResponse
@Tag(name = "2. Logging", description = "API for Logging data")
public class LoggingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingController.class);

    private LoggingService loggingService;
    private final SignService signService;

    final MongoService mongoService;
    private final Connector connector;
    private final DapsTokenProvider provider;


    public LoggingController(LoggingService loggingService, SignService signService, final ConfigContainer container,
                             final DapsTokenProvider provider, MongoService mongoService) {
        this.loggingService = loggingService;
        this.signService = signService;
        this.connector = container.getConnector();
        this.provider = provider;
        this.mongoService = mongoService;
    }



    /**
     * Adds a Log to the Clearing House
     * @return MessageProcessedNotificationMessage or error response.
     */
    @PostMapping(value = {"/messages/log/{id}"})
    @Operation(summary = "Adds a Log to the Clearing House", responses = {
        @ApiResponse(responseCode = "201", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseBody
    @RequestBody(required = true, description = "The Body",
            content = {
                    @Content(mediaType = MediaType.ALL_VALUE)
            }
    )
    @Transactional
    public ResponseEntity<Object> addLog(@PathVariable("id") UUID id,
                                         HttpServletRequest request,
                                         @RequestHeader final Map<String, String> header,
                                         @RequestPart("header") String headerMessage,
                                         @RequestPart("payload") String payload
    ) {
        LOGGER.debug("Received a LogMessage ... ");
        LOGGER.info("Adding Log to the Clearing House");
        LOGGER.info("The agreementID is " + id);


        // Deserialize the message to a LogMessage
        LogMessage logMessage= MessageUtil.deserialize(headerMessage, LogMessage.class);
        LOGGER.info("The ID of the requester is " + logMessage.getIssuerConnector());

        System.err.println("Found header: \n" + logMessage.toRdf());
        System.err.println("Payload: \n" + payload);


        // If there are no processes, create one
        if(!mongoService.hasProcessFromAgreement(String.valueOf(id))){
            LOGGER.debug("There is no existing Process for this Agreement...");
            LOGGER.debug("One will be created...");
            // Adds a new Process to the Database
            loggingService.createNonExistingProcess(DapsUtil.getClientFromDAT(logMessage.getSecurityToken()), id);
            LOGGER.debug("Process created.");

        }
        else LOGGER.debug("Process already existed.");


        // Save data to the database and build message to return
        ResponseMessage responseMessage=loggingService.handleMessage(logMessage, payload, id, connector, provider);

        // Send Message Response
        return MessageUtil.buildResponseEntity(responseMessage);

    }

    /**
     * Adds a process to the Clearing House
     * @return Self-description or error response.
     */
    //@SecurityRequirements
    @PostMapping(value = {"/process/{id}"})
    @Operation(summary = "Adds a process to the Clearing House", responses = {
            @ApiResponse(responseCode = "201", description = "successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseBody
    @Transactional
    public ResponseEntity<?> addProcess(
            @PathVariable("id") UUID processID,
            final HttpServletRequest request,
            @RequestHeader final Map<String, String> header) {

        LOGGER.info("Adding Process with id "+processID+" to the Clearing House");
        // The ID is the Agreement ID


        String messageHeader=ControllerUtil.extractHeaderFromHttpServletRequest(request);
        String messagePayload=ControllerUtil.extractPayloadFromHttpServletRequest(request);

        // Deserialize the message
        RequestMessage message= MessageUtil.deserialize(messageHeader, RequestMessage.class);

        //System.out.println(message.toRdf());
        //System.out.println("Payload "+messagePayload);


        LOGGER.info("The owners of this process are: "+ messagePayload.toString());


        // Save data to the database and build messageto return
        ResponseMessage responseMessage=loggingService.handleMessage(message, messagePayload, processID, connector, provider);

        // Send Message Response
        return MessageUtil.buildResponseEntity(responseMessage);

    }



    /**
     * <rest id="CH_REST">
     *             <post path="/messages/query/{pid}" id="CH_QUERY_PID">
     *                 <to uri="direct:multipart" />
     *             </post>
     *             <post path="/messages/query/{pid}/{id}" id="CH_QUERY_PID_ID">
     *                 <to uri="direct:multipart" />
     *             </post>
     *             <post path="/messages/log/{pid}" id="CH_LOG_PID">
     *                 <to uri="direct:multipart" />
     *             </post>
     *             <post path="/process/{pid}" id="CH_CREATE_PID">
     *                 <to uri="direct:multipart" />
     *             </post>
     *         </rest>*/
}
