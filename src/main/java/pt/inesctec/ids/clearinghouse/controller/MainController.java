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

import ids.messaging.core.config.ConfigContainer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@ApiResponse
@Tag(name = "1. Clearing House", description = "API for Information regarding the Clearing House")
@RequiredArgsConstructor
public class MainController {

    /**
     * The current connector configuration.
     */
    private final @NonNull ConfigContainer configContainer;


    /**
     * Gets connector self-description without catalogs and resources.
     * @return Self-description or error response.
     */
    @SecurityRequirements
    @GetMapping(value = {"/"}, produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Get the public IDS self-description.", responses = {
        @ApiResponse(responseCode = "200", description = "successful operation"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @ResponseBody
    public ResponseEntity<Object> getPublicSelfDescription() {
        final var connector = configContainer.getConnector();
        return ResponseEntity.ok(connector.toRdf());
    }
}
