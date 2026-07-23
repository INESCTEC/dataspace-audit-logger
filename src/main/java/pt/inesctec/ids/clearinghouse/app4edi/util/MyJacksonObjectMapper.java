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
package pt.inesctec.ids.clearinghouse.app4edi.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/*
*
* Most {@code java.time} types are serialized as numbers (integers or decimals as appropriate) if the
* {@link com.fasterxml.jackson.databind.SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} feature is enabled, and otherwise are serialized in
* standard <a href="http://en.wikipedia.org/wiki/ISO_8601" target="_blank">ISO-8601</a> string representation. ISO-8601 specifies formats
* for representing offset dates and times, zoned dates and times, local dates and times, periods, durations, zones, and more. All
* {@code java.time} types have built-in translation to and from ISO-8601 formats.
*
*/

public class MyJacksonObjectMapper extends ObjectMapper {

  private static final long serialVersionUID = 1L;

  public MyJacksonObjectMapper() {
    super();
    registerModule(new Jdk8Module());
    registerModule(new JavaTimeModule());
    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
  
}
