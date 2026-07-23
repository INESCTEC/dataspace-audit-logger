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
/* Copyright (C) [2021-2023], INESC TEC
 *
 * DataSpaceApp4EDI: Implements data exchange between different organizations in 
 * accordance with the International Data Space Reference Architecture
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
package pt.inesctec.ids.clearinghouse.app4edi.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Index;

import lombok.NoArgsConstructor;
import lombok.ToString;

/*
 * https://www.baeldung.com/jpa-indexes
 * https://www.baeldung.com/jpa-unique-constraints
 * 
 * https://docs.jboss.org/hibernate/orm/6.6/introduction/html_single/Hibernate_Introduction.html#entities
 * 
 * Entity classes should override equals() and hashCode(), especially when associations are represented as sets.
 */

@Entity
@Table(
  name = "provided", 
  uniqueConstraints = { 
    @UniqueConstraint(name = "t1_domain_pk", columnNames = { "partner_id", "to_partner_id", "doc_title" }) 
  },
  indexes = { 
    @Index(name = "t1_idx1", columnList = "partner_id, status"), 
    @Index(name = "t1_idx2", columnList = "partner_id, status, to_partner_id ASC"), 
    @Index(name = "t1_idx3", columnList = "partner_id, status, to_partner_id ASC, created_time ASC"),
    @Index(name = "t1_idx4", columnList = "partner_id, status, to_partner_id ASC, published_time ASC"),
    @Index(name = "t1_idx5", columnList = "partner_id, status, to_partner_id ASC, consumed_time ASC"),
    @Index(name = "t1_idx6", columnList = "partner_id, status, to_partner_id ASC, consumed_and_archived_time ASC")
  }
)
@NoArgsConstructor
@ToString
public class ProvidedEdiArtifact {

  static public final String STATUS_CREATED = "created"; // created by the POST controller
  static public final String STATUS_REJECTED = "rejected"; // rejected by the POST controller
  static public final String STATUS_PUBLISHED = "published"; // published in the local connector
  static public final String STATUS_CONSUMED = "consumed"; // consumed by remote connector
  static public final String STATUS_CONSUMED_AND_ARCHIVED = "consumed&archived"; // consumed by remote connector and archived on own connector

  @Id
  public Long id;

  @Column(name = "partner_id", nullable = false, updatable = false)
  public String partnerId;

  @Column(name = "status", nullable = false)
  public String status;

  @Column(name = "to_partner_id", nullable = false, updatable = false)
  public String toPartnerId;

  @Column(name = "doc_title", nullable = false, updatable = false)
  public String docTitle;

  @Column(name = "doc_schema_name", nullable = true, updatable = false)
  public String docSchemaName;

  @Column(name = "doc_schema_location_url", nullable = true, updatable = false)
  public String docSchemaLocationUrl;

  @Column(name = "doc_size", nullable = true, updatable = false)
  public Long docSize;

  @Column(name = "file_path", nullable = false, updatable = true)
  public String filePath;

  @Column(name = "created_time", nullable = false, updatable = false)
  public ZonedDateTime createdTime;

  @Column(name = "rejected_time", nullable = true, updatable = false)
  public ZonedDateTime rejectedTime;

  @Column(name = "rejected_reason", nullable = true, updatable = false, length = 1024)
  public String rejectedReason;

  @Column(name = "published_time", nullable = true, updatable = true)
  public ZonedDateTime publishedTime;

  @Column(name = "consumed_time", nullable = true, updatable = true)
  public ZonedDateTime consumedTime;

  @Column(name = "consumed_and_archived_time", nullable = true, updatable = true)
  public ZonedDateTime consumedAndArchivedTime;

  public ProvidedEdiArtifact(Long id, String partnerId, String toPartnerId, String docTitle, ZonedDateTime createdTime, String filePath, Long fileLenght) {
    this.id= id;
    this.partnerId = partnerId;
    this.toPartnerId = toPartnerId;
    this.docTitle = docTitle;
    this.docSize = fileLenght;
    this.filePath = filePath;
    this.createdTime = createdTime;
    this.status = STATUS_CREATED;
  }

  public boolean statusIsCreated() {
    return status.equals(STATUS_CREATED);
  }

  public boolean statusIsRejected() {
    return status.equals(STATUS_REJECTED);
  }

  public boolean statusIsPublished() {
    return status.equals(STATUS_PUBLISHED);
  }

  public boolean statusIsConsumed() {
    return status.equals(STATUS_CONSUMED);
  }

  public boolean statusIsConsumedAndArchived() {
    return status.equals(STATUS_CONSUMED_AND_ARCHIVED);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ProvidedEdiArtifact))
      return false;
    return id != null && id.equals(((ProvidedEdiArtifact) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
