#!/bin/bash

set -e

IMAGE="keyword-kg-linker"

if [[ "$(docker images -q ${IMAGE})" == "" ]]
then
  echo "Building Docker image"
  docker build -t ${IMAGE} .
fi

if [[ "$#" -eq 4 ]]   # Indexing
then
  DATA_DIR=""
  PREDICATE=""

  if [[ $1 == "-dir" ]]
  then
    DATA_DIR=$2
  elif [[ $1 == "-predicate" ]]
  then
    PREDICATE=$2
  else
    echo "Option '$1' was not recognized"
    exit 1
  fi

  if [[ $3 == "-dir" ]]
  then
    DATA_DIR=$4
  elif [[ $3 == '-predicate' ]]
  then
    PREDICATE=$4
  else
    echo "Option '$3' was not recognized"
    exit 1
  fi

  if [[ ! -d ${DATA_DIR} ]]
  then
    echo "Directory '${DATA_DIR}' does not exist"
    exit 1
  fi

  docker run --rm -v ${PWD}/${DATA_DIR}:/data --network linker-dev ${IMAGE} \
      java -jar keywork-linker.jar index -dir /data -predicate ${PREDICATE}
elif [[ "$#" -eq 8 ]]   # Linking
then
  TABLE=""
  OUTPUT=""
  DIRECTORY=""
  CANDIDATES=""

  if [[ $1 == "-table" ]]
  then
    TABLE=$2
  elif [[ $1 == "-output" ]]
  then
    OUTPUT=$2
  elif [[ $1 == '-dir' ]]
  then
    DIRECTORY=$2
  elif [[ $1 == "-candidates" ]]
  then
    CANDIDATES=$2
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
  elif [[ $3 == "-candidates" ]]
  then
    CANDIDATES=$4
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
  elif [[ $5 == "-candidates" ]]
  then
    CANDIDATES=$6
  else
    echo "Option '$5' was not recognized"
  fi

  if [[ $7 == "-table" ]]
    then
      TABLE=$8
    elif [[ $7 == "-output" ]]
    then
      OUTPUT=$8
    elif [[ $7 == "-dir" ]]
    then
      DIRECTORY=$8
    elif [[ $7 == "-candidates" ]]
    then
      CANDIDATES=$8
    else
      echo "Option '$7' was not recognized"
    fi

  if [[ ! -f ${TABLE} ]]
  then
    echo "Table file '${TABLE}' does not exist"
    exit 1
  fi

  mkdir -p ${OUTPUT}
  TABLE_FILENAME=$(basename ${TABLE})
  TABLE_DIR=$(dirname $TABLE)
  docker run --rm -v ${PWD}/${DIRECTORY}:/data -v ${PWD}/${OUTPUT}:/output -v ${PWD}/${TABLE_DIR}:/table --network linker-dev \
      ${IMAGE} java -jar keywork-linker.jar link -table /table/${TABLE_FILENAME} -output /output -dir /data -candidates ${CANDIDATES}
else
  echo "Did not understand parameters"
fi