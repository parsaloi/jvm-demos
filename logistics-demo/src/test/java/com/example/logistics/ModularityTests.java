package com.example.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    // Analyzes the package structure based on the main application class
    ApplicationModules modules = ApplicationModules.of(LogisticsApplication.class);

    @Test
    void verifyModularity() {
        // This is the gatekeeper. It fails the build if e.g., 'orders' tries 
        // to directly import a class from 'shipping.internal'
        modules.verify(); 
    }

    @Test
    void writeDocumentation() {
        // Generates PlantUML diagrams representing your architecture
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml();
    }
}