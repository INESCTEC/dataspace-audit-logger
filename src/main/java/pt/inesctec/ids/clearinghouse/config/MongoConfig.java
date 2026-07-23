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

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import pt.inesctec.ids.clearinghouse.converter.*;
import pt.inesctec.ids.clearinghouse.service.MongoService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.PROCESS;

@Configuration
@EnableMongoRepositories(basePackages = "pt.inesctec.ids.clearinghouse.repository")
public class MongoConfig extends AbstractMongoClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoConfig.class);


    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port}")
    private String mongoPort;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    @Value("${spring.data.mongodb.username}")
    private String mongoUsername;

    @Value("${spring.data.mongodb.password}")
    private String mongoPassword;

    private List<Converter<?,?>> converters = new ArrayList<Converter<?,?>>();


    /*@Value("${spring.data.mongodb.uri}")
    private String mongoURL;*/


    @Override
    protected @NotNull String getDatabaseName() {
        return mongoDatabase;
    }

    @Override
    public @NotNull MongoClient mongoClient() {
        //ConnectionString connectionString = new ConnectionString("mongodb://cese:password@localhost:27019/clearinghouse");
        ConnectionString connectionString = new ConnectionString("mongodb://"+mongoUsername+":"+mongoPassword+"@"+mongoHost+":"+mongoPort+"/"+getDatabase()+"?authSource=admin");

        LOGGER.info("Connecting to mongo at: "+ connectionString.toString());
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Override
    public @NotNull Collection getMappingBasePackages() {
        return Collections.singleton("pt.inesctec.ids.clearinghouse");
    }

    // Default value
    @Bean("Collection")
    public String getCollectionName() {
        return PROCESS;
    }

    @Bean("Database")
    public String getDatabase() {
        return mongoDatabase;
    }


    @Bean("MongoTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(this.mongoClient(), getDatabase());
    }

    @Override
    public MongoCustomConversions customConversions() {
        converters.add(new ZonedDateTimeWriteConverter());
        converters.add(new RequestCHMessageWriteConverter());
        converters.add(new ProcessesWriteConverter());
        /*converters.add(new ProcessesReadConverter());*/
        return new MongoCustomConversions(converters);
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
