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
package pt.inesctec.ids.clearinghouse.util;

import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.DapsTokenProvider;
import ids.messaging.util.IdsMessageUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.inesctec.ids.clearinghouse.dto.UsageNotificationDto;
import pt.inesctec.ids.clearinghouse.exceptions.BadRequestException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;


public class LogUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogUtil.class);

  public static String buildLogPayload(String value) {
    Map<String, String> payload=new HashMap<>();
    payload.put("data", value);
    JSONObject jsonObject=new JSONObject(payload);
    //System.err.println(jsonObject.toString());
    return jsonObject.toString();
  }

  public static String buildProcessorPayload(String value) {
    Map<String, String> payload=new HashMap<>();
    payload.put("pid", value);
    JSONObject jsonObject=new JSONObject(payload);
    return jsonObject.toString();
  }

  public static List<String> extractOwnersFromPayload(String owners){
    List<String> ownersInList=new ArrayList<>();

    // Regular expression to match strings enclosed in double quotes
    Pattern pattern = Pattern.compile("\"([^\"]*)\"");
    Matcher matcher = pattern.matcher(owners);

    // Find all matches and add them to the result list
    while (matcher.find()) {
      ownersInList.add(matcher.group(1));
    }

    // Remove the first record (it is "owners")
    ownersInList.remove("owners");
    return ownersInList;
  }

  public static Map<String, String> parsePayload(String payload){
    Map<String, String> payloadAsMap=new HashMap<>();

    // Regular expression to separate by comma (,)
    Pattern pattern = Pattern.compile("(\\w+)=([^,}]+)");
    Matcher matcher = pattern.matcher(payload);

    // Find all matches and add them to the result list
    while (matcher.find()) {
      payloadAsMap.put(matcher.group(1), matcher.group(2));
    }

    /*UsageNotificationDto usageNotificationDto=new UsageNotificationDto(parsedPayload.get("issuerConnector"),
                parsedPayload.get("accessed"), parsedPayload.get("target"));*/
    return payloadAsMap;
  }

}
