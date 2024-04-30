#!/bin/bash

set -e

PORT_BULP="7687"
PORT="7474"
IMAGE="neo4j:4.1.4"

if [ "$#" -eq 1 ]
then
  NEO4J_HOME="/var/lib/neo4j"
  NEO4J_IMPORT=${NEO4J_HOME}"/import"
  KG_DIR=$1

  if [[ "$(docker images -q ${IMAGE})" == "" ]]
  then
    echo "Neo4J has not been installed"
    echo "Installing..."
    echo

    docker pull ${IMAGE}
  fi

  if [[ ! -d ${KG_DIR} ]]
  then
    echo "Knowledge graph directory '${KG_DIR}' does not exist"
    exit 1
  fi

  docker network inspect linker-dev > /dev/null 2>&1 || docker network create --driver bridge linker-dev
  docker run -d -p ${PORT}:${PORT} --network linker-dev -p ${PORT_BULP}:${PORT_BULP} --name neo4j-linker -v ${PWD}/${KG_DIR}:/kg \
      -e NEO4J_AUTH=none \
      -e NEO4JLABS_PLUGINS='[\"apoc\", \"n10s\"]' \
      -e NEO4J_dbms_security_procedures_unrestricted=apoc.* \
      -e NEO4J_apoc_export_file_enabled=true \
      -e NEO4J_apoc_import_file_use_neo4j_config=false \
      ${IMAGE}

  echo "Installing Neosemantics..."
  sleep 1m
  docker exec neo4j-linker wget -P plugins/ https://github.com/neo4j-labs/neosemantics/releases/download/4.1.0.1/neosemantics-4.1.0.1.jar
  docker exec neo4j-linker bash -c "echo 'dbms.unmanaged_extension_classes=n10s.endpoint=/rdf' >> conf/neo4j.conf"
  docker restart neo4j-linker

  echo "Importing knowledge graph..."
  sleep 1m
  docker cp load.sh neo4j-linker:/var/lib/neo4j
  docker exec neo4j-linker bash -c "./load.sh ${NEO4J_HOME} ${NEO4J_IMPORT}"

  echo
  echo "Done"
else
  if [[ "$(docker images -q ${IMAGE})" == "" ]]
  then
    echo "Docker image does not exist. Add knowledge graph directory as parameter to setup Neo4J."
    exit 1
  fi

  echo "Starting Neo4J..."
  echo

  docker start neo4j-linker
fi
