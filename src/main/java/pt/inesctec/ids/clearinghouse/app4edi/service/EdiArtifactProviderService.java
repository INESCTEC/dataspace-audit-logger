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
import pt.inesctec.ids.clearinghouse.app4edi.domain.ProvidedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.domain.repository.ProvidedEdiArtifactRepository;

@Service
@Slf4j
public class EdiArtifactProviderService {

  private final ProvidedEdiArtifactRepository repository;

  @Autowired
  public EdiArtifactProviderService(ProvidedEdiArtifactRepository repository) {
    super();
    this.repository = repository;
  }

  @Transactional
  public ProvidedEdiArtifact create(ProvidedEdiArtifact providedEdiArtifact) {
    return repository.save(providedEdiArtifact);
  }

  @Transactional
  public ProvidedEdiArtifact update(ProvidedEdiArtifact providedEdiArtifact) {
    return repository.save(providedEdiArtifact);
  }

  public Optional<ProvidedEdiArtifact> findById(Long id) {
    return repository.findById(id);
  }

  public Optional<ProvidedEdiArtifact> findByPartnerIdAndtoPartnerIdAndDocTitle(String partnerId, String toPartnerId, String docTitle) {
    return repository.findByPartnerIdAndToPartnerIdAndDocTitle(partnerId, toPartnerId, docTitle);
  }

  public List<ProvidedEdiArtifact> findAll() {
    var list = new ArrayList<ProvidedEdiArtifact>();
    list.addAll(repository.findAll());
    return list;
  }

  public List<ProvidedEdiArtifact> findAll(String partnerId) {
    var list = new ArrayList<ProvidedEdiArtifact>();
    list.addAll(repository.findByPartnerId(partnerId));
    return list;
  }

  public List<ProvidedEdiArtifact> findByPartnerId(String partnerId) {
    return repository.findByPartnerId(partnerId);
  }

  public List<ProvidedEdiArtifact> findByStatusCreated(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByCreatedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_CREATED);
  }

  public List<ProvidedEdiArtifact> findByStatusRejected(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByCreatedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_REJECTED);
  }

  public Page<ProvidedEdiArtifact> findByStatusRejected(String partnerId, Pageable pageable) {
    return repository.findByPartnerIdAndStatusOrderByCreatedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_REJECTED, pageable);
  }

  public List<ProvidedEdiArtifact> findByStatusPublished(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByPublishedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_PUBLISHED);
  }

  public Page<ProvidedEdiArtifact> findByStatusPublished(String partnerId, Pageable pageable) {
    return repository.findByPartnerIdAndStatusOrderByPublishedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_PUBLISHED, pageable);
  }

  public List<ProvidedEdiArtifact> findByStatusConsumed(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByConsumedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_CONSUMED);
  }

  public Page<ProvidedEdiArtifact> findByStatusConsumed(String partnerId, Pageable pageable) {
    return repository.findByPartnerIdAndStatusOrderByConsumedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_CONSUMED, pageable);
  }

  public List<ProvidedEdiArtifact> findByStatusPublishedToPartner(String partnerId, String toPartnerId) {
    return repository.findByPartnerIdAndToPartnerIdAndStatusOrderByPublishedTimeAsc(partnerId, toPartnerId, ProvidedEdiArtifact.STATUS_PUBLISHED);
  }

  public Page<ProvidedEdiArtifact> findByStatusPublishedToPartner(String partnerId, String toPartnerId, Pageable pageable) {
    return repository.findByPartnerIdAndToPartnerIdAndStatusOrderByPublishedTimeAsc(partnerId, toPartnerId, ProvidedEdiArtifact.STATUS_PUBLISHED, pageable);
  }

  public List<ProvidedEdiArtifact> findByStatusConsumedAndArchived(String partnerId) {
    return repository.findByPartnerIdAndStatusOrderByConsumedAndArchivedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_CONSUMED_AND_ARCHIVED);
  }

  public Page<ProvidedEdiArtifact> findByStatusConsumedAndArchived(String partnerId, Pageable pageable) {
    return repository.findByPartnerIdAndStatusOrderByConsumedAndArchivedTimeAsc(partnerId, ProvidedEdiArtifact.STATUS_CONSUMED_AND_ARCHIVED, pageable);
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
