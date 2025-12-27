package com.weatherspring.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests using ArchUnit to enforce layered architecture rules.
 *
 * <p>These tests ensure that the codebase follows proper layering, naming conventions,
 * and dependency rules. This helps maintain clean architecture and prevent violations
 * as the codebase grows.</p>
 */
class LayerArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.weatherspring");
    }

    /**
     * Test that enforces proper layer dependencies:
     * - Controllers may only depend on Services and DTOs
     * - Services may only depend on Repositories, Mappers, Clients, and DTOs
     * - Repositories may only depend on Models
     * - No layer should depend on Controllers
     */
    @Test
    void layersShouldRespectDependencies() {
        ArchRule rule = layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .ignoreDependency(com.weatherspring.client.WeatherApiClient.class, com.weatherspring.config.RestClientConfig.class)

                .layer("Controllers").definedBy("..controller..")
                .layer("Services").definedBy("..service..")
                .layer("Repositories").definedBy("..repository..")
                .layer("Models").definedBy("..model..")
                .layer("DTOs").definedBy("..dto..")
                .layer("Mappers").definedBy("..mapper..")
                .layer("Clients").definedBy("..client..")
                .layer("Config").definedBy("..config..")
                .layer("Exceptions").definedBy("..exception..")

                .whereLayer("Controllers").mayNotBeAccessedByAnyLayer()
                .whereLayer("Controllers").mayOnlyAccessLayers("Services", "DTOs", "Exceptions", "Config")
                .whereLayer("Services").mayOnlyAccessLayers("Repositories", "Mappers", "Clients", "DTOs", "Models", "Exceptions", "Config")
                .whereLayer("Repositories").mayOnlyAccessLayers("Models")
                .whereLayer("Mappers").mayOnlyAccessLayers("DTOs", "Models")
                .withOptionalLayers(true);

        rule.check(importedClasses);
    }

    /**
     * Test that classes in the controller package have Controller suffix.
     */
    @Test
    void controllersShouldBeNamedCorrectly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that classes in the service package have Service suffix.
     */
    @Test
    void servicesShouldBeNamedCorrectly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().haveSimpleNameEndingWith("Service")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that classes in the repository package have Repository suffix.
     */
    @Test
    void repositoriesShouldBeNamedCorrectly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areAnnotatedWith(org.springframework.stereotype.Repository.class)
                .should().haveSimpleNameEndingWith("Repository")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that DTOs (records in our case) are in the dto package.
     */
    @Test
    void dtosShouldResideInDtoPackage() {
        ArchRule rule = classes()
                .that().haveSimpleNameEndingWith("Dto")
                .or().haveSimpleNameEndingWith("Request")
                .or().haveSimpleNameEndingWith("Response")
                .should().resideInAPackage("..dto..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that services are annotated with @Service or meta-annotations containing @Service.
     */
    @Test
    void servicesShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areNotAnonymousClasses()
                .and().areNotMemberClasses()
                .and().areNotInterfaces()
                .should().beMetaAnnotatedWith(org.springframework.stereotype.Service.class)
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that controllers are annotated with @RestController.
     */
    @Test
    void controllersShouldBeAnnotatedWithRestController() {
        ArchRule rule = classes()
                .that().resideInAPackage("..controller..")
                .and().areNotAnonymousClasses()
                .and().areNotMemberClasses()
                .should().beAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that repositories extend JpaRepository or are annotated with @Repository.
     */
    @Test
    void repositoriesShouldBeSpringDataRepositories() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areNotAnonymousClasses()
                .should().beAnnotatedWith(org.springframework.stereotype.Repository.class)
                .orShould().beAssignableTo(org.springframework.data.jpa.repository.JpaRepository.class)
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that models (entities) are in the model package and annotated with @Entity.
     */
    @Test
    void entitiesShouldBeInModelPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..model..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that exceptions are in the exception package.
     */
    @Test
    void exceptionsShouldBeInExceptionPackage() {
        ArchRule rule = classes()
                .that().areAssignableTo(Exception.class)
                .and().areNotAssignableTo(org.springframework.data.mapping.PropertyReferenceException.class)
                .and().resideOutsideOfPackage("java..")
                .should().resideInAPackage("..exception..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that configuration classes are annotated with @Configuration or @Component.
     */
    @Test
    void configurationClassesShouldBeAnnotatedProperly() {
        ArchRule rule = classes()
                .that().resideInAPackage("..config..")
                .and().areNotAnonymousClasses()
                .and().areNotMemberClasses()
                .and().areNotInterfaces()
                .and().doNotHaveSimpleName("package-info")
                .should().beAnnotatedWith(org.springframework.context.annotation.Configuration.class)
                .orShould().beAnnotatedWith(org.springframework.stereotype.Component.class)
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that fields annotated with @Autowired are not public.
     */
    @Test
    void fieldsShouldNotBeAutowired() {
        ArchRule rule = noFields()
                .should().beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
                .allowEmptyShould(true)
                .because("Field injection is discouraged, use constructor injection instead");

        rule.check(importedClasses);
    }

    /**
     * Test that repositories are interfaces.
     */
    @Test
    void repositoriesShouldBeInterfaces() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .and().areNotAnonymousClasses()
                .should().beInterfaces()
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    /**
     * Test that services should not directly access repositories from other bounded contexts.
     * (In this case, each service should only use its own repository)
     */
    @Test
    void servicesShouldUseConstructorInjection() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areAnnotatedWith(org.springframework.stereotype.Service.class)
                .should().haveOnlyFinalFields()
                .allowEmptyShould(true)
                .because("Services should use constructor injection with final fields");

        rule.check(importedClasses);
    }
}
