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

import static com.tngtech.archunit.lang.conditions.ArchConditions.beAnnotatedWith;
import static com.tngtech.archunit.lang.conditions.ArchConditions.beAssignableTo;
import static com.tngtech.archunit.lang.conditions.ArchConditions.haveSimpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;

@AnalyzeClasses(
        packages = ArchitectureTest.ROOT_PACKAGE,
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    static final String ROOT_PACKAGE = "de.openknowledge.customermanagement";

    @ArchTest
    static ArchRule packagesAreFreeOfCycles =
            slices().matching(ROOT_PACKAGE + ".(*)..")
                    .should()
                    .beFreeOfCycles()
                    .because(
                            "cyclic dependencies between features make them impossible to change independently");

    @ArchTest
    static ArchRule layersAreRespected =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .layer("Web")
                    .definedBy("..web..")
                    .layer("Domain")
                    .definedBy("..domain..")
                    .layer("Integration")
                    .definedBy("..integration..")
                    .layer("Infrastructure")
                    .definedBy("..infrastructure..")
                    .whereLayer("Web")
                    .mayNotBeAccessedByAnyLayer()
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers("Web")
                    .whereLayer("Integration")
                    .mayOnlyBeAccessedByLayers("Domain")
                    .because("dependencies must point from web via domain to integration");

    @ArchTest
    static ArchRule domainLayerContainsOnlyDomainTypes =
            classes()
                    .that()
                    .resideInAPackage("..domain..")
                    .and()
                    .areTopLevelClasses()
                    .should()
                    .beRecords()
                    .orShould()
                    .beEnums()
                    .orShould()
                    .beAnnotatedWith(Entity.class)
                    .orShould(
                            haveSimpleNameEndingWith("Service").and(beAnnotatedWith(Service.class)))
                    .orShould(
                            beAssignableTo(Repository.class)
                                    .and(haveSimpleNameEndingWith("Repository")))
                    .because(
                            "the domain layer only consists of value objects (records, enums),"
                                    + " entities, aggregates, services (named ...Service) and"
                                    + " repositories (named ...Repository)");
}
