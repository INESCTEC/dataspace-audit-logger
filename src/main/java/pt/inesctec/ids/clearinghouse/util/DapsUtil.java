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

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import ids.messaging.core.daps.ConnectorMissingCertExtensionException;
import ids.messaging.core.daps.DapsConnectionException;
import ids.messaging.core.daps.DapsEmptyResponseException;
import ids.messaging.core.daps.DapsTokenProvider;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;


public class DapsUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(DapsUtil.class);


  public static DynamicAttributeToken getDat(DapsTokenProvider dapsTokenProvider){
    try {
      return dapsTokenProvider.getDAT();
    } catch (ConnectorMissingCertExtensionException | DapsConnectionException | DapsEmptyResponseException e) {
      throw new RuntimeException(e);
    }
  }


  public static Map<String, JSONObject> decodeDAT(String dat_token){

    Map<String, JSONObject> decodedJWT=new HashMap<>();
    String[] chunks = dat_token.split("\\.");
    Base64.Decoder decoder = Base64.getUrlDecoder();

    decodedJWT.put(HEADER, new JSONObject(new String(decoder.decode(chunks[0]))));
    decodedJWT.put(PAYLOAD, new JSONObject(new String(decoder.decode(chunks[1]))));

    //System.err.println(decodedJWT.toString());
    return decodedJWT;
  }

  public static String getClientFromDAT(DynamicAttributeToken dat){
    Map<String, JSONObject> decodedDAT=DapsUtil.decodeDAT(dat.getTokenValue());
    String client=decodedDAT.get(PAYLOAD).get("sub").toString();
    //System.err.println(client);

    return client;
  }

}
