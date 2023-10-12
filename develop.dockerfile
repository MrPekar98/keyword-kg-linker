FROM ubuntu:18.04

RUN apt update
RUN apt install build-essential manpages-dev gdb cmake g++ make software-properties-common libssl-dev git wget zlib1g-dev xz-utils libxapian-dev uuid-dev -y
RUN add-apt-repository ppa:cleishm/neo4j
RUN apt-get update
RUN apt-get install neo4j-client libneo4j-client-dev -y

WORKDIR /deps
RUN wget https://oligarchy.co.uk/xapian/1.4.23/xapian-core-1.4.23.tar.xz -O xapian.tar.xz
RUN xz -dc xapian.tar.xz | tar xf -
RUN rm xapian.tar.xz
WORKDIR xapian-core-1.4.23
RUN ./configure --prefix=/opt
RUN make
RUN make install
WORKDIR /deps
RUN mkdir xapian
RUN mv /opt/include/* xapian/

WORKDIR /linker

CMD []
