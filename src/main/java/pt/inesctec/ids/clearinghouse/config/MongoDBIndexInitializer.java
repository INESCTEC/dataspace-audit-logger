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

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.index.Index;
import pt.inesctec.ids.clearinghouse.constant.DatabaseConstant;

import java.util.Set;

import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.CONTRACTS;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.MESSAGES;

@Component
public class MongoDBIndexInitializer {
    private final MongoTemplate mongoTemplate;

    public MongoDBIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    // https://www.baeldung.com/spring-data-mongodb-index-annotations-converter
    public void createIndexes() {
        String indexName="agreementID";

        // Define index for agreementID
        DatabaseConstant.getCollectionNames().forEach(collection -> {
            // Get indexes
            IndexOperations indexOperations=mongoTemplate.indexOps(collection);

            // If index does not exist, create it
            if(!existIndex(indexOperations, indexName))
                indexOperations.ensureIndex(new Index().on(indexName, Sort.Direction.ASC));
        });
    }

    /**
     * Chacks if index already exists
     * */
    private boolean existIndex(IndexOperations indexOps, String indexName){
        return indexOps.getIndexInfo().stream().anyMatch(name -> name.isIndexForFields(Set.of(indexName)));
    }
}
