#!/bin/bash

set -e

NEO4J_HOME=$1
NEO4J_IMPORT=$2

${NEO4J_HOME}/bin/cypher-shell -u neo4j -p 'admin' "CREATE CONSTRAINT n10s_unique_uri ON (r:Resource) ASSERT r.uri IS UNIQUE;"
${NEO4J_HOME}/bin/cypher-shell -u neo4j -p 'admin' 'call n10s.graphconfig.init( { handleMultival: "OVERWRITE",  handleVocabUris: "SHORTEN", keepLangTag: false, handleRDFTypes: "NODES" })'
rm -rf import/*

for f in /kg/* ; \
do
    FILE_CLEAN=$(basename ${f})
    iconv -f utf-8 -t ascii -c "${f}" | grep -E '^<(https?|ftp|file)://[-A-Za-z0-9\+&@#/%?=~_|!:,.;]*[A-Za-z0-9\+&@#/%?=~_|]>\W<' | grep -Fv 'xn--b1aew' > ${NEO4J_IMPORT}/${FILE_CLEAN}
done

for f in ${NEO4J_IMPORT}/* ; \
do
    filename=$(basename ${f})
    ${NEO4J_HOME}/bin/cypher-shell -u neo4j -p 'admin' "CALL  n10s.rdf.import.fetch(\"file://${NEO4J_IMPORT}/${filename}\",\"Turtle\");"
done


