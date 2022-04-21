FROM adoptopenjdk/openjdk11
ARG JAR_FILE=target/docker/masters*.jar
ARG BOOTSTRAP_YML=target/docker/bootstrap.yml
RUN echo "${JAR_FILE}"
COPY ${JAR_FILE} app.jar
COPY ${BOOTSTRAP_YML} bootstrap.yml
ENTRYPOINT ["java","-jar","-Dspring.cloud.bootstrap.location=/bootstrap.yml","/app.jar"]