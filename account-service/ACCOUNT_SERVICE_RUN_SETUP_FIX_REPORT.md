# Account Service Run Setup Fix Report

## 1. Initial error

From the VS Code PowerShell terminal, both `mvn spring-boot:run` and
`.\mvnw.cmd spring-boot:run` failed because Maven was not on `PATH` and the
repository did not contain Maven Wrapper files.

## 2. Build system detected

- Build system: Maven multi-module project
- Account Service build file: `account-service/pom.xml`
- Parent build file: `pom.xml`
- Packaging: `jar`
- Spring Boot: 3.3.4
- Required Java release: 17
- Main class: `com.ridelink.account.AccountServiceApplication`
- Spring Boot Maven plugin: present
- Gradle build/wrapper: not present

The POM files are valid XML and no dependency or plugin version was changed.

## 3. Java status

Initial shell state:

- `java -version`: Java 24
- `javac -version`: Java 24
- `JAVA_HOME`: `C:\Program Files\Java\jdk-11.0.20.1`
- Java selected by `PATH`: Oracle Java path shim

This was inconsistent with the project's Java 17 requirement. A compatible JDK
is already installed at `C:\Program Files\Java\jdk-17`. The verified commands
below select that JDK for the current PowerShell session without changing the
machine-wide configuration.

## 4. Maven status

`mvn` is not installed on, or accessible through, the system `PATH`.
IntelliJ IDEA includes Apache Maven 3.9.9 at:

`C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd`

That Maven installation was used only to generate the project wrapper. A
`winget search Maven` check could not execute in the verification environment,
so no unverified Winget package ID is recommended.

If system-wide Maven is wanted later, download the Apache Maven binary ZIP from
the official Apache Maven website, extract it to a stable directory such as
`C:\Program Files\Apache\maven`, optionally set `MAVEN_HOME` to that directory,
add its `bin` directory to the user or system `PATH`, restart VS Code, and run
`mvn -version`. System Maven is no longer required for this Account Service
because the wrapper is now present.

## 5. Maven Wrapper status

The initial repository, sibling services, README files, and available Git
history contained no Maven Wrapper. Because the documented project build system
is Maven but the target machine has no `mvn` command, a module-local wrapper was
generated for Account Service only.

- Maven Wrapper plugin: 3.3.4
- Wrapped Maven distribution: 3.9.9
- Distribution type: `only-script`
- Wrapper URL: official Maven Central HTTPS URL

Created files:

- `account-service/mvnw`
- `account-service/mvnw.cmd`
- `account-service/.mvn/wrapper/maven-wrapper.properties`

No wrapper files were copied from another service.

## 6. Root cause

There were two environment/tooling problems:

1. Maven was not available on `PATH`, and no Maven Wrapper existed.
2. The terminal used Java 24 while `JAVA_HOME` pointed to Java 11; the project
   explicitly requires Java 17.

The wrapper fixes the first problem. Selecting the installed JDK 17 in the
terminal fixes the second.

## 7. Files changed

- Added `mvnw`
- Added `mvnw.cmd`
- Added `.mvn/wrapper/maven-wrapper.properties`
- Added `ACCOUNT_SERVICE_RUN_SETUP_FIX_REPORT.md`

No Java source, Account Service business behavior, POM dependency, other
microservice, shared architecture, Postman file, or secret file was changed.

## 8. Commands executed

The material verification commands were:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

& "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd" -N wrapper:wrapper "-Dmaven=3.9.9"
.\mvnw.cmd -version
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

## 9. Test result

**PASS**

- Tests run: 23
- Failures: 0
- Errors: 0
- Skipped: 0
- Maven result: `BUILD SUCCESS`
- Compiler release: Java 17

## 10. Startup result

**BLOCKED**

The wrapper successfully compiled the service and invoked Spring Boot under
Java 17. Tomcat initialized on port 8081. Application-context creation then
stopped because the required `JWT_SECRET` environment variable was deliberately
not available in the verification environment.

This is an environment prerequisite failure, not a compilation or application
logic failure. A live MongoDB connection and HTTP checks require the user's real
Atlas URI and secret; they were not fabricated or printed.

## 11. Actual port

Configured default and startup-log port: `8081`

Account Service URL after successful configuration:

`http://localhost:8081`

## 12. Swagger URL

Configured Swagger UI:

`http://localhost:8081/swagger-ui/index.html`

Configured OpenAPI document:

`http://localhost:8081/v3/api-docs`

The paths were verified from configuration and security rules. Live HTTP loading
is blocked until valid runtime environment variables allow the service to remain
running.

## 13. Required environment variables

The following names come directly from `application.yml`:

| Variable | Required | Default |
|---|---:|---|
| `MONGODB_URI` | Yes | None |
| `MONGODB_DATABASE` | No | `ridelink_account_db` |
| `JWT_SECRET` | Yes | None; must contain at least 32 bytes |
| `JWT_EXPIRATION_MS` | No | `3600000` |
| `ACCOUNT_SERVICE_PORT` | No | `8081` |

Do not use the literal placeholder text as a runtime value and do not commit real
credentials.

## 14. Final VS Code PowerShell commands

Run this from a new or existing VS Code PowerShell terminal:

```powershell
cd "D:\IT3130_AD_Group_Assignment_RideLink\account-service"

$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

java -version
.\mvnw.cmd -version

$env:MONGODB_URI = "<YOUR_MONGODB_ATLAS_URI>"
$env:MONGODB_DATABASE = "ridelink_account_db"
$env:JWT_SECRET = "<YOUR_CRYPTOGRAPHICALLY_RANDOM_SECRET_OF_AT_LEAST_32_BYTES>"
$env:JWT_EXPIRATION_MS = "3600000"
$env:ACCOUNT_SERVICE_PORT = "8081"

.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Replace both angle-bracketed placeholders with real local values before running
the final command. If the Atlas password contains reserved URI characters, URL
encode it and ensure the current client IP is permitted in Atlas Network Access.

## 15. Remaining blockers

- A real `MONGODB_URI` was not provided to this verification environment.
- A real `JWT_SECRET` was not provided to this verification environment.
- Atlas authentication/network connectivity therefore could not be tested.
- Live Swagger and OpenAPI HTTP responses could not be tested because startup
  correctly stopped when the required secret was absent.

Once those two required values are set in the same PowerShell session, use
`.\mvnw.cmd spring-boot:run`; no system-wide Maven installation is needed.
