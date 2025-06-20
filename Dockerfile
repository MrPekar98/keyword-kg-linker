FROM ubuntu:20.04

WORKDIR /home
ARG CONFIG_FILE

RUN apt update
RUN DEBIAN_FRONTEND=noninteractive apt install openjdk-17-jdk openjdk-17-jre curl zip python3 pip -y
RUN curl -O https://dlcdn.apache.org/maven/maven-3/3.9.10/binaries/apache-maven-3.9.10-bin.zip
RUN unzip apache-maven-3.9.10-bin.zip
RUN rm apache-maven-3.9.10-bin.zip
RUN pip3 install -U sentence-transformers==3.2.1

ADD ${CONFIG_FILE} .
ADD pom.xml .
ADD src/ src/
ADD embeddings.py .
RUN ./apache-maven-3.9.10/bin/mvn package
RUN mv target/keyword-kg-linker*with-dependencies.jar keywork-linker.jar

CMD []
