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
package pt.inesctec.ids.clearinghouse.constant;

import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.lang.reflect.Modifier.*;

@NoArgsConstructor
public class DatabaseConstant {
    public static final String CONTRACTS = "contracts";
    public static final String MESSAGES = "messages";
    public static final String PROCESS = "process";
    public static final String LOGGING = "logging";

    public static boolean checkIfExists(String value) {
        AtomicBoolean returnValue= new AtomicBoolean(false);
        Field[] fields = DatabaseConstant.class.getDeclaredFields();
        Arrays.stream(fields).forEach(field -> {
            if (isStatic(field.getModifiers())
                    && isPublic(field.getModifiers())
                    && isFinal(field.getModifiers())) {
                try {
                    String fieldValue = (String) field.get(null);
                    if (value.equals(fieldValue)) {
                        returnValue.set(true);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return returnValue.get();
    }

    public static List<String> getCollectionNames() {
        Field[] fields = DatabaseConstant.class.getDeclaredFields();
        List<String> names=new ArrayList<>();
        Arrays.stream(fields).forEach(field -> {
            try {
                names.add(field.get(field.getName()).toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return names;
    }

    /**
    * Returns all collection names that are supposed to return on the Query Method
     * */
    public static List<String> getCollectionNames_ToReturn() {

        List<String> names=DatabaseConstant.getCollectionNames();
        names.remove(PROCESS);
        return names;
    }
}
