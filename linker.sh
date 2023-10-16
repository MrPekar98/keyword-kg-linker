#!/bin/bash

set -e

IMAGE="keyword-kg-linker"

if [[ "$(docker images -q ${IMAGE})" == "" ]]
then
  echo "Building Docker image"
  docker build -t ${IMAGE} .
fi

if [[ "$#" -eq 2 ]]
then
  DATA_DIR=$2

  if [[ ! -d ${DATA_DIR} ]]
  then
    echo "Directory '${DATA_DIR}' does not exist"
    exit 1
  fi

  docker run --rm -v ${PWD}/${DATA_DIR}:/data ${IMAGE} \
      java -jar keywork-linker.jar index -dir /data
elif [[ "$#" -eq 6 ]]
then
  TABLE=""
  OUTPUT=""
  DIRECTORY=""

  if [[ $1 == "-table" ]]
  then
    TABLE=$2
  elif [[ $1 == "-output" ]]
  then
    OUTPUT=$2
  elif [[ $1 == 'dir' ]]
  then
    DIRECTORY=$2
  else
    echo "Option '$1' was not recognized"
    exit 1
  fi

  if [[ $3 == "-output" ]]
  then
    OUTPUT=$4
  elif [[ $3 == "-table" ]]
  then
    TABLE=$4
  elif [[ $3 == "-dir" ]]
  then
    DIRECTORY=$4
  else
    echo "Option '$3' was not recognized"
    exit 1
  fi

  if [[ $5 == "-table" ]]
  then
    TABLE=$6
  elif [[ $5 == "-output" ]]
  then
    OUTPUT=$6
  elif [[ $5 == "-dir" ]]
  then
    DIRECTORY=$6
  else
    echo "Option '$5' was not recognized"
  fi

  if [[ ! -f ${TABLE} ]]
  then
    echo "Table file '${TABLE}' does not exist"
    exit 1
  fi

  mkdir -p {OUTPUT}
  TABLE_FILENAME=$(basename ${TABLE})
  TABLE_DIR=$(dirname $TABLE)
  docker run --rm -v ${PWD}/${DIRECTORY}:/data -v ${PWD}/${OUTPUT}:/output -v ${PWD}/${TABLE_DIR}:/table \
      ${IMAGE} java -jar keywork-linker.jar link -table /table/${TABLE_FILENAME} -output /output -dir /data
else
  echo "Did not understand parameters"
fi