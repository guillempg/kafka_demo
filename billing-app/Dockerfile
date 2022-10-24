FROM azul/zulu-openjdk:11-jre
COPY build/libs/BillingApp-0.1.0.jar .
ADD src/main/resources/application.yml /app/application.yml
ENTRYPOINT ["java", "-jar","/billing-app-0.1.0.jar"]