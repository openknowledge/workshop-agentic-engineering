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
package de.openknowledge.customermanagement.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static de.openknowledge.customermanagement.architecture.conditions.HaveMatchingSizeConstraintCondition.sizeConstraintMatchingColumnLength;
import static de.openknowledge.customermanagement.architecture.conditions.HaveSchemaMatchingConstraintsCondition.schemaMatchingConstraints;
import static de.openknowledge.customermanagement.architecture.conditions.MatchFlywayMigrationColumnLengthCondition.columnLengthMatchingFlyway;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.have;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AnalyzeClasses(packages = OpenApiConstraintTest.ROOT_PACKAGE)
class OpenApiConstraintTest {

    static final String ROOT_PACKAGE = "de.openknowledge.customermanagement";

    static final Map<String, Integer> DB_COLUMN_LENGTHS = buildDbColumnLengthMap();

    @ArchTest
    static ArchRule everyStringColumnLengthHasMatchingSizeConstraint =
            classes()
                    .that()
                    .areAnnotatedWith(Entity.class)
                    .should(have(sizeConstraintMatchingColumnLength()));

    @ArchTest
    static ArchRule everyEntityColumnLengthMatchesFlywayMigration =
            classes()
                    .that()
                    .areAnnotatedWith(Entity.class)
                    .should(have(columnLengthMatchingFlyway(DB_COLUMN_LENGTHS)));

    @ArchTest
    static ArchRule schemaOnRecordsMatchesBeanValidationConstraints =
            classes()
                    .that()
                    .areRecords()
                    .and()
                    .areAnnotatedWith(Schema.class)
                    .should(have(schemaMatchingConstraints()))
                    .because(
                            "the OpenAPI schema is derived from the constraints; a hand-written"
                                    + " @Schema must not contradict them")
                    .allowEmptyShould(true);

    private static Map<String, Integer> buildDbColumnLengthMap() {
        try {
            Map<String, Integer> result = new HashMap<>();
            URL migrationDir =
                    OpenApiConstraintTest.class.getClassLoader().getResource("db/migration");
            if (migrationDir == null) {
                return result;
            }
            File[] sqlFiles =
                    new File(migrationDir.toURI()).listFiles((dir, name) -> name.endsWith(".sql"));
            if (sqlFiles == null) {
                return result;
            }
            Arrays.sort(sqlFiles, Comparator.comparing(File::getName));
            for (File sqlFile : sqlFiles) {
                String sql = Files.readString(sqlFile.toPath()).toUpperCase();
                parseCreateTable(sql, result);
                parseAlterTable(sql, result);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("failed to read Flyway migration files", e);
        }
    }

    private static void parseCreateTable(String sql, Map<String, Integer> result) {
        Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)");
        Pattern colPattern =
                Pattern.compile("^\\s+(\\w+)\\s+VARCHAR\\s*\\((\\d+)\\)", Pattern.MULTILINE);
        String[] statements = sql.split(";");
        for (String stmt : statements) {
            Matcher tableMatcher = tablePattern.matcher(stmt);
            if (!tableMatcher.find()) {
                continue;
            }
            String tableName = tableMatcher.group(1);
            Matcher colMatcher = colPattern.matcher(stmt);
            while (colMatcher.find()) {
                result.put(
                        tableName + "." + colMatcher.group(1),
                        Integer.parseInt(colMatcher.group(2)));
            }
        }
    }

    private static void parseAlterTable(String sql, Map<String, Integer> result) {
        Pattern alterPattern =
                Pattern.compile(
                        "ALTER TABLE\\s+(\\w+)\\s+ALTER\\s+COLUMN\\s+(\\w+)"
                                + "\\s+TYPE\\s+VARCHAR\\s*\\((\\d+)\\)");
        Matcher matcher = alterPattern.matcher(sql);
        while (matcher.find()) {
            result.put(
                    matcher.group(1) + "." + matcher.group(2), Integer.parseInt(matcher.group(3)));
        }
    }
}
