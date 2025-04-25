# === CONFIGURATION ===
#  echo 'export PATH="/opt/homebrew/opt/tomcat@10/bin:$PATH"' >> ~/.zshrc

TOMCAT_HOME := /opt/homebrew/opt/tomcat@10/libexec
WAR_NAME := qweb-1.0-SNAPSHOT.war

# === TASKS ===

# Build the WAR file using Maven
build:
	mvn clean install

# Deploy WAR file to Tomcat's webapps directory
deploy: build
	cp qweb/target/$(WAR_NAME) $(TOMCAT_HOME)/webapps/

# Start Tomcat server
start:
	$(TOMCAT_HOME)/bin/startup.sh

debug:
	JPDA_ADDRESS=8000 JPDA_TRANSPORT=dt_socket ${TOMCAT_HOME}/bin/catalina.sh jpda start

# Stop Tomcat server
stop:
	$(TOMCAT_HOME)/bin/shutdown.sh

# Restart Tomcat server
restart: stop start

go: build deploy start

godbg: build deploy debug

# Tail Tomcat logs
logs:
	tail -f $(TOMCAT_HOME)/logs/catalina.out

# Clean Maven build output
clean:
	mvn clean
