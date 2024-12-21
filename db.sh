#!/bin/bash

set -e

USERNAME=postgres
PASSWORD=1234
DB_NAME=postgres
IMAGE="timescale/timescaledb-ha:pg16"
CONTAINER="timescaledb"
VECTOR_DIMENSION=384

if [[ "$(docker images -q ${IMAGE})" == "" ]]
then
  docker pull ${IMAGE}
fi

if [[ ! "$(docker ps -q -f name=${CONTAINER})" ]]
then
  docker run -d --name ${CONTAINER} --network linker-dev -p 5432:5432 -e POSTGRES_PASSWORD=${PASSWORD} timescale/timescaledb-ha:pg16
  sleep 10s
  docker exec ${CONTAINER} bash -c "psql -U postgres -c 'CREATE EXTENSION IF NOT EXISTS vectorscale CASCADE;'"
  docker exec ${CONTAINER} bash -c "psql -U postgres -c 'CREATE TABLE IF NOT EXISTS embeddings (id BIGINT PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY, uri TEXT, embedding VECTOR(${VECTOR_DIMENSION}));'"
  docker exec ${CONTAINER} bash -c "psql -U postgres -c 'CREATE INDEX embedding_idx ON embeddings USING diskann (embedding);'"
elif [[ "$(docker container inspect -f '{{.State.Running}}' ${CONTAINER})" != "true" ]]
then
  docker start ${CONTAINER}
fi
