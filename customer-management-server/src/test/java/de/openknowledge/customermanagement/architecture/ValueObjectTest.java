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

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.RECORDS;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.TOP_LEVEL_CLASSES;
import static com.tngtech.archunit.core.domain.JavaMember.Predicates.declaredIn;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.metaAnnotatedWith;
import static com.tngtech.archunit.core.domain.properties.HasModifiers.Predicates.modifier;
import static com.tngtech.archunit.core.domain.properties.HasType.Predicates.rawType;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.all;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.constructors;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static de.openknowledge.customermanagement.architecture.conditions.AcceptNullArgumentsCondition.acceptNullArguments;
import static de.openknowledge.customermanagement.architecture.conditions.BeAnnotatedWithCondition.beAnnotatedWith;
import static de.openknowledge.customermanagement.architecture.conditions.BeMetaAnnotatedWithCondition.beMetaAnnotatedWith;
import static de.openknowledge.customermanagement.architecture.conditions.EveryCondition.every;
import static de.openknowledge.customermanagement.architecture.conditions.HaveParametersCondition.haveParameters;
import static de.openknowledge.customermanagement.architecture.conditions.HaveSizeCondition.hasSize;
import static de.openknowledge.customermanagement.architecture.conditions.HaveTypeCondition.haveType;
import static de.openknowledge.customermanagement.architecture.conditions.IsCollectionHavingTypeParameterCondition.isCollectionHavingTypeParameter;
import static de.openknowledge.customermanagement.architecture.conditions.IsRecordCondition.isRecord;
import static de.openknowledge.customermanagement.architecture.conditions.ReturnValidatedObjectRule.returnValidatedObjectConstructedFromParameters;
import static de.openknowledge.customermanagement.architecture.conditions.SatisfyPredicateCondition.satisfy;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.are;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.classesThat;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.have;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.methodsThat;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.that;
import static de.openknowledge.customermanagement.architecture.grammar.ArchUnitGrammarBlocks.where;
import static de.openknowledge.customermanagement.architecture.grammar.ParametersTransformer.parameters;
import static de.openknowledge.customermanagement.architecture.predicates.CanonicalConstructorPredicate.areCanonical;
import static de.openknowledge.customermanagement.architecture.predicates.ConstructorPredicate.constructorsThat;
import static de.openknowledge.customermanagement.architecture.predicates.DeclaredInPredicate.areDeclaredIn;
import static de.openknowledge.customermanagement.architecture.predicates.FactoryMethodPredicate.areFactoryMethods;
import static de.openknowledge.customermanagement.architecture.predicates.HaveParametersPredicate.haveParameters;
import static de.openknowledge.customermanagement.architecture.predicates.HaveSizePredicate.haveSize;
import static de.openknowledge.customermanagement.architecture.predicates.RecordTypeParameterPredicate.recordTypes;
import static de.openknowledge.customermanagement.architecture.predicates.RecordWithComponentsPredicate.haveAtLeastOneRecordComponent;
import static de.openknowledge.customermanagement.architecture.predicates.StringConstructorPredicate.areStringConstructors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import de.openknowledge.customermanagement.architecture.grammar.Types;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = ValueObjectTest.ROOT_PACKAGE)
class ValueObjectTest {

    static final String ROOT_PACKAGE = "de.openknowledge.customermanagement";

    @ArchTest
    static ArchRule restControllersAreAnnotatedWithValidated =
            classes()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .beAnnotatedWith(Validated.class)
                    .because(
                            "constraints on controller parameters (e.g. @PathVariable) are only"
                                    + " enforced via @Validated and reported as"
                                    + " ConstraintViolationException, which the"
                                    + " ValidationExceptionHandler maps to a response");

    @ArchTest
    static ArchRule restControllerParametersHaveValidOnRecordTypeParameters =
            all(parameters())
                    .that(areDeclaredIn(requestMappingMethods()))
                    .and(are(recordTypes()))
                    .should(beAnnotatedWith(Valid.class))
                    .because(
                            "request records must be validated when they enter the application;"
                                    + " without @Valid, Spring skips bean validation for them");

    @ArchTest
    static ArchRule restControllerParametersAreNeverSimpleTypes =
            all(parameters())
                    .that(areDeclaredIn(requestMappingMethods()))
                    .should(satisfy(not(simpleTypes())))
                    .because(
                            "raw values carry no constraints; controller parameters must be"
                                    + " value objects that validate themselves");

    @ArchTest
    static ArchRule recordComponentsOfRecordTypeAreAnnotatedWithValid =
            all(parameters())
                    .that(
                            areDeclaredIn(
                                    constructorsThat(
                                            areCanonical().and(declaredIn(valueObjects())))))
                    .and(are(recordTypes()))
                    .should(beAnnotatedWith(Valid.class))
                    .because(
                            "bean validation only cascades into nested records annotated with"
                                    + " @Valid, so a missing @Valid leaves them unvalidated");

    @ArchTest
    static ArchRule simpleRecordComponentsHaveBeanValidationConstraint =
            constructors()
                    .that()
                    .areDeclaredInClassesThat(are(valueObjects()))
                    .and(areCanonical())
                    .and(haveParameters(that(haveSize(1))))
                    .should(
                            haveParameters(
                                    every(
                                            beMetaAnnotatedWith(Constraint.class)
                                                    .or(satisfy(rawType(Boolean.TYPE))))))
                    .because(
                            "a record wrapping a single value must constrain it, otherwise any"
                                    + " value is accepted (a boolean has no invalid values)");

    @ArchTest
    static ArchRule canonicalConstructorIsOnlyCalledFromFactoryMethod =
            constructors()
                    .that()
                    .areDeclaredInClassesThat(are(valueObjects()))
                    .and(areCanonical())
                    .should()
                    .onlyBeCalled()
                    .byCodeUnitsThat(areFactoryMethods().or(areStringConstructors()))
                    .because(
                            "records must only be created through their new<RecordName> factory"
                                    + " method, which validates them (String constructors are"
                                    + " allowed for conversion, validation follows via @Valid)");

    @ArchTest
    static ArchRule canonicalConstructorAcceptsNull =
            constructors()
                    .that()
                    .areDeclaredInClassesThat(are(valueObjects()))
                    .and(areCanonical())
                    .should(acceptNullArguments())
                    .because(
                            "validation happens only in the factory method, after normalization,"
                                    + " so the canonical constructor must be null-safe");

    @ArchTest
    static ArchRule factoryMethodCallsValidate =
            methods()
                    .that()
                    .areDeclaredInClassesThat()
                    .areRecords()
                    .and(areFactoryMethods())
                    .should(returnValidatedObjectConstructedFromParameters())
                    .because(
                            "the factory method is the only way to create a record, so it must"
                                    + " return exactly the one instance it validated");

    @ArchTest
    static ArchRule recordsThatHaveMoreThanOneComponentHaveOnlyRecordComponents =
            constructors()
                    .that()
                    .areDeclaredInClassesThat(are(valueObjects()))
                    .and(areCanonical())
                    .should(
                            haveParameters(that(hasSize(1)))
                                    .or(haveParameters(that(areRecordsOrCollectionsOfRecords()))))
                    .because(
                            "composite records only combine other records, each of which"
                                    + " validates itself; raw values belong in their own value"
                                    + " object");

    private static final Set<Class<?>> WRAPPERS =
            Set.of(
                    Boolean.class,
                    Byte.class,
                    Character.class,
                    Short.class,
                    Integer.class,
                    Long.class,
                    Float.class,
                    Double.class,
                    Void.class,
                    String.class);

    private static ArchCondition<List<JavaParameter>> areRecordsOrCollectionsOfRecords() {
        return where(
                every(haveType(isRecord().or(isCollectionHavingTypeParameter(that(isRecord()))))));
    }

    private static DescribedPredicate<JavaParameter> simpleTypes() {
        return DescribedPredicate.describe(
                "primitive types, their wrappers or String",
                parameter -> {
                    Class<?> type = Types.elementTypeOf(parameter);
                    return type.isPrimitive() || WRAPPERS.contains(type);
                });
    }

    private static DescribedPredicate<JavaMember> requestMappingMethods() {
        return methodsThat(
                are(declaredIn(classesThatAreRestControllers()))
                        .and(have(modifier(JavaModifier.PUBLIC)))
                        .and(are(metaAnnotatedWith(RequestMapping.class))));
    }

    private static DescribedPredicate<CanBeAnnotated> classesThatAreRestControllers() {
        return classesThat(are(annotatedWith(RestController.class)));
    }

    private static DescribedPredicate<JavaClass> valueObjects() {
        return RECORDS.and(TOP_LEVEL_CLASSES)
                .and(not(annotatedWith(ConfigurationProperties.class)))
                .and(haveAtLeastOneRecordComponent());
    }
}
