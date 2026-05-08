# 📦 Spring Modulith: Last-Mile Logistics Demo

This repository is the companion code for the **Kenya Java User Group (JUG)** presentation on building modular, event-driven applications using **Spring Modulith** and **Java 25**.

## 🎥 Demo
[![asciicast](https://asciinema.org/a/KPxdJEGoT3yqeS0K.svg)](https://asciinema.org/a/KPxdJEGoT3yqeS0K)

## 🚀 The Architecture
This project demonstrates a structurally enforced Monolith leveraging Spring Modulith's **Event Publication Registry** and Java's **Virtual Threads** for asynchronous event handling.

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

## Troubleshoot
You may get a maven build failure with a "release 25 not supported" error message.  
* Verify what the complier sees:
  **Windows:**
  ```powershell
  .\mvnw.cmd --version
  # Expected output
  Apache Maven 3.9.15 (98b2cdbfdb5f1ac8781f537ea9acccaed7922349)
  Maven home: C:\Users\mypc\.m2\wrapper\dists\apache-maven-3.9.15\0226a00282e400185496f3b60ec5a3f029cbdc6893912937d4876d57695224e1
  Java version: 25.0.3, vendor: BellSoft, runtime: C:\Program Files\BellSoft\LibericaJDK-25-Full
  Default locale: en_US, platform encoding: UTF-8
  OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
  ```

  **Linux/MacOS:**
  ```bash
  ./mvnw --version
  ```
* If you have maven installed locally try use it to run the application.  
  **Windows:**
  ```powershell
  mvn.cmd clean spring-boot:run
  ```

  **Linux:**
  ```bash
  mvn clean spring-boot:run
  ```
