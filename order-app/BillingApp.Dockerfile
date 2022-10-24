FROM azul/zulu-openjdk:11-jre
COPY ../../../../billing-app/build/libs/billing-app-0.1.0.jar .
ENTRYPOINT ["java", "-jar","/billing-app-0.1.0.jar"]