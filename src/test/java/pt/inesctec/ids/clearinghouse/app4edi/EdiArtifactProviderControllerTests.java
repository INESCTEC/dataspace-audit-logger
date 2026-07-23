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
package pt.inesctec.ids.clearinghouse.app4edi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import pt.inesctec.ids.clearinghouse.app4edi.domain.ConsumedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.domain.ProvidedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.service.AppApiAccessByOkhttp;
import pt.inesctec.ids.clearinghouse.app4edi.service.DataConverter;
import pt.inesctec.ids.clearinghouse.app4edi.util.Util;

@IntegrationTests
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class EdiArtifactProviderControllerTests {

  @Autowired
  private ApplicationContext context;
  @Autowired
  private DataConverter dataConverter;
  @Autowired
  private Database database;

  @Value("${spring.security.user.name:}")
  private String userName;
  @Value("${spring.security.user.password:}")
  private String userPassword;

  @BeforeEach
  public void setUp() throws Exception {
    assertThat(context).isNotNull();

    database.deleteAllEntities();
  }

  /*
   * test UpdateOfProvidedEdiArtifacts
   */
  @Test
  public void testUpdateOfProvidedEdiArtifacts() {
    try {
      // Create ProvidedEdiArtifact and POST it
      ProvidedEdiArtifact ediArtifact = createProvidedEdiArtifact();
      {
        String ediArtifactJsonString = dataConverter.object2String(ediArtifact);
        postProvidedEdiArtifactOnServer(ediArtifactJsonString);
      }

      // Change the ProvidedEdiArtifact and POST it
      ediArtifact.docSchemaName = "docSchemaName";
      ediArtifact.docSchemaLocationUrl = "docSchemaLocationUrl";
      ediArtifact.publishedTime = Util.nowAsMillis();
      ediArtifact.consumedTime = Util.nowAsMillis();
      ediArtifact.consumedAndArchivedTime = Util.nowAsMillis();
      ediArtifact.rejectedReason = "rejectedReason";
      ediArtifact.rejectedTime = Util.nowAsMillis();
      {
        String ediArtifactJsonString = dataConverter.object2String(ediArtifact);
        postProvidedEdiArtifactOnServer(ediArtifactJsonString);
      }

      // Get the ProvidedEdiArtifact
      {
        List<ProvidedEdiArtifact> list = getProvidedEdiArtifactsFromServer();
        assertThat(list.size()).isEqualTo(1);
      }

      // Get the ProvidedEdiArtifact
      {
        List<ProvidedEdiArtifact> list = getProvidedEdiArtifactsByParticipantFromServer("partnerId");
        assertThat(list.size()).isEqualTo(1);
      }
    }
    catch (Throwable t) {
      t.printStackTrace();
      Assertions.fail(t.toString());
    }
  }

  private void postProvidedEdiArtifactOnServer(String ediArtifactJsonString) throws Exception {
    // Post Create ProvidedEdiArtifact into the server
    String url = UriComponentsBuilder.fromUriString("http://localhost:8083/api/provided-edi-artifacts/update").build().toString();
    AppApiAccessByOkhttp api = context.getBean(AppApiAccessByOkhttp.class);
    api.setUsername(userName);
    api.setPassword(userPassword);
    api.runPostRequest(url, ediArtifactJsonString, MediaType.APPLICATION_JSON_VALUE);
    api.checkRunPostRequestResponse();
  }

  private ProvidedEdiArtifact createProvidedEdiArtifact() {
    ProvidedEdiArtifact artifact = new ProvidedEdiArtifact(0L, "partnerId", "toPartnerId", "docTitle", Util.nowAsMillis(), "filePath", 0L);
    return artifact;
  }

  private List<ProvidedEdiArtifact> getProvidedEdiArtifactsFromServer() throws Exception {
    // GET ProvidedEdiArtifact from the server
    String url = UriComponentsBuilder.fromUriString("http://localhost:8083/api/provided-edi-artifacts/all").build().toString();
    AppApiAccessByOkhttp api = context.getBean(AppApiAccessByOkhttp.class);
    api.setUsername(userName);
    api.setPassword(userPassword);
    api.runGetRequest(url);
    api.checkRunGetRequestResponse();

    JsonNode json = api.getBodyAsJson();
    assertThat(json.isArray()).isTrue();
    return dataConverter.jsonArray2ProvidedEdiArtifactList2(json);
  }

  private List<ProvidedEdiArtifact> getProvidedEdiArtifactsByParticipantFromServer(String participantId) throws Exception {
    // GET ProvidedEdiArtifact from the server
    String url = UriComponentsBuilder.fromUriString("http://localhost:8083/api/provided-edi-artifacts/from-participant").pathSegment(participantId).build().toString();
    AppApiAccessByOkhttp api = context.getBean(AppApiAccessByOkhttp.class);
    api.setUsername(userName);
    api.setPassword(userPassword);
    api.runGetRequest(url);
    api.checkRunGetRequestResponse();

    JsonNode json = api.getBodyAsJson();
    assertThat(json.isArray()).isTrue();
    return dataConverter.jsonArray2ProvidedEdiArtifactList2(json);
  }

  /*
   * test UpdateOfConsumedEdiArtifacts
   */
  @Test
  public void testUpdateOfConsumedEdiArtifacts() {
    try {
      // Create ConsumedEdiArtifact and POST it
      ConsumedEdiArtifact ediArtifact = createConsumedEdiArtifact();
      {
        String ediArtifactJsonString = dataConverter.object2String(ediArtifact);
        postConsumedEdiArtifactOnServer(ediArtifactJsonString);
      }

      // Change the ConsumedEdiArtifact and POST it
      ediArtifact.archivedTime = Util.nowAsMillis();
      {
        String ediArtifactJsonString = dataConverter.object2String(ediArtifact);
        postConsumedEdiArtifactOnServer(ediArtifactJsonString);
      }

      // Get the ConsumedEdiArtifact
      {
        List<ConsumedEdiArtifact> list = getConsumedEdiArtifactsFromServer();
        assertThat(list.size()).isEqualTo(1);
      }

      // Get the ConsumedEdiArtifact
      {
        List<ConsumedEdiArtifact> list = getConsumedEdiArtifactsByParticipantFromServer("partnerId");
        assertThat(list.size()).isEqualTo(1);
      }
    }
    catch (Throwable t) {
      t.printStackTrace();
      Assertions.fail(t.toString());
    }
  }

  private void postConsumedEdiArtifactOnServer(String ediArtifactJsonString) throws Exception {
    // Post Create ConsumedEdiArtifact into the server
    String url = UriComponentsBuilder.fromUriString("http://localhost:8083/api/consumed-edi-artifacts/update").build().toString();
    AppApiAccessByOkhttp api = context.getBean(AppApiAccessByOkhttp.class);
    api.setUsername(userName);
    api.setPassword(userPassword);
    api.runPostRequest(url, ediArtifactJsonString, MediaType.APPLICATION_JSON_VALUE);
    api.checkRunPostRequestResponse();
  }

  private ConsumedEdiArtifact createConsumedEdiArtifact() {
    ConsumedEdiArtifact artifact = new ConsumedEdiArtifact(0L, "partnerId", "fromPartnerId", "docTitle", Util.nowAsMillis(), "filePath", 0L, "docSchemaName", "docSchemaLocationUrl");
    return artifact;
  }

  private List<ConsumedEdiArtifact> getConsumedEdiArtifactsFromServer() throws Exception {
    // GET ConsumedEdiArtifact from the server
    String url = UriComponentsBuilder.fromUriString("http://localhost:8083/api/consumed-edi-artifacts/all").build().toString();
    AppApiAccessByOkhttp api = context.getBean(AppApiAccessByOkhttp.class);
    api.setUsername(userName);
    api.setPassword(userPassword);
    api.runGetRequest(url);
    api.checkRunGetRequestResponse();

    JsonNode json = api.getBodyAsJson();
    assertThat(json.isArray()).isTrue();
    return dataConverter.jsonArray2ConsumedEdiArtifactList2(json);
  }

  private List<ConsumedEdiArtifact> getConsumedEdiArtifactsByParticipantFromServer(String participantId) throws Exception {
    // GET ConsumedEdiArtifact from the server
    String url = UriComponentsBuilder.fromUriString("http://localhost:8083/api/consumed-edi-artifacts/to-participant").pathSegment(participantId).build().toString();
    AppApiAccessByOkhttp api = context.getBean(AppApiAccessByOkhttp.class);
    api.setUsername(userName);
    api.setPassword(userPassword);
    api.runGetRequest(url);
    api.checkRunGetRequestResponse();

    JsonNode json = api.getBodyAsJson();
    assertThat(json.isArray()).isTrue();
    return dataConverter.jsonArray2ConsumedEdiArtifactList2(json);
  }

}
