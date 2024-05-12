FROM openjdk:11
WORKDIR /opt/usermanagement
COPY target/usermanagement-*.jar usermanagement.jar
CMD ["java", "-jar", "usermanagement.jar", "--spring.profiles.active=local"]