FROM ubuntu:18.04

RUN apt update
RUN apt install cmake g++ make apt install software-properties-common -y
RUN add-apt-repository ppa:cleishm/neo4j
RUN apt-get update
RUN apt-get install neo4j-client libneo4j-client-dev -y

WORKDIR /linker
RUN mkdir src/
RUN mkdir include/
RUN mkdir build/
ADD src/ src/
ADD include/ include/

WORKDIR build/
RUN cmake ..

WORKDIR /linker
RUN cmake --build -j 6 .

ENTRYPOINT ["./build/keyword-kg-linker"]