FROM openjdk:17
ARG JAR_FILE=happytalk-hli.jar
COPY ${JAR_FILE} app.jar
COPY OrgAccess.properties ./
COPY . /CREAM_Happytalk_Gateway_HLI/
RUN mkdir -p /home/ec2-user/CREAM_Happytalk_Gateway_HLI/logs
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]