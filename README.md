# Wallet Application
![Screenshot 2025-01-29 183316](https://github.com/user-attachments/assets/affc20bc-2a18-416e-b7fb-e457c112e343)

- &#9679; It consists of the following microservices -
  - user-service
  - wallet-service
  - transaction-service
  - notification-service

- &#9675; **Project Setup**
  1. Clone the project on your local machine.
  2. Build the project using Maven.  
     - *(In VS Code, you can probably do "Maven Reload" to get all the Spring dependencies.)*
  3. Open `src/main/resources/application.properties` in each microservice and update:
     ```properties
     spring.datasource.password=your_actual_mysql_password
     ```
- &#9679; **Run Project**
  - Run each microservice.
  - From the controller, you can get the endpoints to hit.
