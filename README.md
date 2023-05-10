# Before first run:

## New method(suitable only for Mac and Linux):
1. Make sure that you have `docker` and `docker compose` installed on your machine
2. Run `docker compose up` or `docker compose up -d` to use detached mode
### To use debugger:
1. Click on the "Add Configuration" button in the top right corner and select `Remote JVM debug`
2. Set whatever name you want
3. Make sure that port for debugging is `5005`
4. Save configuration and now you are free to use debug

## Old method(windows developers must use this instruction):

1. Click on the "Add Configuration" button in the top right corner and select "Docker Compose" from the list.
2. In the "Configuration file" field, specify the path to the docker-compose.yml file.
3. In the "Services" field, select the services that you want to start.
4. Click on "Apply" and "OK" to save the changes.
5. Open the Spring Boot startup configuration and in the "Before launch" section click on "+" to add a new task.
6. Select "Run Another Configuration" from the list.
7. In the "Configuration" field, select the Docker Compose configuration that you created earlier.
8. Click on "Apply" and "OK" to save the changes.
9. Now, when you click on the "Run" button in the upper menu, the selected services in the Docker Compose configuration will be started before the Spring Boot application.
10. It may not start from first time, if it fails, try next time in 10 seconds