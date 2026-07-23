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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/*
 * https://www.baeldung.com/guide-to-okhttp
 * https://www.baeldung.com/okhttp-self-signed-cert
 * https://github.com/square/okhttp/tree/master/samples/guide/src/main/java/okhttp3/recipes
 */
@Service
@Scope("prototype")
public class AppApiAccessByOkhttp extends RestApiAccessByOkhttp {

  @Autowired
  public AppApiAccessByOkhttp(DataConverter dataConverter) {
    super(dataConverter);
  }

  @Value("${spring.security.user.name:admin}")
  private String username;
  @Value("${spring.security.user.password:password}")
  private String password;
  @Value("${dataspaceapp4edi.api-access.page.size:0}")
  protected int pageSize = 0; // 0 ==> we don't use pagination
  protected int pageNumber = 0;

  @Override
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  @Override
  public int getPageNumber() {
    return pageNumber;
  }
}
