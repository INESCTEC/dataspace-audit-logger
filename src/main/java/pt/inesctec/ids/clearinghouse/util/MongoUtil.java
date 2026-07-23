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

import ids.messaging.util.IdsMessageUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MongoUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoUtil.class);


  public static String hash(DBAbstract messageToHash){
    try {
      return IdsMessageUtils.hash(MessageDigest.getInstance("SHA-256"), messageToHash.toHashString());
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidValueException(e.getMessage());
    }
  }

  public static String hash(String messageToHash){
    try {
      return IdsMessageUtils.hash(MessageDigest.getInstance("SHA-256"), messageToHash);
    } catch (NoSuchAlgorithmException e) {
      throw new InvalidValueException(e.getMessage());
    }
  }

}
