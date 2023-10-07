FROM ubuntu:18.04

RUN apt update
RUN apt install cmake g++ make apt install software-properties-common git -y
RUN add-apt-repository ppa:cleishm/neo4j
RUN apt-get update
RUN apt-get install neo4j-client libneo4j-client-dev -y

WORKDIR /deps
RUN git clone git://clucene.git.sourceforge.net/gitroot/clucene/clucene
RUN mkdir clucene/build
WORKDIR /deps/clucene/build
RUN cmake ..
RUN make
RUN make install
WORKDIR /deps
RUN rm -rf clucene && mkdir CLucene/ && mv /usr/local/include/* CLucene/

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

CMD ["./build/keyword-kg-linker", "-index", "/data"]