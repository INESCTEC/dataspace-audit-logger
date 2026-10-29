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
package pt.inesctec.ids.clearinghouse.app4edi.controller;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import pt.inesctec.ids.clearinghouse.app4edi.domain.ConsumedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.domain.ProvidedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.service.DataConverter;
import pt.inesctec.ids.clearinghouse.app4edi.service.EdiArtifactConsumerService;
import pt.inesctec.ids.clearinghouse.app4edi.service.EdiArtifactProviderService;
import pt.inesctec.ids.clearinghouse.app4edi.util.ExceptionUtil;

/* API :
 *   POST   /api/provided-edi-artifacts/update
 *   GET    /api/provided-edi-artifacts/all
 *   GET    /api/provided-edi-artifacts/from-participant/{participantId}
 *   DEL    /api/provided-edi-artifacts/from-participant/{participantId}
 *
 *   POST   /api/consumed-edi-artifacts/update
 *   GET    /api/consumed-edi-artifacts/all
 *   GET    /api/consumed-edi-artifacts/to-participant/{participantId}
 *   DEL    /api/consumed-edi-artifacts/to-participant/{participantId}
 */

//@RestController == @Controller + @ResponseBody
@RestController // marks the class as a controller where every method returns a domain object instead of a view
@RequestMapping("/api")
@Slf4j
public class EdiArtifactsLogController {

  private final ResponseEntities responseEntities;
  private final DataConverter dataConverter;
  private final EdiArtifactProviderService ediArtifactProviderService;
  private final EdiArtifactConsumerService ediArtifactConsumerService;

  @Autowired
  public EdiArtifactsLogController(ResponseEntities responseEntities, DataConverter dataConverter,
    EdiArtifactProviderService ediArtifactProviderService, EdiArtifactConsumerService ediArtifactConsumerService ) {
    super();
    this.responseEntities = responseEntities;
    this.dataConverter = dataConverter; 
    this.ediArtifactProviderService = ediArtifactProviderService;
    this.ediArtifactConsumerService = ediArtifactConsumerService;
  }

  // POST   /api/provided-edi-artifacts/update
  @PostMapping(path = "provided-edi-artifacts/update", consumes = { MediaType.APPLICATION_JSON_VALUE })
  @Operation(
    summary = "Update the provided edi artifacts log",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "201", description = "Item created"),
      @ApiResponse(responseCode = "400", description = "Bad input parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
  )
  @RequestBody(required = true, description = "The Sender Event")
  public ResponseEntity<?> updateProvidedEdiArtifact(InputStream inputStream, final HttpServletRequest request) {
    try {
      JsonNode json = dataConverter.inputStream2Json(inputStream);
      removeNonSupportedFieldsOnProvidedEdiArtifact(json);
      ProvidedEdiArtifact providedEdiArtifact = dataConverter.json2Object(ProvidedEdiArtifact.class, json);

      ediArtifactProviderService.update(providedEdiArtifact);

      return responseEntities.withStatus(HttpStatus.CREATED);
    }
    catch (Throwable e) {
      log.warn("updateProvidedEdiArtifact {} ", ExceptionUtil.getFullStackTrace(e));
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }

  private void removeNonSupportedFieldsOnProvidedEdiArtifact(JsonNode json) {
    // App4Edi (edc version) sends this field
    if (json.has("docContentType")) {
      ObjectNode object = (ObjectNode) json;
      object.remove("docContentType");
    }
  }

  private void removeNonSupportedFieldsOnConsumedEdiArtifact(JsonNode json) {
    // App4Edi (edc version) sends this field
    if (json.has("docContentType")) {
      ObjectNode object = (ObjectNode) json;
      object.remove("docContentType");
    }
    if (json.has("createdTime")) {
      ObjectNode object = (ObjectNode) json;
      object.remove("createdTime");
    }
  }

  // POST   /api/consumed-edi-artifacts/update
  @PostMapping(path = "consumed-edi-artifacts/update", consumes = { MediaType.APPLICATION_JSON_VALUE })
  @Operation(
    summary = "Update the consumed edi artifacts log",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "201", description = "Item created"),
      @ApiResponse(responseCode = "400", description = "Bad input parameters"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
  )
  @RequestBody(required = true, description = "The Receiver Event")
  public ResponseEntity<?> updateConsumedEdiArtifact(InputStream inputStream, final HttpServletRequest request) {
    try {
      JsonNode json = dataConverter.inputStream2Json(inputStream);
      removeNonSupportedFieldsOnConsumedEdiArtifact(json);
      ConsumedEdiArtifact consumedEdiArtifact = dataConverter.json2Object(ConsumedEdiArtifact.class, json);

      ediArtifactConsumerService.update(consumedEdiArtifact);

      return responseEntities.withStatus(HttpStatus.CREATED);
    }
    catch (Throwable e) {
      log.warn("updateConsumedEdiArtifact {} ", ExceptionUtil.getFullStackTrace(e));
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }

  //  GET    /api/provided-edi-artifacts/all
  @GetMapping(path = "provided-edi-artifacts/all", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Operation(
    summary = "Returns the provided edi artifacts log",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "200", description = "List of artifacts", 
        content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
    )
  public ResponseEntity<?> getProvidedEdiArtifactAll(final HttpServletRequest request) {
    try {
      List<ProvidedEdiArtifact> list = ediArtifactProviderService.findAll();
      ArrayNode arrayNode = dataConverter.providedEdiArtifactList2JsonArray(list);

      return responseEntities.withJson(arrayNode, HttpStatus.OK);
    }
    catch (Throwable e) {
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }

  //  GET    /api/consumed-edi-artifacts/all
  @GetMapping(path = "consumed-edi-artifacts/all", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Operation(
    summary = "Returns the consumed edi artifacts log",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "200", description = "List of artifacts", 
        content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
    )
  public ResponseEntity<?> getConsumedEdiArtifactAll(final HttpServletRequest request) {
    try {
      List<ConsumedEdiArtifact> list = ediArtifactConsumerService.findAll();
      ArrayNode arrayNode = dataConverter.consumedEdiArtifactList2JsonArray(list);

      return responseEntities.withJson(arrayNode, HttpStatus.OK);
    }
    catch (Throwable e) {
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }

  //  GET    /api/provided-edi-artifacts/from-participant/{participantId}
  @GetMapping(path = "provided-edi-artifacts/from-participant/{participantId}", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Operation(
    summary = "Returns the provided edi artifacts log",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "200", description = "List of artifacts created by a specific participant", 
        content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
    )
  public ResponseEntity<?> getProvidedEdiArtifactByParticipant(final HttpServletRequest request, @PathVariable String participantId) {
    try {
      List<ProvidedEdiArtifact> list = ediArtifactProviderService.findByPartnerId(participantId);
      ArrayNode arrayNode = dataConverter.providedEdiArtifactList2JsonArray(list);

      return responseEntities.withJson(arrayNode, HttpStatus.OK);
    }
    catch (Throwable e) {
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }

  //  DEL    /api/provided-edi-artifacts/from-participant/{participantId}
  @DeleteMapping(path = "provided-edi-artifacts/from-participant/{participantId}")
  @Operation(
    summary = "Removes the provided edi artifacts log of given participant",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "204", description ="No content", 
        content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
    )
  public ResponseEntity<?> delProvidedEdiArtifactByParticipant(final HttpServletRequest request, @PathVariable String participantId) {
    try {
      ediArtifactProviderService.deleteByPartnerId(participantId);

      return responseEntities.withStatus(HttpStatus.NO_CONTENT);
    }
    catch (Throwable e) {
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }
  
  //  GET    /api/consumed-edi-artifacts/to-participant/{participantId}
  @GetMapping(path = "consumed-edi-artifacts/to-participant/{participantId}", produces = { MediaType.APPLICATION_JSON_VALUE })
  @Operation(
    summary = "Returns the consumed edi artifacts log",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "200", description = "List of artifacts received by a specific participant", 
        content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
    )
  public ResponseEntity<?> getConsumedEdiArtifactByParticipant(final HttpServletRequest request, @PathVariable String participantId) {
    try {
      List<ConsumedEdiArtifact> list = ediArtifactConsumerService.findByPartnerId(participantId);
      ArrayNode arrayNode = dataConverter.consumedEdiArtifactList2JsonArray(list);

      return responseEntities.withJson(arrayNode, HttpStatus.OK);
    }
    catch (Throwable e) {
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }

  //  DEL    /api/consumed-edi-artifacts/to-participant/{participantId}
  @DeleteMapping(path = "consumed-edi-artifacts/to-participant/{participantId}")
  @Operation(
    summary = "Removed the consumed edi artifacts log of given participant",
    tags = {"5. EDI Scenario - artifacts log"},
    responses = {
      @ApiResponse(responseCode = "204", description ="No content", 
        content = { @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)}),
      @ApiResponse(responseCode = "500", description = "Internal server error")
    }
    )
  public ResponseEntity<?> delConsumedEdiArtifactByParticipant(final HttpServletRequest request, @PathVariable String participantId) {
    try {
      ediArtifactConsumerService.deleteByPartnerId(participantId);

      return responseEntities.withStatus(HttpStatus.NO_CONTENT);
    }
    catch (Throwable e) {
      return responseEntities.withException(e, request.getMethod() + " " + request.getServletPath());
    }
  }
}
