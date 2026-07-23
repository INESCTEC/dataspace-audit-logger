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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import pt.inesctec.ids.clearinghouse.app4edi.domain.ConsumedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.domain.ProvidedEdiArtifact;
import pt.inesctec.ids.clearinghouse.app4edi.exception.BadRequestException;
import pt.inesctec.ids.clearinghouse.app4edi.util.MyJacksonObjectMapper;

/*
 * Important helper classes: 
 *   org.apache.commons.io.FileUtils
 *   org.apache.commons.io.IOUtils
 */
@Service
@Scope("singleton")
public class DataConverter {

  private final ObjectMapper objectMapper = new MyJacksonObjectMapper();

  @Autowired
  public DataConverter() {
    super();
  }

  public ArrayNode newArrayNode() {
    return objectMapper.createArrayNode();
  }

  public ObjectNode newObjectNode() {
    return objectMapper.createObjectNode();
  }

  public ArrayNode newArrayNodeWithJsonNodes(JsonNode... objs) {
    var array = newArrayNode();
    for (JsonNode obj : objs)
      array.add(obj);
    return array;
  }

  public JsonNode string2Json(String jsonString) throws Exception {
    return objectMapper.readTree(jsonString);
  }

  public JsonNode file2Json(File jsonFile) throws Exception {
    return objectMapper.readTree(jsonFile);
  }

  public JsonNode file2Json(String fileName) throws Exception {
    try (var stream = new FileInputStream(fileName);) {
      return inputStream2Json(stream);
    }
  }

  public void json2File(String fileName, JsonNode json) throws IOException {
    jsonString2File(fileName, json.toPrettyString());
  }

  public void jsonString2File(String fileName, String fileContents) throws IOException {
    FileOutputStream outputFileStream = null;
    try {
      var file = new File(fileName);
      FileUtils.createParentDirectories(file);
      outputFileStream = new FileOutputStream(file);
      str2OutputStream(fileContents, outputFileStream, Charset.forName("UTF-8"));
    }
    finally {
      if (outputFileStream != null)
        outputFileStream.close();
    }
  }

  private void str2OutputStream(String s, OutputStream out, Charset charset) throws IOException {
    try (var writer = new OutputStreamWriter(out, charset)) {
      writer.write(s);
      // writeString(s, writer);
    }
  }

  public void sdf2File(String fileName, String fileContents) throws IOException {
    try (var outputFileStream = new FileOutputStream(new File(fileName))) {
      str2OutputStream(fileContents, outputFileStream, Charset.forName("UTF-8"));
    }
  }

  public void byteArray2File(String fileName, byte[] bytes) throws IOException {
    try (var stream = new FileOutputStream(new File(fileName));) {
      stream.write(bytes);
    }
  }

  public void byteArray2File(File file, byte[] bytes) throws IOException {
    try (var stream = new FileOutputStream(file);) {
      stream.write(bytes);
    }
  }

  public String file2String(String fileName) throws Exception {
    return FileUtils.readFileToString(new File(fileName), "UTF-8");
  }

  public InputStream file2InputStream(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }

  public String inputStream2String(InputStream stream) throws Exception {
    try {
      return IOUtils.toString(stream, "UTF-8");
    }
    finally {
      stream.close();
    }
  }

  public InputStream string2InputStream(String string) throws Exception {
    return IOUtils.toInputStream(string, "UTF-8");
  }

  public byte[] file2ByteArray(String fileName) throws Exception {
    return FileUtils.readFileToByteArray(new File(fileName));
  }

  public JsonNode inputStream2Json(InputStream src) throws Exception {
    try {
      return objectMapper.readTree(src);
    }
    finally {
      src.close();
    }
  }

  public JsonNode object2Json(final Object data) throws Exception {
    return objectMapper.valueToTree(data);
  }

  public String object2String(Object obj) throws Exception {
    return objectMapper.writeValueAsString(obj);
  }

  public List<JsonNode> objectList2JsonList(final List<Object> objList) throws Exception {
    var list = new ArrayList<JsonNode>();
    for (Object obj : objList)
      list.add(object2Json(obj));
    return list;
  }

  public ArrayNode providedEdiArtifactList2JsonArray(final List<ProvidedEdiArtifact> objList) throws Exception {
    ArrayNode array = newArrayNode();
    for (Object obj : objList) {
      array.add(object2Json(obj));
    }
    return array;
  }

  public ArrayNode consumedEdiArtifactList2JsonArray(final List<ConsumedEdiArtifact> objList) throws Exception {
    ArrayNode array = newArrayNode();
    for (Object obj : objList) {
      array.add(object2Json(obj));
    }
    return array;
  }

  public List<ProvidedEdiArtifact> jsonArray2ProvidedEdiArtifactList2(final JsonNode array) throws Exception {
    List<ProvidedEdiArtifact> list = new ArrayList<ProvidedEdiArtifact>();
    Iterator<JsonNode> it = array.iterator();
    while (it.hasNext()) {
      JsonNode json = it.next();
      list.add(json2Object(ProvidedEdiArtifact.class, json));
    }
    return list;
  }

  public List<ConsumedEdiArtifact> jsonArray2ConsumedEdiArtifactList2(final JsonNode array) throws Exception {
    List<ConsumedEdiArtifact> list = new ArrayList<ConsumedEdiArtifact>();
    Iterator<JsonNode> it = array.iterator();
    while (it.hasNext()) {
      JsonNode json = it.next();
      list.add(json2Object(ConsumedEdiArtifact.class, json));
    }
    return list;
  }

  public <T> T json2Object(Class<T> type, JsonNode jsonNode) throws Exception {
    T obj = fromJson(jsonNode, type);
    if (obj != null)
      return obj;
    else
      throw new BadRequestException("Invalid Json data, expecting data to populate " + type);
  }

  @SuppressWarnings("unchecked")
  public <T> T string2Object(Class<T> type, String jsonString) throws Exception {
    if (type == String.class)
      return (T) jsonString;
    else
      return json2Object(type, string2Json(jsonString));
  }

  public <T> List<T> json2ObjectList(Class<T> type, JsonNode jsonNode) throws Exception {
    if (!jsonNode.isArray())
      throw new BadRequestException("Invalid Json data, expecting an array");

    List<T> list = new ArrayList<T>();

    for (JsonNode node : jsonNode) {
      T obj = fromJson(node, type);
      if (obj != null)
        list.add(obj);
      else
        throw new BadRequestException("Invalid Json data, expecting data to populate " + type);
    }

    return list;
  }

  public <A> A fromJson(JsonNode json, Class<A> clazz) throws Exception {
    return objectMapper.treeToValue(json, clazz);
  }

  public String xmlString2JsonString(String xml) {
    var xml1 = xml.replace("\n", "\\n");
    var xml2 = xml1.replace("\f", "\\f");
    var xml3 = xml2.replace("\r", "\\r");
    var xml4 = xml3.replace("\b", "\\b");
    var xml5 = xml4.replace("\t", "\\t");
    var xml6 = xml5.replace("\"", "\\\"");
    var xml7 = xml6.replace("/", "\\/");
    return xml7;
  }

  public String jsonString2XmlString(String jsonString) {
    var s1 = jsonString.replace("\\n", "\n");
    var s2 = s1.replace("\\f", "\f");
    var s3 = s2.replace("\\r", "\r");
    var s4 = s3.replace("\\b", "\b");
    var s5 = s4.replace("\\t", "\t");
    var s6 = s5.replace("\\\"", "\"");
    var s7 = s6.replace("\\/", "/");
    return s7;
  }

  /* String */
  public JsonNode stringArrayToJson(String[] stringArray) {
    var json = newArrayNode();
    for (String name : stringArray)
      json.add(name);
    return json;
  }

  public List<String> json2StringList(JsonNode json) {
    var stringArray = new ArrayList<String>();

    Iterator<JsonNode> it = json.elements();
    while (it.hasNext()) {
      JsonNode e = it.next();
      stringArray.add(e.asText());
    }

    return stringArray;
  }

  public JsonNode stringListToJson(List<String> stringList) {
    var json = newArrayNode();
    for (String name : stringList)
      json.add(name);
    return json;
  }

  /* double */
  public JsonNode doubleArrayToJson(double[] doubleArray) {
    var json = newArrayNode();
    for (double value : doubleArray)
      json.add(value);
    return json;
  }

  public double[] json2DoubleArray(JsonNode json) {
    double[] doubleArray = new double[json.size()];

    Iterator<JsonNode> it = json.elements();
    int i = 0;
    while (it.hasNext()) {
      JsonNode e = it.next();
      doubleArray[i++] = e.asDouble();
    }

    return doubleArray;
  }

  public JsonNode doubleListToJson(List<Double> doubleList) {
    var json = newArrayNode();
    for (double value : doubleList)
      json.add(value);
    return json;
  }

}
