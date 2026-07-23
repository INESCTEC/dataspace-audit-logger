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
package pt.inesctec.ids.clearinghouse.app4edi.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import pt.inesctec.ids.clearinghouse.app4edi.domain.ConsumedEdiArtifact;


public interface ConsumedEdiArtifactRepository extends JpaRepository<ConsumedEdiArtifact, Long> {

  public List<ConsumedEdiArtifact> findByPartnerId(String partnerId);

  public List<ConsumedEdiArtifact> findByPartnerIdAndStatusOrderByConsumedTimeAsc(String partnerId, String status);
  public Page<ConsumedEdiArtifact> findByPartnerIdAndStatusOrderByConsumedTimeAsc(String partnerId, String status, Pageable pageable);

  public List<ConsumedEdiArtifact> findByPartnerIdAndStatusOrderByArchivedTimeAsc(String partnerId, String status);
  public Page<ConsumedEdiArtifact> findByPartnerIdAndStatusOrderByArchivedTimeAsc(String partnerId, String status, Pageable pageable);

  public Optional<ConsumedEdiArtifact> findByPartnerIdAndFromPartnerIdAndFilePath(String partnerId, String fromPartnerId, String filePath);

  public List<ConsumedEdiArtifact> findByPartnerIdAndFromPartnerIdAndStatusOrderByConsumedTimeAsc(String partnerId, String fromPartnerId, String status);
  public Page<ConsumedEdiArtifact> findByPartnerIdAndFromPartnerIdAndStatusOrderByConsumedTimeAsc(String partnerId, String fromPartnerId, String status, Pageable pageable);

  public Optional<ConsumedEdiArtifact> findByPartnerIdAndFromPartnerIdAndFilePathAndStatus(String partnerId, String fromPartnerId, String filePath, String status);

  public void deleteByPartnerId(String partnerId);

}
