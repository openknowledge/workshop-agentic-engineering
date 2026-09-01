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
package de.openknowledge.baseproject.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.RECORDS;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static de.openknowledge.baseproject.architecture.conditions.HaveFieldsCondition.haveFields;
import static de.openknowledge.baseproject.architecture.conditions.HaveSizeCondition.hasSize;
import static de.openknowledge.baseproject.architecture.grammar.ArchUnitGrammarBlocks.are;
import static de.openknowledge.baseproject.architecture.grammar.ArchUnitGrammarBlocks.that;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Version;

@AnalyzeClasses(packages = EntityTest.ROOT_PACKAGE)
class EntityTest {

    static final String ROOT_PACKAGE = "de.openknowledge.baseproject";

    @ArchTest
    static ArchRule entitiesAreNamedEntityOrAggregate =
            classes()
                    .that()
                    .areAnnotatedWith(Entity.class)
                    .should()
                    .haveSimpleNameEndingWith("Entity")
                    .orShould()
                    .haveSimpleNameEndingWith("Aggregate")
                    .because("the suffix makes persistent classes recognizable at first sight");

    @ArchTest
    static ArchRule entityFieldsAreIdVersionOrEmbedded =
            fields().that()
                    .areDeclaredInClassesThat()
                    .areAnnotatedWith(Entity.class)
                    .and()
                    .areNotStatic()
                    .should()
                    .beAnnotatedWith(Id.class)
                    .orShould()
                    .beAnnotatedWith(Version.class)
                    .orShould()
                    .beAnnotatedWith(Embedded.class)
                    .orShould()
                    .beAnnotatedWith(Enumerated.class)
                    .orShould()
                    .beAnnotatedWith(ManyToOne.class)
                    .orShould()
                    .beAnnotatedWith(ManyToMany.class)
                    .orShould()
                    .beAnnotatedWith(OneToOne.class)
                    .because(
                            "entities only hold their technical fields and delegate their state to"
                                    + " an embedded value object");

    @ArchTest
    static ArchRule embeddedFieldsAreRecords =
            fields().that()
                    .areDeclaredInClassesThat()
                    .areAnnotatedWith(Entity.class)
                    .and()
                    .areAnnotatedWith(Embedded.class)
                    .should()
                    .haveRawType(RECORDS)
                    .because("the embedded state of an entity is a domain record");

    @ArchTest
    static ArchRule entitiesHaveAtMostOneEmbeddedField =
            classes()
                    .that()
                    .areAnnotatedWith(Entity.class)
                    .should(
                            haveFields(
                                    that(are(annotatedWith(Embedded.class))),
                                    hasSize(0).or(hasSize(1))))
                    .because(
                            "an entity wraps exactly one domain record; if it has more embedded"
                                    + " fields, introduce a value object that combines them");
}
