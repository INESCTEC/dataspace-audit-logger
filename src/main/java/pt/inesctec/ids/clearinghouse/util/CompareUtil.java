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

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.inesctec.ids.clearinghouse.model.ClearingHouseMessage;
import pt.inesctec.ids.clearinghouse.model.DBAbstract;

import java.util.*;

public class CompareUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompareUtil.class);


  /**
   * Checks if two lists are the same when one or both are empty or null
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean compareListsEmptyAndNull(List<?> list1, List<?> list2){

    // Ideal condition. Already handles all nulls and all empty lists, as well as list content. For more specific cases it uses the other conditions
    if(Objects.equals(list1, list2)) return true;

    // If one list is null and other is empty, they are considered equal
    try {
      if((list1==null && list2.isEmpty()) || list2==null && list1.isEmpty()) return true;
    }
    catch (NullPointerException ex){}

    if(list1==null) return false;
    if(list2==null) return false;

    return false;
  }

  public static boolean strings(List<String> list1, List<String> list2){

    if(compareListsEmptyAndNull(list1, list2)) return true;

    if(list1==null) return false;
    if(list2==null) return false;

    List<String> constraintList1= new ArrayList<>(list1);
    List<String> constraintList2= new ArrayList<>(list2);

    constraintList1.sort(new SortByString());
    constraintList2.sort(new SortByString());


    if(CollectionUtils.isEqualCollection(constraintList1, constraintList2) ||
            ListUtils.isEqualList(constraintList1,constraintList2) || constraintList1.toString().equals(constraintList2.toString())){
      return true;
    }

    return false;
  }

  /**
   * Compares two maps
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean maps(Map<?, ?> map1, Map<?, ?> map2){

    if (map1.size() != map2.size()) {
      return false;
    }

    return map1.entrySet().stream()
            .allMatch(e -> Objects.equals(
                    e.getValue().toString(),
                    map2.get(e.getKey()).toString())
            );

  }

  /**
   * Compares two sets
   * @return true if they are equal
   * @return false if they are different
   * */
  public static boolean sets(Set<?> set1, Set<?> set2){

    // Handles if both sets are null or empty
    if(Objects.equals(set1, set2)) return true;

    // Handles if one is empty and the other null
    try {
      if((set1==null && set2.isEmpty()) || set2==null && set1.isEmpty()) return true;
    }
    catch (NullPointerException ex){}

    // Handles if only one set is null
    if(set1==null) return false;
    if(set2==null) return false;

    // Sees the difference
    //System.err.println(Sets.difference(set1,set2));
    return Sets.difference(set1,set2).isEmpty();
  }

  static class SortByString implements Comparator<String>
  { @Override
  public int compare(String o1, String o2) {
    return o1.compareTo(o2);
  }
  }

  public static class SortByDate implements Comparator<DBAbstract>
  {
    @Override
    public int compare(DBAbstract clearingHouseMessage, DBAbstract t1) {
      return clearingHouseMessage.getTimestamp().compareTo(t1.getTimestamp());
    }
  }

  public static class SortByDateDesc implements Comparator<DBAbstract>
  {
    @Override
    public int compare(DBAbstract clearingHouseMessage, DBAbstract t1) {
      return t1.getTimestamp().compareTo(clearingHouseMessage.getTimestamp());
    }
  }

}
