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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import pt.inesctec.ids.clearinghouse.app4edi.service.EdiArtifactConsumerService;
import pt.inesctec.ids.clearinghouse.app4edi.service.EdiArtifactProviderService;

@Service
@Scope("singleton")
public class Database {

  private final EdiArtifactProviderService ediArtifactProviderService;
  private final EdiArtifactConsumerService ediArtifactConsumerService;

  @Autowired
  public Database(EdiArtifactProviderService ediArtifactProviderService, EdiArtifactConsumerService ediArtifactConsumerService) {
    super();
    this.ediArtifactProviderService = ediArtifactProviderService;
    this.ediArtifactConsumerService = ediArtifactConsumerService;
  }

  public void deleteAllEntities() {
    ediArtifactProviderService.deleteAll();
    ediArtifactConsumerService.deleteAll();
  }
}
