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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.stream.Collectors;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

public class ControllerUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerUtil.class);

  public static void printAllHeaders(HttpServletRequest request){
    Enumeration<String> headerNames = request.getHeaderNames();
    Map headers = Collections.list(headerNames)
            .stream()
            .collect(Collectors.toMap(
                    // Header name
                    name -> name,
                    // Header values
                    name -> Collections.list(request.getHeaders(name))
            ));
    headers.forEach((name, values) -> {
      System.out.println(name + ": " + values);
    });

  }

  public static void printImportantHeaders(HttpServletRequest request){
    System.out.println("RequestURL: "+  request.getRequestURL().toString());
    System.out.println("ServletPath: "+  request.getServletPath());
    System.out.println("Method: "+  request.getMethod());

  }

  public static void printParameters(HttpServletRequest request){
    Enumeration<String> params = request.getParameterNames();
    while(params.hasMoreElements()){
      String paramName = params.nextElement();
      System.out.println("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
    }

  }

  /**
   * Extract the header part from the HttpServletRequest.
   *
   * @param request The HttpServletRequest.
   * @return The ids header.
   * @throws IllegalArgumentException If the message is null.
   */
  public static String extractHeaderFromHttpServletRequest(final HttpServletRequest request) {
    try {
      //String header=request.getParameter(HEADER);
      Part headerP = request.getPart(HEADER);
      String header=IOUtils.toString(headerP.getInputStream(), StandardCharsets.UTF_8);
      //System.out.println("Header from request: "+ IOUtils.toString(headerP.getInputStream(), StandardCharsets.UTF_8));
      if(header.isEmpty() || header==null)
        throw new IllegalArgumentException("Value cannot be null.");
      else return header;
    } catch (IOException | ServletException e) {
      throw new IllegalArgumentException(e);
    }

  }

  /**
   * Extract the payload part from the HttpServletRequest.
   *
   * @param request The HttpServletRequest.
   * @return The ids payload.
   * @throws IllegalArgumentException If the message is null.
   */
  public static String extractPayloadFromHttpServletRequest(final HttpServletRequest request) {
    try {
      Part part = request.getPart(PAYLOAD);
      String payload=IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8);
      //System.out.println("Payload from request: "+ IOUtils.toString(part.getInputStream(), StandardCharsets.UTF_8));
      if(payload.isEmpty() || payload==null)
        throw new IllegalArgumentException("Value cannot be null.");
      else return payload;
    } catch (IOException | ServletException e) {
      throw new IllegalArgumentException(e);
    }

  }

}
