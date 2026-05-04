# 📦 Spring Modulith: Last-Mile Logistics Demo

This repository is the companion code for the **Kenya Java User Group (JUG)** presentation on building modular, event-driven applications using **Spring Modulith** and **Java 25**.

## 🎥 Pre-recorded Demo
* `[Watch the Demo Walkthrough](https://link-to-your-video)`

## 🚀 The Architecture
This project demonstrates a structurally enforced Monolith. It avoids complex message brokers (like Kafka) by leveraging Spring Modulith's **Event Publication Registry** and Java's **Virtual Threads** for asynchronous event handling.

### Modules:
1. `orders`: The entry point. Handles persistence and publishes `OrderPlacedEvent`.
2. `shipping`: Listens asynchronously to assign riders.
3. `billing`: Listens asynchronously to generate invoices.

*Note: There is zero direct method invocation between these modules. They communicate purely through events.*

## 🛠️ How to Run Locally

### Prerequisites
- JDK 25 installed (e.g., BellSoft Liberica)
- That's it! (Maven is bundled via the wrapper)

### Startup
Open your terminal and run:

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux/MacOS:**
```bash
./mvnw spring-boot:run
```

Watch the terminal output. You will see an automated `DemoRunner` place an order, and the asynchronous event listeners will immediately trigger on separate threads.

## 🛡️ Verifying the Architecture
To see Spring Modulith's architectural enforcement in action, run the test suite:
```powershell
.\mvnw.cmd test
```
This runs `ModularityTests.java`, which verifies that no module violates encapsulation rules and automatically generates updated PlantUML architecture diagrams in the `/target/spring-modulith-docs` directory.