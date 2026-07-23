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
  name = "consumed", 
  uniqueConstraints = { 
    @UniqueConstraint(name = "t2_domain_pk", columnNames = { "partner_id", "from_partner_id", "doc_title" }) 
  },
  indexes = {
    @Index(name = "t2_idx1", columnList = "partner_id, status"), 
    @Index(name = "t2_idx2", columnList = "partner_id, status, from_partner_id ASC"), 
    @Index(name = "t2_idx3", columnList = "partner_id, status, from_partner_id ASC, consumed_time ASC"),
    @Index(name = "t2_idx4", columnList = "partner_id, status, from_partner_id ASC, archived_time ASC")
  }
)
@NoArgsConstructor
@ToString
public class ConsumedEdiArtifact {

  static public final String STATUS_CONSUMED = "consumed"; // consumed == received
  static public final String STATUS_ARCHIVED = "archived";

  @Id
  public Long id;

  @Column(name = "partner_id", nullable = false, updatable = false)
  public String partnerId;

  @Column(name = "status", nullable = false, updatable = true)
  public String status;

  @Column(name = "from_partner_id", nullable = false, updatable = false)
  public String fromPartnerId;

  @Column(name = "doc_title", nullable = false, updatable = false)
  public String docTitle;

  @Column(name = "doc_schema_name", nullable = true, updatable = false)
  public String docSchemaName;

  @Column(name = "doc_schema_location_url", nullable = true, updatable = false)
  public String docSchemaLocationUrl;

  @Column(name = "doc_size", nullable = true, updatable = false)
  public Long docSize;

  @Column(name = "file_path", nullable = false)
  public String filePath;

  @Column(name = "consumed_time", nullable = false, updatable = false)
  public ZonedDateTime consumedTime;

  @Column(name = "archived_time", nullable = true, updatable = true)
  public ZonedDateTime archivedTime;

  public ConsumedEdiArtifact(Long id, String partnerId, String fromPartnerId, String docTitle, ZonedDateTime consumedTime, String filePath, Long docSize, String docSchemaName, String docSchemaLocationUrl) {
    this.id = id;
    this.partnerId = partnerId;
    this.fromPartnerId = fromPartnerId;
    this.docTitle = docTitle;
    this.docSize = docSize;
    this.docSchemaName = docSchemaName;
    this.docSchemaLocationUrl = docSchemaLocationUrl;
    this.filePath = filePath;
    this.consumedTime = consumedTime;
    this.status = STATUS_CONSUMED;
  }

  public boolean statusIsConsumed() {
    return status.equals(STATUS_CONSUMED);
  }

  public boolean statusIsArchived() {
    return status.equals(STATUS_ARCHIVED);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ConsumedEdiArtifact))
      return false;
    return id != null && id.equals(((ConsumedEdiArtifact) o).id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
