# Getting Started

### Setup
Dependencies:

- java jdk 1.8
- maven
- MySQL

Change your MySQL server in application.properties

Running tests:
- mvn test

Running a server:
- mvn clean compile
- mvn install -DskipTests=true
- java -jar target/todo-0.0.1-SNAPSHOT.jar

### API Usage
Project uses Swagger as API Document

After run project you can see full api in json or try to run on Swagger UI 
- Json: http://localhost:8080/v2/api-docs
- Swagger UI: http://localhost:8080/swagger-ui.html
