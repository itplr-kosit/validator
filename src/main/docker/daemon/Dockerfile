FROM openjdk:8

RUN mkdir /opt/validationtool
ADD maven/validationtool-standalone.jar /opt/validationtool
ADD run.sh /opt/validationtool/
ADD config/ /opt/validationtool/
EXPOSE 8080
ENTRYPOINT ["bash", "/opt/validationtool/run.sh" ]