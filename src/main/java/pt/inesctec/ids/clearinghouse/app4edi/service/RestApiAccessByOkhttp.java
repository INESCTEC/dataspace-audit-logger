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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.tls.HandshakeCertificates;
import pt.inesctec.ids.clearinghouse.app4edi.exception.HttpRequestException;

/*
 * https://www.baeldung.com/guide-to-okhttp
 * https://www.baeldung.com/okhttp-self-signed-cert
 * https://github.com/square/okhttp/tree/master/samples/guide/src/main/java/okhttp3/recipes
 */
@Service
@Scope("prototype")
public abstract class RestApiAccessByOkhttp {

  private final DataConverter dataConverter;

  @Autowired
  public RestApiAccessByOkhttp(DataConverter dataConverter) {
    super();
    this.dataConverter = dataConverter;
  }

  private Response response; // response of every HTTP call
  private byte[] bodyAsBytes; // body of every HTTP call

  public abstract void setUsername(String username);
  public abstract void setPassword(String password);
  public abstract void setPageSize(int pageSize);
  public abstract void setPageNumber(int pageNumber);
  public abstract String getUsername();
  public abstract String getPassword();
  public abstract int getPageSize();   // if getPageSize() is > 0 ==> pagination is used (parameters 'number' and 'size') 
  public abstract int getPageNumber();

  public static final MediaType OCTECT = MediaType.parse("application/octet-stream");

  private OkHttpClient client() {
    HandshakeCertificates certificates = MyHandshakeCertificates.forTests();
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager())
      .hostnameVerifier(hostnameVerifier())
      .readTimeout(50, TimeUnit.SECONDS)             // maximum time of inactivity between two data packets when waiting for the server's response
      .writeTimeout(50, TimeUnit.SECONDS);           // maximum time of inactivity between two data packets when sending the request to the server
    return builder.build();
  }

  private HostnameVerifier hostnameVerifier() {
    return new HostnameVerifier() {
      @Override
      public boolean verify(String hostname, SSLSession session) {
        return true;
      }
    };
  }

  /*
   * Run GET Request
   */
  public void runGetRequest(String url) throws Exception {
    if (getPageSize() != 0 && !url.contains("size=") && !url.contains("page="))
      url = UriComponentsBuilder.fromUriString(url).queryParam("page", getPageNumber()).queryParam("size", getPageSize()).build().toString();

    OkHttpClient client = client();

    Request request = new Request.Builder().url(url).header("Authorization", Credentials.basic(getUsername(), getPassword())).get().build();

    response = client.newCall(request).execute();
    bodyAsBytes = response.body().bytes();
  }

  public void checkRunGetRequestResponse() throws Exception {
    assertResponseIsOkAndHasBody();
  }

  /*
   * Run POST Request
   */
  public void runPostRequest(String url, String body, String contentType) throws Exception {
    OkHttpClient client = client();

    RequestBody requestBody = RequestBody.create(body, MediaType.parse(contentType));
    Request request = new Request.Builder().url(url).header("Authorization", Credentials.basic(getUsername(), getPassword())).post(requestBody).build();

    response = client.newCall(request).execute();
    bodyAsBytes = response.body().bytes();
  }

  // same as previous but accepts a body with File
  public void runPostRequest(String url, File body) throws Exception {
    OkHttpClient client = client();

    RequestBody requestBody = RequestBody.create(body, OCTECT);
    Request request = new Request.Builder().url(url).header("Authorization", Credentials.basic(getUsername(), getPassword())).post(requestBody).build();

    response = client.newCall(request).execute();
    bodyAsBytes = response.body().bytes();
  }

  public void checkRunPostRequestResponse() throws Exception {
    assertResponseIsCreated();
  }

  /*
   * Run PUT Request
   */
  public void runPutRequest(String url, String body, String contentType) throws Exception {
    OkHttpClient client = client();

    RequestBody requestBody = RequestBody.create(body, MediaType.parse(contentType));
    Request request = new Request.Builder().url(url).header("Authorization", Credentials.basic(getUsername(), getPassword())).put(requestBody).build();

    response = client.newCall(request).execute();
    bodyAsBytes = response.body().bytes();
  }

  // same as previous but accepts a body with File
  public void runPutRequest(String url, File body) throws Exception {
    OkHttpClient client = client();

    RequestBody requestBody = RequestBody.create(body, OCTECT);
    Request request = new Request.Builder().url(url).header("Authorization", Credentials.basic(getUsername(), getPassword())).put(requestBody).build();

    response = client.newCall(request).execute();
    bodyAsBytes = response.body().bytes();
  }

  public void checkRunPutRequestResponse() throws Exception {
    assertResponseIsCreated();
  }

  /*
   * Run DELETE Request
   */
  public void runDeleteRequest(String url) throws Exception {
    OkHttpClient client = client();

    Request request = new Request.Builder().url(url).header("Authorization", Credentials.basic(getUsername(), getPassword())).delete().build();

    response = client.newCall(request).execute();
    bodyAsBytes = response.body().bytes();
  }

  public void checkRunDeleteRequestResponse() throws Exception {
    assertResponseIsDeleted();
  }

  /*
   * Utilities
   */

  public boolean bodyIsJson() {
    String value = response.header("content-type");
    return value != null ? value.contains("application/json") : false;
  }

  public JsonNode getBodyAsJson() throws IOException, Exception {
    String bodyAsString = new String(bodyAsBytes, "UTF-8");
    return dataConverter.string2Json(bodyAsString);
  }
  
  public int getResponseStatus() {
    return response.code();
  }

  public Response getResponse() {
    return response;
  }

  public byte[] getAttachment() {
    return bodyAsBytes;
  }

  public String getAttachmentAsString() throws UnsupportedEncodingException {
    return new String(bodyAsBytes, "UTF-8");
  }

  public byte[] getBodyAsByteArray() throws IOException {
    return bodyAsBytes;
  }

  private String printBody(String part1) {
    return String.format("%s %s %s", part1, getStatusCodeStr(response.code()), (response.body() != null) ? ("Body: " + response.body()) : ("Body is also empty"));
  }

  private String getStatusCodeStr(int status) {
    return HttpStatus.valueOf(status).toString();
  }

  public boolean responseIsOk() {
    return response.code() == HttpStatus.OK.value() ? true : false;
  }

  public boolean responseIsCreated() {
    return response.code() == HttpStatus.CREATED.value() ? true : false;
  }

  public boolean responseIsInternalServerError() {
    return response.code() == HttpStatus.INTERNAL_SERVER_ERROR.value() ? true : false;
  }

  public boolean responseIsNoContent() {
    return response.code() == HttpStatus.NO_CONTENT.value() ? true : false;
  }

  public boolean responseIsBadRequest() {
    return response.code() == HttpStatus.BAD_REQUEST.value() ? true : false;
  }

  public void assertResponseIsOk() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsOk())
      throw new HttpRequestException(printBody("Expected OK as response, got "));
  }

  public void assertResponseIsNoContent() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsNoContent())
      throw new HttpRequestException(printBody("Expected NO_CONTENT as response, got "));
  }

  public void assertResponseIsCreated() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsCreated())
      throw new HttpRequestException(printBody("Expected CREATED as response, got "));
  }

  public void assertResponseIsInternalServerError() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsInternalServerError())
      throw new HttpRequestException(printBody("Expected INTERNAL_SERVER_ERROR as response, got "));
  }

  public void assertResponseIsOkAndHasBody() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsOk())
      throw new HttpRequestException(printBody("Expected OK as response, got "));
    assertThat(response.body()).isNotNull();
  }

  public void assertResponseIsCreatedAndHasBody() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsCreated())
      throw new HttpRequestException(printBody("Expected CREATED as response, got "));
    assertThat(response.body()).isNotNull();
  }

  public void assertResponseIsDeleted() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsNoContent())
      throw new HttpRequestException(printBody("Expected NO_CONTENT as response, got "));
  }

  public void assertResponseIsBadRequest() throws Exception {
    assertThat(response).isNotNull();
    if (!responseIsBadRequest())
      throw new HttpRequestException(printBody("Expected BAD_REQUESTED as response, got "));
  }

}
