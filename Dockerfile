FROM openjdk:17
ARG JAR_FILE=happytalk-hli.jar
COPY ${JAR_FILE} app.jar
COPY OrgAccess.properties ./
COPY . /HLI_Happytalk_Gateway/
RUN mkdir -p /home/ec2-user/HLI_Happytalk_Gateway/logs
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]