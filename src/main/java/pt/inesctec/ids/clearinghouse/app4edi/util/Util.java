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

import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

/*
 * NOTE: Application sets the default timezone on startup
 *     TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
 */
public class Util {

  static public ZonedDateTime asSecs(ZonedDateTime dateTime) {
    return dateTime.truncatedTo(ChronoUnit.SECONDS);
  }

  static public ZonedDateTime nowAsSecs() {
    return ZonedDateTime.now().truncatedTo(ChronoUnit.SECONDS);
  }

  static public ZonedDateTime nowAsMillis() {
    //return ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS); DONT USE THIS FORMAT: causes the retrieval of entities in JPA to be different from the origin
    return ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
  }

  static public ZonedDateTime now() {
    //return ZonedDateTime.now(ZoneOffset.UTC); DONT USE THIS FORMAT: causes the retrieval of entities in JPA to be different from the origin
    return ZonedDateTime.now();
  }

  static public String nowAsIso8601String() {
    return zonedDateTime2Iso8601String(now());
  }

  static public boolean stringContainsSpecialCharacters(String str) {
    var pattern = Pattern.compile("[^a-zA-Z0-9-.,_]");
    var matcher = pattern.matcher(str);
    var stringContainsSpecialCharacter = matcher.find();
    return stringContainsSpecialCharacter;
  }

  //"yyyy-MM-ddTHH:mm:ss.SSSZ"
  static public String zonedDateTime2Iso8601String(ZonedDateTime date) {
    return date.format(DateTimeFormatter.ISO_INSTANT);
  }

  static public String zonedDateTime2FileName(ZonedDateTime date) {
    var res = date.format(DateTimeFormatter.ISO_INSTANT);
    return res.replace(':', '_'); // ':' comes from the time string
  }

  static public ZonedDateTime iso8601String2ZonedDateTime(String str) throws ParseException {
    try {
      return ZonedDateTime.parse(str, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
    catch (Throwable e) {
      // TODO 2022-06-26 don't know a better way to parse these data time strings sent by the Dataspace Connector 
      //   on the 'additional' property array (dateTime strings terminated by +0000 instead of Z)
      var newstr = str.replace("+0000", "Z");
      return ZonedDateTime.parse(newstr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
  }

  static public Instant iso8601String2Instant(String str) throws ParseException {
    return Instant.parse(str); // DateTimeFormatter.ISO_INSTANT
  }

  static public boolean isValidURL(String url) {
    try {
      URI.create(url).toURL();
      return true;
    }
    catch (Throwable e) {
      return false;
    }
  }

  static public boolean jsonFieldNotNull(JsonNode json, String fieldName) {
    return (json.get(fieldName) != null && !(json.get(fieldName) instanceof NullNode)) ? true : false;
  }

  static public boolean jsonFieldIsNull(JsonNode json, String fieldName) {
    return jsonFieldNotNull(json, fieldName) ? false : true;
  }

  static public boolean jsonTextFieldNotEmpty(JsonNode json, String fieldName) {
    return (jsonFieldNotNull(json, fieldName) && json.get(fieldName).asText().length() > 0) ? true : false;
  }

  static public boolean stringIsEmpty(String value) {
    return (value == null || value.length() == 0) ? true : false;
  }
}
