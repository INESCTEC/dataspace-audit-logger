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
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import ids.messaging.common.SerializeException;
import ids.messaging.core.config.ConfigContainer;
import ids.messaging.core.daps.*;
import ids.messaging.response.ErrorResponse;
import ids.messaging.response.MessageResponse;
import ids.messaging.util.IdsMessageUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import pt.inesctec.ids.clearinghouse.TestUtil;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DescriptionRequestMessageHandlerTest {

  Map<String, Object> map;
  Serializer serializer;
  TestUtil testUtil;


  @Autowired
  private DescriptionRequestMessageHandler handler;

  @Mock
  private ConfigContainer connectorConfig;

  @Mock
  DapsTokenProvider provider;

  @MockBean
  private ConfigurationModel configurationModel;

  @MockBean
  private DapsValidator dapsValidator;

  DescriptionRequestMessage message;

  URI participantID;

  @BeforeEach
  void setUp() throws ConnectorMissingCertExtensionException, DapsConnectionException, DapsEmptyResponseException {
    testUtil=new TestUtil();
    Connector connector= new BaseConnectorBuilder(URI.create("https://test-broker.com/id/test/connector-producer"))
            ._hasDefaultEndpoint_(
                    new ConnectorEndpointBuilder(URI.create("https://test-broker.com/id/test/connectorEndpoint/dhiasdcna"))
                            ._accessURL_(URI.create("https://host.docker.internal:8080/test/connector/api/ids/data")).build())
            ._inboundModelVersion_("4.2.6")
            ._outboundModelVersion_("4.2.6")
            ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
            ._maintainer_(URI.create("https://www.inesctec.pt/"))
            ._curator_(URI.create("https://www.inesctec.pt/"))
            ._description_(new TypedLiteral("Description"))
            ._title_(new TypedLiteral("Title"))
            ._publicKey_(new PublicKeyBuilder(URI.create("https://test-broker.com/id/test/publicKey/dhiasdcna"))
                    ._keyValue_("KeyValueExample".getBytes())
                    ._keyType_(KeyType.RSA)
                    .build())
            ._resourceCatalog_(new ResourceCatalogBuilder(URI.create("https://test-broker.com/id/test/catalog/api/connector-producer"))
                    .build())
            .build();

    DynamicAttributeToken dat=new DynamicAttributeTokenBuilder()._tokenValue_("Fake token")._tokenFormat_(TokenFormat.JWT).build();

    Mockito.when(connectorConfig.getConnector()).thenReturn(connector);
    Mockito.when(connectorConfig.getConfigurationModel()).thenReturn(configurationModel);
    Mockito.when(connectorConfig.getConfigurationModel().getConnectorDeployMode()).thenReturn(ConnectorDeployMode.TEST_DEPLOYMENT);
    Mockito.when(provider.provideDapsToken()).thenReturn("Mock Token");
    Mockito.when(dapsValidator.checkDat(dat)).thenReturn(true);
    Mockito.when(provider.getDAT()).thenReturn(dat);

    handler=new DescriptionRequestMessageHandler(connectorConfig, provider);

    serializer=new Serializer();

    try {


      // Create the Header to send on the handleMessage()
      message=new DescriptionRequestMessageBuilder()
              ._securityToken_(provider.getDAT())
              ._issued_(IdsMessageUtils.getGregorianNow())
              ._issuerConnector_(connectorConfig.getConnector().getId())
              ._modelVersion_(this.connectorConfig.getConnector().getOutboundModelVersion())
              ._senderAgent_(connectorConfig.getConnector().getId())
              ._recipientConnector_(connector.getHasDefaultEndpoint().getAccessURL())
              ._requestedElement_(participantID)
              .build();

      //IdsMessageUtils.buildSelfDeclaration()

    } catch (ConnectorMissingCertExtensionException | DapsConnectionException | DapsEmptyResponseException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @Order(1)
  void handleMessage() {

    try {
      // Send the Message
      MessageResponse messageResponse = handler.handleMessage((DescriptionRequestMessageImpl) message, null);

      // Get the Response
      map = messageResponse.createMultipartMap(serializer);
      //System.out.println(map.toString());
      try {
        System.out.println(map.get("header").getClass());
        System.out.println(map.get("header").toString());
        final var requestHeader = serializer.deserialize((String) map.get("header"), DescriptionResponseMessage.class);
        assertInstanceOf(DescriptionResponseMessage.class, requestHeader);
        Assertions.assertTrue(DescriptionResponseMessage.class.isAssignableFrom(requestHeader.getClass()),
                "Method should return DescriptionResponseMessage. It returns " + requestHeader.getClass());
      } catch (IOException e) {
       throw new RuntimeException(e);
      }

      System.out.println("Payload: "+map.get("payload").toString());

    } catch (SerializeException e) {
      throw new RuntimeException(e);
    }
  }

}