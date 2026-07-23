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
package pt.inesctec.ids.clearinghouse.service;

import com.mongodb.client.result.DeleteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.inesctec.ids.clearinghouse.constant.DatabaseConstant;
import pt.inesctec.ids.clearinghouse.exceptions.BadRequestException;
import pt.inesctec.ids.clearinghouse.exceptions.InvalidValueException;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.repository.ClearingHouseMessageRepository;
import pt.inesctec.ids.clearinghouse.util.CompareUtil;
import pt.inesctec.ids.clearinghouse.util.MongoUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.PROCESS;

@Service
public class MongoService  {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoService.class);

    // Tabela para Contratos e outra para mensagens de artefatos

    private final MongoTemplate mongoTemplate;
    private final ClearingHouseMessageRepository clearingHouseMessageRepository;


    @Autowired
    public MongoService(MongoTemplate mongoTemplate, ClearingHouseMessageRepository clearingHouseMessageRepository) {
        this.mongoTemplate = mongoTemplate;
        this.clearingHouseMessageRepository = clearingHouseMessageRepository;
    }

    /**
     * Checks if the collection exists
     * @param collection name of the MongoDB collection
     * */
    public void checkIfCollectionExists(String collection) {
        if(!DatabaseConstant.checkIfExists(collection)) {
            throw new InvalidValueException("Invalid Collection");
        }
    }

    @Transactional
    public synchronized DBAbstract insert(DBAbstract message, String collection) {
        checkIfCollectionExists(collection);
        LOGGER.debug("Inserting data into collection '"+ collection + "' ...");
        DBAbstract savedMessage=null;

        // Find previous log entry and hash it, if present
        findLastInserted()
                .ifPresent(lastLog -> {
                    // Only the first ever log on the Clearing House is that should have the hash as null
                    message.setHash(MongoUtil.hash(lastLog));
        });

        if(message instanceof ClearingHouseMessage){
            // Saves document
            savedMessage=mongoTemplate.insert(message, collection);
        }

        if (message instanceof Processes){
            // In this case ignore the collection name
            savedMessage=mongoTemplate.insert(message, PROCESS);
        }
        LOGGER.debug("Inserting data into collection '"+ collection + "' ... OK");

        //System.out.println(savedMessage.toString_Complete());

        return savedMessage;
    }


    /**
     * Lists all messages from a collection
     * @param collection name of the MongoDB collection
     * @return list of all messages from the collection
     * */
    public synchronized List<?> findAll(String collection){
        checkIfCollectionExists(collection);

        if(collection.equals(PROCESS)){
            // This method maps all objects stored on Database into a Processes MessageType
            return mongoTemplate.findAll(Processes.class, collection);
        }else {
            // This method maps all objects stored on Database into a Clearing MessageType
            return mongoTemplate.findAll(ClearingHouseMessage.class, collection);
        }
    }

    /**
     * Checks if an Agreement has a Process
     * @param agreement
     * @return true or false
     * */
    public synchronized boolean hasProcessFromAgreement(String agreement){

        Query query = this.buildQueryToSearchByAgreementID(agreement);

        LOGGER.debug("Checks if a Process exists ...");
        boolean exists= mongoTemplate.exists(query, Processes.class, PROCESS);
        LOGGER.debug("Checks if a Process exists ... OK");

        return exists;
    }

    /**
     * Get last document from an agreement
     * @param agreement id
     * @return optional of DBAbstract
     * */
    public synchronized Optional<DBAbstract> findLastInsertedByAgreement(String agreement){
        // Devo poder fazer o findAll com o template quando tiver artefactos e contratos guardados

        Query query = this.buildQueryToSearchByAgreementID(agreement);
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        // Get all collections
        List<String> mongoCollectionNames=DatabaseConstant.getCollectionNames();
        List<DBAbstract> messages = new ArrayList<>();

        LOGGER.debug("Query database ...");
        mongoCollectionNames.forEach(name -> {
            DBAbstract found;
            if(name.equals(PROCESS)){
                // This method maps all objects stored on Database into a Processes MessageType
                found=mongoTemplate.findOne(query, Processes.class, name);
            }else {
                // This method maps all objects stored on Database into a Clearing MessageType
                found=mongoTemplate.findOne(query, ClearingHouseMessage.class, name);
            }
            if(found!=null) {
                messages.add(found);
            }
        });
        LOGGER.debug("Query database ... OK");

        // If it is not empty, sort by timestamp DESC to order by last timestamp
        LOGGER.debug("Sort response by date ...");
        messages.sort(new CompareUtil.SortByDateDesc());
        LOGGER.debug("Sort response by date ... OK");

        // If there is no messages it returns 'Optional.empty'
        return messages.stream().findFirst();
    }

    /**
     * Get all documents from an agreement
     * @param agreement id
     * @return list of ClearingHouseMessage
     * */
    public synchronized List<ClearingHouseMessage> findAllMessagesByAgreement_ExcludeProcesses(String agreement){
        Query query = this.buildQueryToSearchByAgreementID(agreement);

        List<String> mongoCollectionNames=DatabaseConstant.getCollectionNames_ToReturn();
        List<ClearingHouseMessage> messages = new ArrayList<>();

        LOGGER.debug("Query database ...");
        mongoCollectionNames.forEach(name -> {
            messages.addAll(mongoTemplate.find(query, ClearingHouseMessage.class, name));
        });
        LOGGER.debug("Query database ... OK");


        LOGGER.debug("Sort response by date ...");
        messages.sort(new CompareUtil.SortByDate());
        LOGGER.debug("Sort response by date ... OK");

        //messages.forEach(m -> LOGGER.debug(m.toJson()));

        return messages;
    }

    /**
     * Get all documents from an agreement
     * @param agreement id
     * @return list of DBAbstract
     * */
    public synchronized List<DBAbstract> findAllMessagesByAgreement(String agreement){
        Query query = this.buildQueryToSearchByAgreementID(agreement);

        List<String> mongoCollectionNames=DatabaseConstant.getCollectionNames();
        List<DBAbstract> messages = new ArrayList<>();

        LOGGER.debug("Query database ...");
        mongoCollectionNames.forEach(name -> {
            if(name.equals(PROCESS)){
                // This method maps all objects stored on Database into a Processes MessageType
                messages.addAll(mongoTemplate.find(query, Processes.class, name));
            }else {
                // This method maps all objects stored on Database into a Clearing MessageType
                messages.addAll(mongoTemplate.find(query, ClearingHouseMessage.class, name));

            }
        });
        LOGGER.debug("Query database ... OK");



        LOGGER.debug("Sort response by date ...");
        messages.sort(new CompareUtil.SortByDate());
        LOGGER.debug("Sort response by date ... OK");

        //messages.forEach(m -> LOGGER.debug(m.toJson()));

        return messages;
    }

    /**
     * Get all documents stored on a colection from the agreement
     * @param agreement id
     * @param collectionName collection to search
     * @return list of DBAbstract
     * */
    public synchronized List<DBAbstract> findAllMessagesByAgreement(String agreement, String collectionName){
        checkIfCollectionExists(collectionName);
        Query query = this.buildQueryToSearchByAgreementID(agreement);

        List<DBAbstract> messages = new ArrayList<>();
        LOGGER.debug("Query database ...");
        if(collectionName.equals(PROCESS)){
            // This method maps all objects stored on Database into a Processes MessageType
            messages.addAll(mongoTemplate.find(query, Processes.class, collectionName));
        }else {
            // This method maps all objects stored on Database into a Clearing MessageType
            messages.addAll(mongoTemplate.find(query, ClearingHouseMessage.class, collectionName));

        }
        LOGGER.debug("Query database ... OK");

        //messages.forEach(System.err::println);

        LOGGER.debug("Sort response by data ...");
        messages.sort(new CompareUtil.SortByDate());
        LOGGER.debug("Sort response by data ... OK");

        //messages.forEach(m -> LOGGER.debug(m.toJson()));

        return messages;
    }

    /**
     * Get all Process documents from the agreement
     * @param agreement id
     * @return list of Processes
     * */
    public synchronized List<Processes> findProcessFromAgreement(String agreement){
        Query query = this.buildQueryToSearchByAgreementID(agreement);

        List<Processes> messages = new ArrayList<>();
        LOGGER.debug("Query database ...");
        messages.addAll(mongoTemplate.find(query, Processes.class, PROCESS));
        LOGGER.debug("Query database ... OK");

        LOGGER.debug("Sort response by data ...");
        messages.sort(new CompareUtil.SortByDate());
        LOGGER.debug("Sort response by data ... OK");


        return messages;
    }

    /**
     * Get last inserted document
     * @return optional of DBAbstract
     * */
    public synchronized Optional<DBAbstract> findLastInserted(){
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));

        // Get all collections
        List<String> mongoCollectionNames=DatabaseConstant.getCollectionNames();
        List<DBAbstract> messages = new ArrayList<>();

        LOGGER.debug("Query database ...");
        mongoCollectionNames.forEach(name -> {
            DBAbstract found;
            if(name.equals(PROCESS)){
                // This method maps all objects stored on Database into a Processes MessageType
                found=mongoTemplate.findOne(query, Processes.class, name);
            }else {
                // This method maps all objects stored on Database into a Clearing MessageType
                found=mongoTemplate.findOne(query, ClearingHouseMessage.class, name);
            }
            if(found!=null) {
                messages.add(found);
            }
        });
        LOGGER.debug("Query database ... OK");

        // If it is not empty, sort by timestamp DESC to order by last timestamp
        LOGGER.debug("Sort response by date ...");
        messages.sort(new CompareUtil.SortByDateDesc());
        LOGGER.debug("Sort response by date ... OK");

        // If there is no messages it returns 'Optional.empty'
        return messages.stream().findFirst();
    }

    /**
     * Create a Query that returns all documents from an agreement
     * @param agreement id
     * @return query
     * */
    private Query buildQueryToSearchByAgreementID(String agreement){
        LOGGER.debug("Creating Query with agreement ...");
        Query query = new Query();
        query.addCriteria(Criteria.where("agreementID").is(agreement));
        LOGGER.debug(query.toString());
        LOGGER.debug("Creating Query with agreement ... OK");
        return query;
    }

    // Method used for testing ONLY
    public synchronized void remove(ClearingHouseMessage message, String collection){
        LOGGER.debug("Removing data from collection '"+ collection + "' ...");
        checkIfCollectionExists(collection);
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(message.get_id()));


        DeleteResult result = mongoTemplate.remove(message, collection);
        //mongoTemplate.findAllAndRemove(query, RequestClearingHouseMessage.class, collection);
        if(result.getDeletedCount()==0) {
            LOGGER.debug("Removing data from collection '"+ collection + "' ... NOK");
            throw new BadRequestException("Error while deleting message");
        }

        LOGGER.debug("Removing data from collection '"+ collection + "' ... OK");

    }

    public synchronized void remove(Processes message, String collection){
        LOGGER.debug("Removing data from collection '"+ collection + "' ...");
        checkIfCollectionExists(collection);
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(message.get_id()));


        DeleteResult result = mongoTemplate.remove(message, collection);
        //mongoTemplate.findAllAndRemove(query, ClearingHouseMessage.class, collection);
        if(result.getDeletedCount()==0) {
            LOGGER.debug("Removing data from collection '"+ collection + "' ... NOK");
            throw new BadRequestException("Error while deleting message");
        }

        LOGGER.debug("Removing data from collection '"+ collection + "' ... OK");

    }

    // Method used for testing ONLY
    public synchronized void removeAll(){
        List<String> mongoCollectionNames=DatabaseConstant.getCollectionNames();

        mongoCollectionNames.forEach(col ->{
            Query query = new Query();
            DeleteResult removed= mongoTemplate.remove(query, col);
            LOGGER.debug("Removed " +removed.getDeletedCount() + " entries from collection '"+col+"'");
        });
    }

}
