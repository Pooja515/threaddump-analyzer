# Java Thread Dump Analyzer 

A robust Java Spring Boot backend application designed to parse, analyze, and diagnose Java thread dump files. This tool helps developers and DevOps engineers quickly identify performance bottlenecks, deadlocks, CPU spikes, and thread contention in their Java applications.

---

## Features

* **Thread Dump Analysis:** Parses standard Java thread dump files to extract critical thread states (RUNNABLE, BLOCKED, WAITING, etc.).
* **Remote Capture & Inspection:** Enhanced controller support allowing both local file processing and remote thread dump capture.
* **Diagnostics:** Helps pinpoint deadlocks, long-running threads, and resource contention.
* **REST API Powered:** Built as a headless Spring Boot service, easily integrable with any frontend UI or CLI tool.

---

##  Repository Structure

```text
├── .mvn/                  # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/         # Spring Boot application source code
│   │   └── resources/    # Application configurations (application.properties/yml)
├── mvnw                   # Maven wrapper execution script for Linux/macOS
├── mvnw.cmd               # Maven wrapper execution script for Windows
└── pom.xml                # Project dependencies and build configuration

Prerequisites
Before running this project, ensure you have the following installed:

Java Development Kit (JDK): Version 17 or higher recommended.

Maven: (Optional, as the repository includes the Maven wrapper mvnw).

Getting Started
1. Clone the Repository
Bash
git clone [https://github.com/Pooja515/threaddump-analyzer.git](https://github.com/Pooja515/threaddump-analyzer.git)
cd threaddump-analyzer
2. Build the Application
Use the Maven wrapper to clean and build the project packages:

Bash
# On Linux/macOS
./mvnw clean install

# On Windows
mvnw.cmd clean install
3. Run the Application
Start the Spring Boot backend server:

Bash
# On Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run
The server will typically spin up on http://localhost:8080 (unless configured otherwise in your application.properties).

API Usage
The application exposes REST endpoints via the ThreadDumpController to manage your analyses:

Local Analysis: Upload or point to a local thread dump text file to receive a structured JSON diagnostic summary.

Remote Capture: Trigger and capture a thread dump remotely from a target application instance for immediate analysis.

(Tip: You can use tools like Postman, cURL, or an interactive Swagger UI if configured to test the endpoints.)

Contributing
Contributions make the open-source community an amazing place to learn, inspire, and create.

Fork the Project.

Create your Feature Branch (git checkout -b feature/AmazingFeature).

Commit your Changes (git commit -m 'Add some AmazingFeature').

Push to the Branch (git push origin feature/AmazingFeature).

Open a Pull Request.
