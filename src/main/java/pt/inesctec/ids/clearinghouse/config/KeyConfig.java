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
package pt.inesctec.ids.clearinghouse.config;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Resource;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;

@Configuration
public class KeyConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyConfig.class);

    @Value("${private.key.file}")
    private String keyPath;


    @Bean
    public PrivateKey privateKey()   {
        File derFile = null;
        try {
            derFile = ResourceUtils.getFile(keyPath);
        } catch (FileNotFoundException e) {
            throw new InvalidValueException("Key path is not valid. Error: "+e.getMessage());
        }

        // Tries to load a PKCS8 Key. It is the more common format
        try {
            return this.readPKCS8Key(derFile);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Tries to load a PKCS1 Key
            try {
                return this.readPKCS1Key(derFile);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read private key", e);
            }
        }
    }

    private PrivateKey readPKCS8Key(File file) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        // Fixes the error: algid-parse-error-not-a-sequence
        // https://stackoverflow.com/questions/6559272/algid-parse-error-not-a-sequence
        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );

        byte[] privKeyByteArray = Files.readAllBytes(file.toPath());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privKeyByteArray);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey= keyFactory.generatePrivate(keySpec);

        String PEM= DatatypeConverter.printBase64Binary(
                privateKey.getEncoded());
        //System.out.println("PEM: " + PEM);

        return privateKey;
    }

    // Taken from https://docs.hidglobal.com/dev/auth-service/buildingapps/java/read-different-certificate-key-file-formats-with-java.htm
    private PrivateKey readPKCS1Key(File file) throws IOException {
        Path path = Paths.get(file.getPath());
        byte[] privKeyByteArray = Files.readAllBytes(path);
        ASN1Sequence seq = ASN1Sequence.getInstance(privKeyByteArray);
        RSAPrivateKey bcPrivateKey = RSAPrivateKey.getInstance(seq);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE);

        PrivateKey privateKeyJava = converter
                .getPrivateKey(new PrivateKeyInfo(algId, bcPrivateKey));

        String PEM=DatatypeConverter.printBase64Binary(
                privateKeyJava.getEncoded());

        //System.out.println("PEM : " + PEM);
        return privateKeyJava;
    }
}
