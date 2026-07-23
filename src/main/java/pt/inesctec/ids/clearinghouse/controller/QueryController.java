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
import ids.messaging.util.IdsMessageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.inesctec.ids.clearinghouse.converter.JsonConverter;
import pt.inesctec.ids.clearinghouse.dto.QueryDto;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;
import pt.inesctec.ids.clearinghouse.service.MongoService;
import pt.inesctec.ids.clearinghouse.service.QueryService;
import pt.inesctec.ids.clearinghouse.service.SignService;
import pt.inesctec.ids.clearinghouse.util.DapsUtil;
import pt.inesctec.ids.clearinghouse.util.MessageUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@ApiResponse
@Tag(name = "3. Query", description = "API for query data")
public class QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    private QueryService queryService;
    private final SignService signService;

    final MongoService mongoService;
    private final Connector connector;
    private final DapsTokenProvider provider;


    public QueryController(QueryService queryService, SignService signService, final ConfigContainer container,
                           final DapsTokenProvider provider, MongoService mongoService) {
        this.queryService = queryService;
        this.signService = signService;
        this.connector = container.getConnector();
        this.provider = provider;
        this.mongoService = mongoService;
    }




    /**
     * Queries the Clearing House
     * @return Documents or error response.
     */
    @PostMapping(value = {"/messages/query/{id}"})
    @Operation(summary = "Queries the Clearing House", responses = {
            @ApiResponse(responseCode = "200", description = "successful operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorize"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseBody
    /*@RequestBody(required = true, description = "The Body",
            content = {
                    @Content(mediaType = MediaType.ALL_VALUE)
            }
    )*/
    public ResponseEntity<?> queryProcess(
            @PathVariable("id") UUID agreementID,
            final HttpServletRequest request,
            @RequestHeader final Map<String, String> mapHeaders,
            @RequestPart("header") String headerMessage,
            @RequestPart("payload") String payload) throws Exception {

        LOGGER.info("Queriyng the Agreement with id "+agreementID+".");

        /*String messageHeader= ControllerUtil.extractHeaderFromHttpServletRequest(request);
        String messagePayload=ControllerUtil.extractPayloadFromHttpServletRequest(request);
        System.out.println(messageHeader);
        System.out.println(messagePayload);*/

        // The ID is the Agreement ID

        // Deserialize the message
        QueryMessage message= MessageUtil.deserialize(headerMessage, QueryMessage.class);

        // Check if user has permissions to query this agreement

        // If the user cannot access this agreement return error message
        if(!queryService.checkPermissions(message.getSecurityToken(), agreementID.toString())) {
            LOGGER.info("User does not have permissions to access the agreement");
            Message headerToReturn=new RejectionMessageBuilder()
                    ._securityToken_(provider.getDAT())
                    ._correlationMessage_(message.getId())
                    ._issued_(IdsMessageUtils.getGregorianNow())
                    ._issuerConnector_(connector.getId())
                    ._modelVersion_(connector.getOutboundModelVersion())
                    ._senderAgent_(connector.getId())
                    .build();

            // Create MessageResponse
            ResponseMessage responseMessage =MessageUtil.createResponseMessage(headerToReturn, null);

            LOGGER.info("Returning error message");
            // Send Message Response
            return MessageUtil.buildUnauthorizeResponseEntity(responseMessage);
        }


        // Handle the header to return
        Message headerToReturn=new ResultMessageBuilder()
                ._securityToken_(provider.getDAT())
                ._correlationMessage_(message.getId())
                ._issued_(IdsMessageUtils.getGregorianNow())
                ._issuerConnector_(connector.getId())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._senderAgent_(connector.getId())
                .build();
        LOGGER.debug("Returning header: "+headerToReturn);


        // Build payload to return:
        //  1) Get list of messages from database
        List<ClearingHouseMessage> messagesFromAgreement= queryService.findAllByAgreement(agreementID.toString());

        if(messagesFromAgreement.size()==0) {
            LOGGER.info("There are no messages from that agreement.");
        }

        //  2) Create the list (array) of log messages
        QueryDto payloadToReturn= queryService.buildQueryPayload(messagesFromAgreement, message, connector, DapsUtil.getDat(provider));


        // Create MessageResponse
        JsonConverter jsonConverter=new JsonConverter();
        String stringToSend= jsonConverter.object2String(payloadToReturn);
        // System.out.println("Serialized Query" + stringToSend);

        // Create MessageResponse
        ResponseMessage responseMessage =MessageUtil.createResponseMessage(headerToReturn, stringToSend);

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
