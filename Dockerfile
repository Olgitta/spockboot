FROM tomcat:10.1-jdk17

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WARs to webapps folder
COPY booking-service/target/booking-service*.war /usr/local/tomcat/webapps/booking-service.war
COPY qweb/target/qweb*.war /usr/local/tomcat/webapps/qweb.war

# Expose Tomcat default port
EXPOSE 8080

CMD ["catalina.sh", "run"]
