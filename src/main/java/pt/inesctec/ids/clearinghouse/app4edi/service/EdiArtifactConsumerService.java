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
package pt.inesctec.ids.clearinghouse.app4edi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pt.inesctec.ids.clearinghouse.app4edi.domain.ConsumedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.domain.ProvidedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.domain.repository.ConsumedEdiArtifactRepository;

@Service
@Slf4j
public class EdiArtifactConsumerService {

  private final ConsumedEdiArtifactRepository repository;

  @Autowired
  public EdiArtifactConsumerService(ConsumedEdiArtifactRepository repository) {
    super();
    this.repository = repository;
  }

  @Transactional
  public ConsumedEdiArtifact create(ConsumedEdiArtifact consumedEdiArtifact) {
    return repository.save(consumedEdiArtifact);
  }

  @Transactional
  public ConsumedEdiArtifact update(ConsumedEdiArtifact consumedEdiArtifact) {
    return repository.save(consumedEdiArtifact);
  }

  public Optional<ConsumedEdiArtifact> findById(Long id) {
    return repository.findById(id);
  }

  public List<ConsumedEdiArtifact> findAll() {
    var list = new ArrayList<ConsumedEdiArtifact>();
    list.addAll(repository.findAll());
    return list;
  }

  public List<ConsumedEdiArtifact> findAll(String partnerId) {
    var list = new ArrayList<ConsumedEdiArtifact>();
    list.addAll(repository.findByPartnerId(partnerId));
    return list;
  }

  public List<ConsumedEdiArtifact> findByPartnerId(String partnerId) {
    return repository.findByPartnerId(partnerId);
  }

  public List<ConsumedEdiArtifact> findByStatusConsumed(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByConsumedTimeAsc(partnerId, ConsumedEdiArtifact.STATUS_CONSUMED);
  }

  public Page<ConsumedEdiArtifact> findByStatusConsumed(String partnerId, Pageable pageable) {
    return repository.findByPartnerIdAndStatusOrderByConsumedTimeAsc(partnerId, ConsumedEdiArtifact.STATUS_CONSUMED, pageable);
  }

  public List<ConsumedEdiArtifact> findByStatusArchived(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByArchivedTimeAsc(partnerId, ConsumedEdiArtifact.STATUS_ARCHIVED);
  }

  public Page<ConsumedEdiArtifact> findByStatusArchived(String partnerId, Pageable pageable) {
    return repository.findByPartnerIdAndStatusOrderByArchivedTimeAsc(partnerId, ConsumedEdiArtifact.STATUS_ARCHIVED, pageable);
  }

  public Optional<ConsumedEdiArtifact> findByFromParterIdAndFilePath(String partnerId, String fromPartnerId, String filePath) {
    return repository.findByPartnerIdAndFromPartnerIdAndFilePath(partnerId, fromPartnerId, filePath);
  }

  public Optional<ConsumedEdiArtifact> findByFromParterIdAndFilePathAndStatusConsumed(String partnerId, String fromPartnerId, String filePath) {
    return repository.findByPartnerIdAndFromPartnerIdAndFilePathAndStatus(partnerId, fromPartnerId, filePath, ConsumedEdiArtifact.STATUS_CONSUMED);
  }

  public List<ConsumedEdiArtifact> findByFromParterIdAndStatusConsumed(String partnerId, String fromPartnerId) {
    return repository.findByPartnerIdAndFromPartnerIdAndStatusOrderByConsumedTimeAsc(partnerId, fromPartnerId, ProvidedEdiArtifact.STATUS_CONSUMED);
  }

  public Page<ConsumedEdiArtifact> findByFromParterIdAndStatusConsumed(String partnerId, String fromPartnerId, Pageable pageable) {
    return repository.findByPartnerIdAndFromPartnerIdAndStatusOrderByConsumedTimeAsc(partnerId, fromPartnerId, ProvidedEdiArtifact.STATUS_CONSUMED, pageable);
  }

  @Transactional
  public void deleteAll() {
    repository.deleteAll();
  }

  @Transactional
  public void deleteByPartnerId(String partnerId) {
    repository.deleteByPartnerId(partnerId);
  }
}
