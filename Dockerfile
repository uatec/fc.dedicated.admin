FROM maven:3.2-jdk-8-onbuild
ENTRYPOINT ["java", "-jar", "target/admin-0.1.0.jar"]