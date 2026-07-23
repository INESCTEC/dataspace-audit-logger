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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;
import pt.inesctec.ids.clearinghouse.app4edi.exception.BadRequestException;
import pt.inesctec.ids.clearinghouse.app4edi.service.DataConverter;
import pt.inesctec.ids.clearinghouse.app4edi.util.ExceptionUtil;

@Service
@Scope("singleton")
@Slf4j
public class ResponseEntities {

  private final DataConverter dataConverter;

  @Autowired
  public ResponseEntities(DataConverter dataConverter) {
    super();
    this.dataConverter = dataConverter;
  }

  public ResponseEntity<?> withStatus(HttpStatus status) throws Exception {
    return new ResponseEntity<>(status);
  }

  public ResponseEntity<JsonNode> withError(Error error, HttpStatus status) throws Exception {
    JsonNode body = dataConverter.object2Json(error);
    log.warn("Failure on REST controller: {} {}", status.toString(), error.error);
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
  }

  public ResponseEntity<JsonNode> withJson(JsonNode body, HttpStatus status) throws Exception {
    return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
  }

  public ResponseEntity<JsonNode> withException(Throwable e, String... details) {
    if (e instanceof BadRequestException)
      log.warn(e.toString());
    else
      log.warn(ExceptionUtil.getFullStackTrace(e, details));

    ObjectNode body = dataConverter.newObjectNode();
    body.put("exception", e.toString());
    for (int i = 0; i < details.length; ++i)
      body.put("context_" + i, details[i]);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(body);
  }

}
