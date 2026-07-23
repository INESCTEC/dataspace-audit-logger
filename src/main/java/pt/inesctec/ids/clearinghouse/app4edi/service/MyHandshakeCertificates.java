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

import java.security.cert.X509Certificate;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;

/*
 * We are using the same certificates as stored on 'play-ahc-ws-application-itest.conf' and 'play-ahc-ws-application.conf'
 */
public final class MyHandshakeCertificates {
  
  // PEM files for root certificates
  
  static final X509Certificate ids_broker_CertificateAuthority = Certificates.decodeCertificatePem(""
      + "-----BEGIN CERTIFICATE-----\n"
      + "MIIDwDCCAqigAwIBAgIBCDANBgkqhkiG9w0BAQwFADBMMQswCQYDVQQGEwJQVDES\n"
      + "MBAGA1UECgwJSU5FU0MgVEVDMSkwJwYDVQQDDCBJRFMgSU5FU0NURUMgU1RWIFRl\n"
      + "c3QgU3ViQ0EgMjAyMjAeFw0yMjA3MDUwOTQ0MTNaFw0yNTA3MDQwOTQ0MTNaMF4x\n"
      + "CzAJBgNVBAYTAlBUMQ8wDQYDVQQKDAZCUk9LRVIxDzANBgNVBAsMBkJST0tFUjEt\n"
      + "MCsGA1UEAwwkYzNhNTc0OTUtNzdhZS00YjVlLThlNjAtNzgzYTQ4MDQ0MjhmMIIB\n"
      + "IjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxczMQBZyCCMyF05Nb/kkc6ci\n"
      + "1kVWn/2F0+gtok6PqXJBX68S0PqR8CnEVtXh8bYDmWeaJFGMzkofXo0bw+upMSQd\n"
      + "+i7znbE7keG/QoSz/OhbAb9AzlI10DKLT0GbulvulibdPuIUEGnX2w1It7zD5gDt\n"
      + "n/IY/dXaiAQZmkj9LNwAgcr/BvgyWRz+FUUzNG1GU3q4wClNTFO4cNEQnWYMhSYq\n"
      + "jWLLITBwhX7+OnfLqtbkdVhd6ZTpsFtcFkpk94pgXkRpaGDT8nfVWGgxCpaGXl7C\n"
      + "MHvN7uFBp/tLImISwuA5oARS5kqSLBZDdKT1fQqj781KyzkNK/P4pW9y6kgGXQID\n"
      + "AQABo4GaMIGXMAkGA1UdEwQCMAAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUF\n"
      + "BwMCMAsGA1UdDwQEAwIDqDAdBgNVHQ4EFgQUy9nuhe3VArsVy04S1RbuCoxKlnow\n"
      + "HwYDVR0jBBgwFoAUJ6jWaeYlR7oJjZjl33k/CYn0TYMwHgYDVR0RBBcwFYITdmNl\n"
      + "c2UxOS5pbmVzY3RlYy5wdDANBgkqhkiG9w0BAQwFAAOCAQEAR1UeIvYpdbQ7eetM\n"
      + "+C3OeL4MXiwdkS5GUxAf1H0UxTZVGe/1jMxqmGAA7w/ZdMUnVY20j3Ahgj7mlmoM\n"
      + "5XxqAAfpm7/Sgp9VJtAkq9Do9NN7Etxcmp4I9yOOkwnUtvDXFlQlKv5L6ICexnEp\n"
      + "89hWlFY0v+VkoDZpCBkxsiZYLVCxfMdfD5Vv6o22vFqWFdnt5nEXFJnoq0h8CIsl\n"
      + "xZjuLV1ddiPG5+obGFrxTcPLpa/oruE5XZALb7fwCxcoKVCI3xUHHxdKdUGiz8eD\n"
      + "cfjj1lx0jU85hXmCEKRWcvoIACIB0o6/fORU0AR2cWhIXRYjYxuakbltjdmD1CeE\n"
      + "EqC53Q==\n"
      + "-----END CERTIFICATE-----\n");

  static final X509Certificate ids_root_test_ca_2018_CertificateAuthority = Certificates.decodeCertificatePem(""
      + "-----BEGIN CERTIFICATE-----\n"
      + "MIIDZzCCAk+gAwIBAgIJALIB7y7FZtiHMA0GCSqGSIb3DQEBBQUAMEIxCzAJBgNV\n"
      + "BAYTAkRFMRMwEQYDVQQKDApGcmF1bmhvZmVyMR4wHAYDVQQDDBVJRFMgUm9vdCBU\n"
      + "ZXN0IENBIDIwMTgwHhcNMTgxMDMxMTYxMjAyWhcNMzgxMDI2MTYxMjAyWjBCMQsw\n"
      + "CQYDVQQGEwJERTETMBEGA1UECgwKRnJhdW5ob2ZlcjEeMBwGA1UEAwwVSURTIFJv\n"
      + "b3QgVGVzdCBDQSAyMDE4MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\n"
      + "sh2FI432kI1BYWCs1CBPgJakTBsQt/BP7DQJ2lULlBKhtJpJM85tD1dXgIhLoBFi\n"
      + "5cMQPCX8kEy7TQLEOB4pPvqvUk+Tc48bNA39F4p3nOLZzlYREaTHui9r+FDvHy7j\n"
      + "TJGuRuDNKbP8qOAAd8Ci63e9iabSTL1wXLuZFnkEEHKraCwUQhzck5Dk7ll5Vncw\n"
      + "StishI+zk47uOiM3v0yMO4TSRyxFvwKfHB09hKLbLg6fr+62IqLEQWU/r0qo9h8e\n"
      + "4N16HdXLojyTam36JPDfExB2yHwMAe5fqtzwknWy0VfkrI2+KjN004EmjhsD9tL0\n"
      + "dKS+MSRARDQYuHenSpG6XQIDAQABo2AwXjAdBgNVHQ4EFgQUl+fsV0kEi4xmZ+NX\n"
      + "uXYPdqDwY5AwHwYDVR0jBBgwFoAUl+fsV0kEi4xmZ+NXuXYPdqDwY5AwDwYDVR0T\n"
      + "AQH/BAUwAwEB/zALBgNVHQ8EBAMCAUYwDQYJKoZIhvcNAQEFBQADggEBAIgDI5Zq\n"
      + "xvrI0ihxtgdnr/p7Nx71imoEIquGDWR+W9smzUGcY5zkAX9V2zm/vydNhRrA3iNn\n"
      + "/+bmsM/xUYk4J6Oju8czYE1EQ/43OUB3iODzdzdAUspaa4MQORi0qaHehLrOzf28\n"
      + "RVDPzLhhy64Z7muvhiKlWlwlLMqJSKIrJpeCSYTQZJFLLvu2atktoFJok477z1cX\n"
      + "ZpV2P1ODUyVETlA2WpOkb1g+BeaboDcODV3lwHA0kToDnjCeVo1rzs9ghjIpxvmg\n"
      + "gWMVSvLUgHTqUq1ruyqg9dJ7kN4rxpmTYBhVzYueVCfaRNQ+kKkUOyNEIAH9/c2g\n"
      + "lmTm5QRelsvLnbk=\n"
      + "-----END CERTIFICATE-----\n");

  static final X509Certificate ids_inesctec_root_test_ca_2022_CertificateAuthority = Certificates.decodeCertificatePem(""
    + "-----BEGIN CERTIFICATE-----\n"
    + "MIIDrjCCApagAwIBAgIBADANBgkqhkiG9w0BAQ0FADBKMQswCQYDVQQGEwJQVDES\n"
    + "MBAGA1UECgwJSU5FU0MgVEVDMScwJQYDVQQDDB5JRFMgSU5FU0NURUMgUm9vdCBU\n"
    + "ZXN0IENBIDIwMjIwHhcNMjIwNzA0MTQ0MTQ1WhcNMzIwNzAxMTQ0MTQ1WjBKMQsw\n"
    + "CQYDVQQGEwJQVDESMBAGA1UECgwJSU5FU0MgVEVDMScwJQYDVQQDDB5JRFMgSU5F\n"
    + "U0NURUMgUm9vdCBUZXN0IENBIDIwMjIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw\n"
    + "ggEKAoIBAQDbpyO0BZFQVXIRkPyUm2CbjDv8luH7T/82Gn4uIX6vka7UVd14C518\n"
    + "PDqi8aORDZpQ/5VaXkiFRdITEuZ1f1Pui9lQSOduBC8BmcOvNRLMxExe8Tk0c4eF\n"
    + "T5Nn75Q3azv5z1F25UM28zQmlhg6ykU99j7WHxn7QLKw4Pz/bAu2ZeWFeb2Pb8b0\n"
    + "0EUmCHJv/imOeG7eOG+c2/xHvC+ADVQsTdmVO2VfuREEusHSYPoE+zkuJQd/HYdy\n"
    + "KOGKQyBboaZSo8UVHbSbOqVXrTIi90GApXW3M+hbDH8J0a2nWxWcTmgCmB1zGAap\n"
    + "f64On/OcTF0pCuliYTdSULZ+Z8jWRivHAgMBAAGjgZ4wgZswDwYDVR0TAQH/BAUw\n"
    + "AwEB/zALBgNVHQ8EBAMCAUYwXAYDVR0jBFUwU6FOpEwwSjELMAkGA1UEBhMCUFQx\n"
    + "EjAQBgNVBAoMCUlORVNDIFRFQzEnMCUGA1UEAwweSURTIElORVNDVEVDIFJvb3Qg\n"
    + "VGVzdCBDQSAyMDIyggEAMB0GA1UdDgQWBBR/f/me/+C+czh7AwhYYk2UcynpyzAN\n"
    + "BgkqhkiG9w0BAQ0FAAOCAQEAtKyueW85pVQm2pz8fp3xP31QKcKz5ISqg9hC9y4K\n"
    + "C1Al6Ugb/w2QrFMt8OGUr6lkfsoFm7WhWtm4YbwkC03+oH80GNqGIAIciwP7T5C0\n"
    + "PBjB7xs+U30XLCV7Oc0DIO1L5mEG4zZkjybLa+Mdaz+cmQzRy4vsILks/wJzQLa6\n"
    + "6nZrN51ttsFYSJALyQgpWC+n3t9xGnzht1qnkzG+fkVJBrRmFWsVP4R7v4HHPVkk\n"
    + "dJyqHNYIbw9PPEUSkjAm3LXUInW1B56VArqeQr6ZvhLvFGOY9Z7efpOJfTGwVPPO\n"
    + "NhJY+E+2dgpBoyHpfNErbxMPWqttvsnHuvZk52pIhQYzLw==\r\n"
    + "-----END CERTIFICATE-----\n");

  static final X509Certificate localhost_CertificateAuthority = Certificates.decodeCertificatePem(""
    + "-----BEGIN CERTIFICATE-----\n"
    + "MIIDlzCCAn+gAwIBAgIEMOWR4TANBgkqhkiG9w0BAQsFADBuMRAwDgYDVQQGEwdV\n"
    + "bmtub3duMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRAwDgYD\n"
    + "VQQKEwdVbmtub3duMRAwDgYDVQQLEwdVbmtub3duMRIwEAYDVQQDEwlsb2NhbGhv\n"
    + "c3QwHhcNMTkxMTIwMTA0NDEyWhcNMjkxMTE3MTA0NDEyWjBuMRAwDgYDVQQGEwdV\n"
    + "bmtub3duMRAwDgYDVQQIEwdVbmtub3duMRAwDgYDVQQHEwdVbmtub3duMRAwDgYD\n"
    + "VQQKEwdVbmtub3duMRAwDgYDVQQLEwdVbmtub3duMRIwEAYDVQQDEwlsb2NhbGhv\n"
    + "c3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCUgvKLKtLPKEgTsfb5\n"
    + "FGDgNh3NSLVR+5sSDdmhGQM1bYWp89eRpE0JKn0fr1/qaoG2jo2VSg6gnG5TvT7T\n"
    + "gae0Iagt0lTGlfrMk9SrWby2tx4Q0ZYxIQ9FMUpVMwaUN260ILD87RyEa2nsRvOO\n"
    + "EHvSFGQZ9dEfEdyti8vlquJ5GF68nJ5I3iDUBga5Gq0pEi+zMwltUsinEDnGa7Jj\n"
    + "+izwbu7NBX3hEFBpOm4M5lekRaJAegQj115+VKKFQSOkW3FxaqRsZQhlJ8Ydf+nQ\n"
    + "wGZqRxvHtL9At6Ag6slZhi3CKjJ3nCBiqIf34MmDa7Cu7jGYaL8SOSxyywbZ+/pE\n"
    + "FMsHAgMBAAGjPTA7MBoGA1UdEQQTMBGCCWxvY2FsaG9zdIcEfwAAATAdBgNVHQ4E\n"
    + "FgQUykS9rQM10rvPTZ80S63u0ehQBRIwDQYJKoZIhvcNAQELBQADggEBAIsMYYWh\n"
    + "f9ruq25ogi5uvOO0jE4c1Cri+Q2U1EbbBdgEMBMCQc46XDPe9QwL8ygpNSUl9VwM\n"
    + "mqExL4pAwfoqHK0sC3lpAn26FCJ7wedjd0v9/vKwGbEZMxlrUKjKmLPYNg+cK7Ge\n"
    + "UbG+uKZ2H9sZFajS1Txs17MCtf0/Ewt372tBrIJMNZFf5hiPSkysMClSxMuQxiPH\n"
    + "QhX6TON4W3I2HsjI/d868OQIMaNn3VipNj7Gkj2zjo2WBTP9C584YVmO5IiJ0RyT\n"
    + "hENcPy1dRA22UXUwHs/EEX5H83phXqDeaJ/9iPg7pYPD9g62r9wY/fht6hZ+G8Dl\n"
    + "dL9EQ8s/pWyAlyk=\n"
    + "-----END CERTIFICATE-----\n");

  // we are returning the same certificates as stored in file 'play-ahc-ws-application-itest.conf'
  static public HandshakeCertificates forTests() {
    return new HandshakeCertificates.Builder()
      .addTrustedCertificate(ids_broker_CertificateAuthority)
      .addTrustedCertificate(ids_root_test_ca_2018_CertificateAuthority)
      .addTrustedCertificate(ids_inesctec_root_test_ca_2022_CertificateAuthority)
      .addTrustedCertificate(localhost_CertificateAuthority)
      .addPlatformTrustedCertificates() // standard certificates
      .build();
  }

  // we are returning the same certificates as stored in file 'play-ahc-ws-application.conf'
  static public HandshakeCertificates forOperation() {
    return new HandshakeCertificates.Builder()
      .addTrustedCertificate(ids_root_test_ca_2018_CertificateAuthority)
      .addTrustedCertificate(ids_inesctec_root_test_ca_2022_CertificateAuthority)
      .addPlatformTrustedCertificates() // standard certificates
      .build();
  }
}