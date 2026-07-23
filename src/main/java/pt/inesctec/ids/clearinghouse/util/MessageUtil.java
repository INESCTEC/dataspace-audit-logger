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
package pt.inesctec.ids.clearinghouse.util;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.DeserializeException;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.util.IdsMessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import pt.inesctec.ids.clearinghouse.dto.ResponseMessage;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;

import java.io.IOException;
import java.net.URI;

public class MessageUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageUtil.class);

  public static <T> T deserialize(String header, Class<?> classToD) {
    Serializer serializer=new Serializer();
    try {
      return (T) serializer.deserialize(header, classToD);
    } catch (IOException e) {
      throw new InvalidValueException("Error while deserializing resource: "+e.getMessage());
    }

  }

  public static boolean checkDeserialize(String header, Class<?> classToD) {
    try {
      MessageUtil.deserialize(header, classToD);
      return true;
    } catch (RuntimeException e) {
      return false;
    }

  }

  public static Message buildReturnHeader(Message message, Connector connector, DapsTokenProvider provider) {
    try {
      LOGGER.debug("Building message header to return ... ");
      Message headerToReturn=new MessageProcessedNotificationMessageBuilder()
              ._securityToken_(provider.getDAT())
              ._correlationMessage_(message.getId())
              ._issued_(IdsMessageUtils.getGregorianNow())
              ._issuerConnector_(connector.getId())
              ._modelVersion_(connector.getOutboundModelVersion())
              ._senderAgent_(connector.getId())
              .build();
      LOGGER.debug("Building message header to return ... OK");

      return headerToReturn;
    } catch (ConnectorMissingCertExtensionException | DapsConnectionException | DapsEmptyResponseException e) {
      throw new RuntimeException(e);
    }
  }

  public static ResponseMessage createResponseMessage(Message headerToReturn, String payloadToReturn) {
    return new ResponseMessage(headerToReturn, payloadToReturn);
  }

  public static ResponseEntity buildResponseEntity(ResponseMessage responseMessage) {
    return responseMessage.createSuccessResponseEntity();
  }

  public static ResponseEntity buildUnauthorizeResponseEntity(ResponseMessage responseMessage) {
    return responseMessage.createUnauthorizeResponseEntity();
  }

  public static LogMessage buildLogMessages(String connectorID, Connector connector, DynamicAttributeToken dat)  {
    return new LogMessageBuilder()
            ._securityToken_(dat)
            ._issued_(IdsMessageUtils.getGregorianNow())
            ._issuerConnector_(URI.create(connectorID))
            ._modelVersion_(connector.getOutboundModelVersion())
            ._senderAgent_(URI.create(connectorID))
            .build();

  }

}
