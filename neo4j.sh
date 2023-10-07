#!/bin/bash

set -e

PORT_BULP="7687"
PORT="7474"
IMAGE="neo4j:4.1.4"

if [ "$#" -eq 1 ]
then
  NEO4J_HOME="${PWD}"
  NEO4J_IMPORT=${NEO4J_HOME}"/import"
  KG_DIR=$1

  if [[ ! "$(docker images -q ${IMAGE})" == "" ]]
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

  docker run -d -p ${PORT}:${PORT} -p ${PORT_BULP}:${PORT_BULP} --name neo4j -v ${PWD}/${KG_DIR}:/kg \
      -e NEO4J_AUTH=neo4j/admin \
      -e NEO4JLABS_PLUGINS='[\"apoc\", \"n10s\"]' \
      -e NEO4J_dbms_security_procedures_unrestricted=apoc.* \
      -e NEO4J_apoc_export_file_enabled=true \
      -e NEO4J_apoc_import_file_use_neo4j_config=false

  docker exec neo4j "wget -P plugins/ https://github.com/neo4j-labs/neosemantics/releases/download/4.1.0.1/neosemantics-4.1.0.1.jar && echo 'dbms.unmanaged_extension_classes=n10s.endpoint=/rdf' >> conf/neo4j.conf"
  docker restart neo4j

  echo "Import knowledge graph..."
  docker exec neo4j "${NEO4J_HOME}/bin/cypher-shell -u neo4j -p 'admin' \"CREATE CONSTRAINT n10s_unique_uri ON (r:Resource) ASSERT r.uri IS UNIQUE;\""
  docker exec neo4j "${NEO4J_HOME}/bin/cypher-shell -u neo4j -p 'jazero_admin' 'call n10s.graphconfig.init( { handleMultival: \"OVERWRITE\",  handleVocabUris: \"SHORTEN\", keepLangTag: false, handleRDFTypes: \"NODES\" })'"
  docker exec neo4j "rm -rf ${NEO4J_IMPORT}/*"

  docker exec neo4j "for f in /kg/* ; do FILE_CLEAN=$(basename ${f}) ; iconv -f utf-8 -t ascii -c "${f}" | grep -E '^<(https?|ftp|file)://[-A-Za-z0-9\+&@#/%?=~_|!:,.;]*[A-Za-z0-9\+&@#/%?=~_|]>\W<' | grep -Fv 'xn--b1aew' > ${NEO4J_IMPORT}/${FILE_CLEAN} ; done"
  docker exec  neo4j "for f in ${NEO4J_IMPORT}/* ; then filename=$(basename ${f}) ; ${NEO4J_HOME}/bin/cypher-shell -u neo4j -p 'admin' \"CALL  n10s.rdf.import.fetch(\"file://${NEO4J_IMPORT}/${filename}\",\"Turtle\");\" ; done"

  echo
  echo "Done"
else
  echo "Starting Neo4J..."
  echo

  docker start neo4j
fi