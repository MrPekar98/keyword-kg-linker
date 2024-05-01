FROM ubuntu:20.04

WORKDIR /home
ARG CONFIG_FILE

RUN apt update
RUN apt install openjdk-17-jdk openjdk-17-jre curl zip -y
RUN curl -O https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip
RUN unzip apache-maven-3.9.5-bin.zip
RUN rm apache-maven-3.9.5-bin.zip

ADD ${CONFIG_FILE} .
ADD pom.xml .
ADD src/ src/
RUN ./apache-maven-3.9.5/bin/mvn package
RUN mv target/keyword-kg-linker*with-dependencies.jar keywork-linker.jar

CMD []
