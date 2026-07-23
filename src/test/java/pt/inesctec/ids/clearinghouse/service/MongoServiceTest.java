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

import de.fraunhofer.iais.eis.ContractAgreement;
import de.fraunhofer.iais.eis.Message;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;
import pt.inesctec.ids.clearinghouse.model.Processes;
import pt.inesctec.ids.clearinghouse.util.MongoUtil;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class MongoServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoServiceTest.class);

    TestUtil testUtil;
    @Autowired MongoService mongoService;

    @BeforeEach
    void setUp() {
        testUtil=new TestUtil();
        mongoService.removeAll();

    }

    @AfterEach
    void tearDown() {
        //mongoService.removeAll();
    }

    @Test
    void insert() {
        String agreementID="12359-785";
        // Prepare header and Payload
        LOGGER.info("1. Test inserting an Artifact Request Message ... ");
        Message logMessage=testUtil.logMessage();
        String payload= testUtil.artefactRequestMessage().toString();
        // given
        ClearingHouseMessage message= new ClearingHouseMessageBuilder()
                .header(logMessage.toRdf())
                .payload(payload)
                .agreementID(agreementID)
                .build();

        // when
        mongoService.insert(message, MESSAGES);

        // then
        assertThat(message).isIn(mongoService.findAll(MESSAGES));
        assertThat(message.get_id()).isNotNull();
        assertThat(message.getAgreementID()).isEqualTo(agreementID);
        assertThat(message.getHeader()).isNotNull();
        assertThat(message.getTimestamp()).isNotNull();
        assertThat(message.getPayload()).isNotNull();
        assertThat(message.getHash()).isNull(); // It is the only Null because there are no previous messages
        LOGGER.info("1. Test inserting an Artifact Request Message ... OK");


        LOGGER.info("2. Test inserting an Artifact Response Message. This time Hash should not be null ... ");
        // 2. Check if hash is correct Prepare header and Payload
        logMessage=testUtil.logMessage();
        payload= testUtil.artefactResponseMessage().toString();
        // given
        message= new ClearingHouseMessageBuilder()
                .header(logMessage.toRdf())
                .payload(payload)
                .agreementID(agreementID)
                .build();

        // when
        mongoService.insert(message, MESSAGES);
        LOGGER.info("Inserting 'header' and 'payload' ... OK");

        // then
        assertThat(message).isIn(mongoService.findAll(MESSAGES));
        assertThat(message.get_id()).isNotNull();
        assertThat(message.getAgreementID()).isEqualTo(agreementID);
        assertThat(message.getHeader()).isNotNull();
        assertThat(message.getTimestamp()).isNotNull();
        assertThat(message.getPayload()).isNotNull();
        assertThat(message.getHash()).isNotNull(); // Now, it is not Null because there is already one message. The hash is the hash of the previois message
        LOGGER.info("2. Test inserting an Artifact Response Message. This time Hash should not be null ... OK");


        // 3. Insert a Process
        // Prepare header and Payload
        LOGGER.info("3. Test inserting a Process... ");
        logMessage=testUtil.requestMessage();
        payload= testUtil.owners();
        // given
        Processes processMessage= new ProcessesBuilder()
                .header(logMessage.toRdf())
                .owners(testUtil.owners_List())
                .agreementID("3456g-785")
                .build();

        // when
        mongoService.insert(processMessage, MESSAGES);

        // then
        assertThat(processMessage).isIn(mongoService.findAll(PROCESS));
        assertThat(processMessage.get_id()).isNotNull();
        assertThat(processMessage.getAgreementID()).isEqualTo("3456g-785");
        assertThat(processMessage.getHeader()).isNotNull();
        assertThat(processMessage.getTimestamp()).isNotNull();
        assertThat(processMessage.getOwners()).isEqualTo(testUtil.owners_List());
        assertThat(processMessage.getHash()).isNotNull(); // Now, it is not Null because there is already one message. The hash is the hash of the previois message
        LOGGER.info("3. Test inserting a Process... OK");


        // 4. Insert a Log into Contracts collection
        // Prepare header and Payload
        LOGGER.info("4. Test inserting a Contract Agreement Message... ");
        logMessage=testUtil.logMessage();
        payload= testUtil.contractAgreement().toRdf();

        // given
        message= new ClearingHouseMessageBuilder()
                .header(logMessage.toRdf())
                .payload(payload)
                .agreementID("3456g-785")
                .build();

        // when
        mongoService.insert(message, CONTRACTS);

        // then
        assertThat(message).isIn(mongoService.findAll(CONTRACTS));
        assertThat(message.get_id()).isNotNull();
        assertThat(message.getAgreementID()).isNotNull();
        assertThat(message.getHeader()).isNotNull();
        assertThat(message.getPayload()).isNotNull();
        assertThat(message.getHash()).isNotNull(); // Now, it is not Null because there is already messages on the database. The hash is the hash of the previous message
        LOGGER.info("4. Test inserting a Contract Agreement Message... OK");

    }

    @Test
    void findAll() {
        mongoService.removeAll();
        LOGGER.info("Printing all database values ... ");

        List found=mongoService.findAll(MESSAGES);
        found.forEach(System.out::println);
        LOGGER.info("Printing all database values ... OK");

        assertThat(found.size()).isEqualTo(0);

        // Populate Database
        populateDatabase();

        found=mongoService.findAll(MESSAGES);
        assertThat(found.size()).isEqualTo(2);

        found=mongoService.findAll(PROCESS);
        found.forEach(System.err::println);
        assertThat(found.size()).isEqualTo(2);
        found.clear();

        found=mongoService.findAll(CONTRACTS);
        assertThat(found.size()).isEqualTo(1);

    }

    @Test
    void findAllByAgreement() {
        populateDatabase();

        List<DBAbstract> listOfMessages=mongoService.findAllMessagesByAgreement("12345");
        assertThat(listOfMessages.size()).isEqualTo(2);

        listOfMessages=mongoService.findAllMessagesByAgreement("278655-36fb-4bfd-8cac-75478");
        assertThat(listOfMessages.size()).isEqualTo(1);

        listOfMessages=mongoService.findAllMessagesByAgreement("015fd3fb-36fb-4bfd-8cac-2f49aeafb328");
        assertThat(listOfMessages.size()).isEqualTo(1);

        // This agreement has 1 Log Message. So, the test needs to return 1
        listOfMessages=mongoService.findAllMessagesByAgreement("grwerfdcae-4bfd-8cac-2f49aeafb328");
        assertThat(listOfMessages.size()).isEqualTo(1);
    }

    @Test
    void findAllByAgreementWithCollection() {
        mongoService.removeAll();
        populateDatabase();

        List<DBAbstract> listOfMessages=mongoService.findAllMessagesByAgreement("12345", MESSAGES);
        assertThat(listOfMessages.size()).isEqualTo(1);

        listOfMessages=mongoService.findAllMessagesByAgreement("12345", CONTRACTS);
        assertThat(listOfMessages.size()).isEqualTo(0);

        listOfMessages=mongoService.findAllMessagesByAgreement("12345", PROCESS);
        assertThat(listOfMessages.size()).isEqualTo(1);

        listOfMessages=mongoService.findAllMessagesByAgreement("015fd3fb-36fb-4bfd-8cac-2f49aeafb328", MESSAGES);
        assertThat(listOfMessages.size()).isEqualTo(1);

        // This agreement has 1 Log Message. So, the test needs to return 1
        listOfMessages=mongoService.findAllMessagesByAgreement("grwerfdcae-4bfd-8cac-2f49aeafb328", CONTRACTS);
        assertThat(listOfMessages.size()).isEqualTo(1);
    }

    @Test
    void findProcessFromAgreement() {
        mongoService.removeAll();
        populateDatabase();

        // This agreement has 1 Process and 1 logMessage. So, the test needs to return 1
        List<Processes> listOfMessages=mongoService.findProcessFromAgreement("12345");
        assertThat(listOfMessages.size()).isEqualTo(1);
        assertThat(listOfMessages.get(0).getOwners()).isInstanceOf(List.class);

        // This agreement only has 1 Process. So, the test needs to return 1
        listOfMessages=mongoService.findProcessFromAgreement("278655-36fb-4bfd-8cac-75478");
        assertThat(listOfMessages.size()).isEqualTo(1);

        // This agreement only has 1 Log Message. So, the test needs to return 0
        listOfMessages=mongoService.findProcessFromAgreement("015fd3fb-36fb-4bfd-8cac-2f49aeafb328");
        assertThat(listOfMessages.size()).isEqualTo(0);

        // This agreement has 1 Log Message. So, the test needs to return 0
        listOfMessages=mongoService.findProcessFromAgreement("grwerfdcae-4bfd-8cac-2f49aeafb328");
        assertThat(listOfMessages.size()).isEqualTo(0);
    }

    @Test
    void findAllMessagesByAgreement_ExcludeProcesses() {
        mongoService.removeAll();
        populateDatabase();

        // This agreement has 1 Process and 1 logMessage. So, the test needs to return 1
        List<ClearingHouseMessage> listOfMessages=mongoService.findAllMessagesByAgreement_ExcludeProcesses("12345");
        assertThat(listOfMessages.size()).isEqualTo(1);
        assertThat(listOfMessages.get(0).getPayload()).isInstanceOf(String.class);

        // This agreement only has 1 Process. So, the test needs to return 0
        listOfMessages=mongoService.findAllMessagesByAgreement_ExcludeProcesses("278655-36fb-4bfd-8cac-75478");
        assertThat(listOfMessages.size()).isEqualTo(0);

        // This agreement has 1 Log Message. So, the test needs to return 1
        listOfMessages=mongoService.findAllMessagesByAgreement_ExcludeProcesses("015fd3fb-36fb-4bfd-8cac-2f49aeafb328");
        assertThat(listOfMessages.size()).isEqualTo(1);

        // This agreement has 1 Log Message. So, the test needs to return 1
        listOfMessages=mongoService.findAllMessagesByAgreement_ExcludeProcesses("grwerfdcae-4bfd-8cac-2f49aeafb328");
        assertThat(listOfMessages.size()).isEqualTo(1);
    }

    @Test
    void hasProcessFromAgreement() {
        populateDatabase();

        mongoService.findProcessFromAgreement("12345").forEach(System.err::println);

        boolean hasProcess=mongoService.hasProcessFromAgreement("12345");
        System.out.println(hasProcess);
        assertThat(hasProcess).isTrue();

        hasProcess=mongoService.hasProcessFromAgreement("85239");
        System.out.println(hasProcess);
        assertThat(hasProcess).isFalse();
    }

    @Test
    void remove() {
        populateDatabase();
        assertThat(mongoService.findAll(MESSAGES).size()).isEqualTo(2);

        // Prepare test
        ClearingHouseMessage logMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("852369741")
                .build();
        mongoService.insert(logMessage, MESSAGES);
        System.out.println(logMessage.toString());

        assertThat(mongoService.findAll(MESSAGES).size()).isEqualTo(3);

        mongoService.remove(logMessage, MESSAGES);
        assertThat(mongoService.findAll(MESSAGES).size()).isEqualTo(2);

    }

    @Test
    void removeAll() {
        mongoService.removeAll();
        assertThat(mongoService.findAll(MESSAGES).size()).isEqualTo(0);
        assertThat(mongoService.findAll(CONTRACTS).size()).isEqualTo(0);
        assertThat(mongoService.findAll(PROCESS).size()).isEqualTo(0);

    }

    @Test
    void findLastInsertedByAgreement(){
        mongoService.removeAll();
        String agreementID="12345";

        LOGGER.info("Start test 1 'Test with an agreement that does not have any previous message' ...");
        // 1. Test with an agreement that does not have any previous message
        // given that there are no messages with that agreement
        // when
        Optional<DBAbstract> lastInserted=mongoService.findLastInsertedByAgreement(agreementID);
        // then
        assertThat(lastInserted).isEmpty();
        LOGGER.info("Start test 1 'Test with an agreement that does not have any previous message' ... OK");


        // 2. Test with an agreement that alreaday has one document
        LOGGER.info("Start test 2: 'Test with an agreement that already has one document' ...");
        // given
        Processes processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID(agreementID)
                .build();
        mongoService.insert(processMessage, PROCESS);
        // when
         lastInserted=mongoService.findLastInsertedByAgreement(agreementID);

        // then
        assertThat(lastInserted).isPresent();
        assertThat(lastInserted.get().toString()).isEqualTo(processMessage.toString()); // ->Passes because when inserting it updates the inserted variable
        assertThat(lastInserted.get().get_id()).isNotNull();
        assertThat(lastInserted.get().getAgreementID()).isEqualTo(processMessage.getAgreementID());
        assertThat(lastInserted.get().getHeader()).isEqualTo(processMessage.getHeader());
        assertThat(lastInserted.get().getTimestamp()).isEqualTo(processMessage.getTimestamp());
        assertThat(lastInserted.get().getHash()).isNull();
        assertThat(((Processes)lastInserted.get()).getOwners()).isEqualTo(processMessage.getOwners());
        assertThat(lastInserted.get()).isNotInstanceOf(ClearingHouseMessage.class);
        LOGGER.info("Start test 2: 'Test with an agreement that alreaday has one document' ... OK");


        // 3. Test with an agreement that has two documents
        LOGGER.info("Start test 3: 'Test with an agreement that has two documents' ...");
        // given
        ClearingHouseMessage contractMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.contractAgreement().toRdf())
                .agreementID(agreementID)
                .build();
        mongoService.insert(contractMessage, CONTRACTS);
        // when
        System.err.println("TEST");
        lastInserted=mongoService.findLastInsertedByAgreement(agreementID);

        /*System.err.println(lastInserted);
        System.out.println(contractMessage.toJson());*/
        // then
        assertThat(lastInserted).isPresent();
        assertThat(lastInserted.get()).isEqualTo(contractMessage); // ->Passes because when inserting it updates the inserted variable
        assertThat(lastInserted.get().get_id()).isNotNull();
        assertThat(lastInserted.get().getAgreementID()).isEqualTo(contractMessage.getAgreementID());
        assertThat(lastInserted.get().getHeader()).isEqualTo(contractMessage.getHeader());
        assertThat(lastInserted.get().getTimestamp()).isEqualTo(contractMessage.getTimestamp());
        assertThat(((ClearingHouseMessage)lastInserted.get()).getPayload()).isEqualTo(contractMessage.getPayload());
        assertThat(lastInserted.get().getHash()).isEqualTo(MongoUtil.hash(processMessage.toHashString()));
        LOGGER.info("Start test 3: 'Test with an agreement that has two documents' ... OK");


        // 4. Test with an agreement that has three documents
        LOGGER.info("Start test 4: 'Test with an agreement that has three documents' ...");
        ClearingHouseMessage artifactMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID(agreementID)
                .build();
        mongoService.insert(artifactMessage, MESSAGES);
        // when
        lastInserted=mongoService.findLastInsertedByAgreement(agreementID);

        System.err.println(lastInserted);
        System.out.println(artifactMessage.toJson());
        // then
        assertThat(lastInserted).isPresent();
        assertThat(lastInserted.get()).isEqualTo(artifactMessage); // ->Passes because when inserting it updates the inserted variable
        assertThat(lastInserted.get().get_id()).isNotNull();
        assertThat(lastInserted.get().getAgreementID()).isEqualTo(artifactMessage.getAgreementID());
        assertThat(lastInserted.get().getHeader()).isEqualTo(artifactMessage.getHeader());
        assertThat(lastInserted.get().getTimestamp()).isEqualTo(artifactMessage.getTimestamp());
        assertThat(((ClearingHouseMessage)lastInserted.get()).getPayload()).isEqualTo(artifactMessage.getPayload());
        assertThat(lastInserted.get().getHash()).isEqualTo(MongoUtil.hash(contractMessage)); // The hash is the hash of the last inserted message (by agreement)
        LOGGER.info("Start test 4: 'Test with an agreement that has three documents' ... OK");


        // 5. Test with a NEW agreement
        LOGGER.info("Start test 5: 'Test with a NEW agreement' ...");
        // given that there are no messages with that agreement
        // when
        lastInserted=mongoService.findLastInsertedByAgreement("147963");
        // then
        assertThat(lastInserted).isEmpty();
        LOGGER.info("Start test 5: 'Test with a NEW agreement' ... OK");


        // 6. Test with a NEW agreement
        LOGGER.info("Start test 6: 'Test with a NEW agreement' ...");
        processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("147963")
                .build();
        mongoService.insert(processMessage, PROCESS);
        // when
        lastInserted=mongoService.findLastInsertedByAgreement("147963");

        // then
        assertThat(lastInserted).isPresent();
        assertThat(lastInserted.get().toJson()).isEqualTo(processMessage.toJson()); // ->Passes because when inserting it updates the inserted variable
        assertThat(lastInserted.get().get_id()).isNotNull();
        assertThat(lastInserted.get().getAgreementID()).isEqualTo(processMessage.getAgreementID());
        assertThat(lastInserted.get().getHeader()).isEqualTo(processMessage.getHeader());
        assertThat(lastInserted.get().getTimestamp()).isEqualTo(processMessage.getTimestamp());
        assertThat(((Processes)lastInserted.get()).getOwners()).isEqualTo(processMessage.getOwners());
        assertThat(lastInserted.get().getHash()).isEqualTo(MongoUtil.hash(artifactMessage));
        assertThat(lastInserted.get()).isNotInstanceOf(ClearingHouseMessage.class);
        LOGGER.info("Start test 6: 'Test with a NEW agreement' ... OK");

    }

    @Test
    void findLastInserted() throws IOException {
        mongoService.removeAll();

        // After removing assert that method returns an empty optional
        Optional<DBAbstract> found=mongoService.findLastInserted();
        assertThat(found.isEmpty()).isTrue();

        // Populate Database
        populateDatabase();

        // After populating database assert that method returns a Document and that document was the last inserted
        found=mongoService.findLastInserted();
        assertThat(found.isPresent()).isTrue();
        System.out.println(found.get().toJson());
        Object deserializedMessage=found.get()
                .deserialize(((ClearingHouseMessage)found.get())
                        .getPayload(), ContractAgreement.class);
        assertThat(deserializedMessage).isInstanceOf(ContractAgreement.class);

    }

    private void populateDatabase(){
        // Populate Database
        Processes processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("12345")
                .build();
        ClearingHouseMessage logMessage=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("12345")
                .build();
        ClearingHouseMessage logMessage2=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.artefactRequestMessage().toRdf())
                .agreementID("015fd3fb-36fb-4bfd-8cac-2f49aeafb328")
                .build();
        Processes processMessage2=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("278655-36fb-4bfd-8cac-75478")
                .build();

        ClearingHouseMessage contract=new ClearingHouseMessageBuilder()
                .header(testUtil.logMessage().toRdf())
                .payload(testUtil.contractAgreement().toRdf())
                .agreementID("grwerfdcae-4bfd-8cac-2f49aeafb328")
                .build();

        mongoService.insert(processMessage, PROCESS);
        mongoService.insert(processMessage2, PROCESS);
        mongoService.insert(logMessage, MESSAGES);
        mongoService.insert(logMessage2, MESSAGES);
        mongoService.insert(contract, CONTRACTS);
    }
}