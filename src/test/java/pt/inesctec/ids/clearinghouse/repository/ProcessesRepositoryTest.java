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
package pt.inesctec.ids.clearinghouse.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import pt.inesctec.ids.clearinghouse.TestUtil;
import pt.inesctec.ids.clearinghouse.builder.ClearingHouseMessageBuilder;
import pt.inesctec.ids.clearinghouse.builder.ProcessesBuilder;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.Processes;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static pt.inesctec.ids.clearinghouse.constant.DatabaseConstant.*;

@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-test.properties")
class ProcessesRepositoryTest {

    @Autowired ProcessRepository repository;
   TestUtil testUtil=new TestUtil();
    Processes processMessage_LastToBeInserted;

    @Test
    void findFirstByAgreementIDOrderByTimestamp() {
        repository.deleteAll();

        // Fist, Assert that BD is empty
        assertThat(repository.findAll().size()).isEqualTo(0);

        populateDatabase();
        assertThat(repository.findAll().size()).isEqualTo(3);

        // Start actual test
        Optional<Processes> lastInserted_Op=repository.findFirstByAgreementIDOrderByTimestampDesc("12345");
        assertThat(lastInserted_Op).isPresent();
        System.err.println("Returned id" + lastInserted_Op.get()._id);

        assertThat(lastInserted_Op.get().toString()).isEqualTo(processMessage_LastToBeInserted.toString());

        // Clean database
        repository.deleteAll();

    }

    private void populateDatabase(){
        // Populate Database
        Processes processMessage=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("12345")
                .build();
        Processes processMessage2=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("278655-36fb-4bfd-8cac-75478")
                .build();
        processMessage_LastToBeInserted=new ProcessesBuilder()
                .header(testUtil.requestMessage().toRdf())
                .owners(testUtil.owners_List())
                .agreementID("12345")
                .build();

        repository.insert(processMessage);
        repository.insert(processMessage2);
        repository.insert(processMessage_LastToBeInserted);

        System.err.println("processMessage_LastToBeInserted" + processMessage_LastToBeInserted.getTimestamp());
        System.err.println("processMessage" + processMessage.getTimestamp());

    }
}