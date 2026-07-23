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
package pt.inesctec.ids.clearinghouse.messaging;

import de.fraunhofer.iais.eis.*;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.handler.message.MessageHandler;
import ids.messaging.handler.message.MessagePayload;
import ids.messaging.handler.message.SupportedMessageType;
import ids.messaging.response.BodyResponse;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pt.inesctec.ids.clearinghouse.exceptions.BadRequestException;

import java.net.URI;


@Component
@SupportedMessageType(DescriptionRequestMessageImpl.class)
public class DescriptionRequestMessageHandler implements MessageHandler<DescriptionRequestMessageImpl> {
  private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionRequestMessageHandler.class);

  private final Connector connector;
  private final DapsTokenProvider provider;

  public DescriptionRequestMessageHandler(final ConfigContainer container,
                                          final DapsTokenProvider provider) {
    this.connector = container.getConnector();
    this.provider = provider;
  }


  /**
   * This message implements the logic that is needed to handle the message.
   *
   * @param requestMessage The DescriptionRequestMessage (part one of the multipart message) containing interesting meta data.
   * @param messagePayload The received payload that has to be handled here - The Connector Self-description
   * @return received payload as string, wrapped inside a @{@link BodyResponse}
   */
  @Override
  public MessageResponse handleMessage(DescriptionRequestMessageImpl requestMessage, MessagePayload messagePayload) {
    LOGGER.info("Received a DescriptionRequestMessage!");

    try {
      final var message = createSuccessReturnMessage(requestMessage);

      //System.out.println(IdsMessageUtils.buildSelfDeclaration(connector));
      return BodyResponse.create(message, IdsMessageUtils.buildSelfDeclaration(connector));
    }
    catch (BadRequestException e){
      LOGGER.error("Error while handling message...");
      LOGGER.error("... "+e.getMessage());
      return ErrorResponse.withDefaultHeader(RejectionReason.BAD_PARAMETERS, e.getMessage(), connector.getId(), connector.getOutboundModelVersion());
    }
    catch (Exception e) {
      LOGGER.error("Error while handling message...");
      LOGGER.error("... "+e.getMessage());
      return ErrorResponse.withDefaultHeader(RejectionReason.INTERNAL_RECIPIENT_ERROR, e.getMessage(), connector.getId(), connector.getOutboundModelVersion());
    }
    //System.out.println(requestMessage.toRdf());

  }



  public DescriptionResponseMessage createSuccessReturnMessage(DescriptionRequestMessageImpl message) throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
    LOGGER.info("Creating message...");
    return new DescriptionResponseMessageBuilder()
            ._securityToken_(provider.getDAT())
            ._correlationMessage_(message.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connector.getId())
            ._modelVersion_(this.connector.getOutboundModelVersion())
            ._senderAgent_(connector.getId())
            .build();

  }

  public RejectionMessage createRejectionReturnMessage(ConnectorUpdateMessageImpl message) throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
    return new RejectionMessageBuilder()
            ._securityToken_(provider.getDAT())
            ._correlationMessage_(message.getId())
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(connector.getId())
            ._modelVersion_(this.connector.getOutboundModelVersion())
            ._senderAgent_(connector.getId())
            .build();

  }
}