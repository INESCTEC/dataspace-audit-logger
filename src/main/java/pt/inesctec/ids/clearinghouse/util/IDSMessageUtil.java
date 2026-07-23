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

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import ids.messaging.common.SerializeException;
import ids.messaging.protocol.multipart.parser.MultipartDatapart;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpEntity;
import pt.inesctec.ids.clearinghouse.exceptions.NullValueException;

import java.io.*;
import java.util.*;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

public class IDSMessageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(IDSMessageUtil.class);

    /**
     * Extract the header part from a Map from the HttpEntity.
     *
     * @param message The ids response message as map.
     * @return The ids header.
     * @throws IllegalArgumentException If the message is null.
     */
    public static String extractHeaderFromMultipartMessage(final Map<String, Object> message) {
        if(message!=null)
            return message.get("header").toString();
        else throw new NullValueException("value cannot be null.");
    }

    /**
     * Extract the payload part from the ids messaging services response.
     *
     * @param message The ids response message as map.
     * @return The ids payload.
     * @throws IllegalArgumentException If the message is null.
     */
    public static String extractPayloadFromMultipartMessage(final Map<String, Object> message) {
        if(message!=null)
            return message.get("payload").toString();
        else throw new NullValueException("value cannot be null.");
    }


    public static byte[] buildByteResponse(HttpEntity responseEntity){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            responseEntity.writeTo(out);
            byte[] bytes = out.toByteArray();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //InputStream inputStream = new ByteArrayInputStream(out.toByteArray());

    }
}
