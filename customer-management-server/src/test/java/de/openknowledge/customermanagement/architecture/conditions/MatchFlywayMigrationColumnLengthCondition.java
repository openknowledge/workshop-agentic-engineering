/*
 * Copyright (C) open knowledge GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.openknowledge.customermanagement.architecture.conditions;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Table;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchFlywayMigrationColumnLengthCondition extends ArchCondition<JavaClass> {

    private final Map<String, Integer> dbLengths;

    public MatchFlywayMigrationColumnLengthCondition(Map<String, Integer> dbLengths) {
        super("match the VARCHAR(N) length defined in Flyway migrations");
        this.dbLengths = dbLengths;
    }

    public static ArchCondition<JavaClass> columnLengthMatchingFlyway(
            Map<String, Integer> dbLengths) {
        return new MatchFlywayMigrationColumnLengthCondition(dbLengths);
    }

    @Override
    public void check(JavaClass javaClass, ConditionEvents events) {
        List<String> violations = new ArrayList<>();
        checkEntityVsDb(javaClass.reflect(), dbLengths, violations);
        for (String violation : violations) {
            events.add(SimpleConditionEvent.violated(javaClass, violation));
        }
    }

    private static void checkEntityVsDb(
            Class<?> entityClass, Map<String, Integer> dbLengths, List<String> violations) {
        Table table = entityClass.getAnnotation(Table.class);
        if (table == null) {
            return;
        }
        String tableName = table.name().toUpperCase();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.getAnnotation(Embedded.class) == null) {
                continue;
            }
            AttributeOverride[] overrides = field.getAnnotationsByType(AttributeOverride.class);
            for (AttributeOverride override : overrides) {
                checkColumnVsDb(entityClass, tableName, override, dbLengths, violations);
            }
        }
    }

    private static void checkColumnVsDb(
            Class<?> entityClass,
            String tableName,
            AttributeOverride override,
            Map<String, Integer> dbLengths,
            List<String> violations) {
        Column column = override.column();
        String colName = column.name().toUpperCase();
        Integer dbLength = dbLengths.get(tableName + "." + colName);
        if (dbLength == null) {
            return;
        }
        int jpaLength = column.length();
        if (jpaLength != dbLength) {
            violations.add(
                    String.format(
                            "%s.%s: @Column(length=%d) but Flyway defines VARCHAR(%d)",
                            entityClass.getSimpleName(), colName, jpaLength, dbLength));
        }
    }
}
