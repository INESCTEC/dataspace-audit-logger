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
package pt.inesctec.ids.clearinghouse.converter;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.Processes;

import java.util.HashMap;
import java.util.Map;

import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.HEADER;
import static pt.inesctec.ids.clearinghouse.constant.IDSConstant.PAYLOAD;

// Taken from https://docs.spring.io/spring-data/mongodb/reference/mongodb/mapping/custom-conversions.html
public class ProcessesWriteConverter implements Converter<Processes, Document> {

    @Override
    public Document convert(Processes source) {
        //Document document=new Document(source.toMap());
        Document document=new Document();
        document.put("_id", source._id);
        document.put(HEADER, source.getHeader());
        document.put("owners", source.getOwners());
        document.put("agreementID", source.getAgreementID());
        document.put("timestamp", source.getTimestamp());
        document.put("hash", source.getHash());

        return document;
    }
}
