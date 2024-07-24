#!/bin/bash

set -e

IMAGE="keyword-kg-linker"
CONTAINER="keyword-kg-linker-container"

if [[ "$#" -eq 6 ]]   # Indexing
then
  DATA_DIR=""
  KG_DIR=""
  CONFIG=""

  if [[ $1 == "-dir" ]]
  then
    DATA_DIR=$2
  elif [[ $1 == "-config" ]]
  then
    CONFIG=$2
  elif [[ $1 == "-kg" ]]
  then
    KG_DIR=$2
  else
    echo "Option '$1' was not recognized"
    exit 1
  fi

  if [[ $3 == "-dir" ]]
  then
    DATA_DIR=$4
  elif [[ $3 == '-config' ]]
  then
    CONFIG=$4
  elif [[ $3 == "-kg" ]]
  then
    KG_DIR=$4
  else
    echo "Option '$3' was not recognized"
    exit 1
  fi

  if [[ $5 == "-dir" ]]
  then
    DATA_DIR=$6
  elif [[ $5 == "-config" ]]
  then
    CONFIG=$6
  elif [[ $5 == "-kg" ]]
  then
    KG_DIR=$6
  else
    echo "Option '$5' was not recognized"
    exit 1
  fi

  if [[ ! -d ${DATA_DIR} ]]
  then
    echo "Directory '${DATA_DIR}' does not exist"
    exit 1
  fi

  if [[ ! -d ${KG_DIR} ]]
  then
    echo "Directory '${KG_DIR}' does not exist"
    exit 1
  fi

  # If image has not been built yet
  if [[ "$(docker images -q ${IMAGE})" == "" ]]
  then
    echo "Building Docker image"
    docker build -t ${IMAGE} . --build-arg CONFIG_FILE=${CONFIG} --no-cache
  fi

  docker run --rm -v ${PWD}/${DATA_DIR}:/data --network linker-dev --name ${CONTAINER} ${IMAGE} \
      java -jar keywork-linker.jar index -dir /data -config ${CONFIG}

elif [[ "$#" -eq 10 ]]   # Linking
then
  TABLES=""
  OUTPUT=""
  DIRECTORY=""
  CONFIG=""
  TYPE=""

  if [[ $1 == "-tables" ]]
  then
    TABLES=$2
  elif [[ $1 == "-output" ]]
  then
    OUTPUT=$2
  elif [[ $1 == '-dir' ]]
  then
    DIRECTORY=$2
  elif [[ $1 == "-config" ]]
  then
    CONFIG=$2
  elif [[ $1 == "-type" ]]
  then
    TYPE=$2
  else
    echo "Option '$1' was not recognized"
    exit 1
  fi

  if [[ $3 == "-output" ]]
  then
    OUTPUT=$4
  elif [[ $3 == "-tables" ]]
  then
    TABLES=$4
  elif [[ $3 == "-dir" ]]
  then
    DIRECTORY=$4
  elif [[ $3 == "-config" ]]
  then
    CONFIG=$4
  elif [[ $3 == "-type" ]]
    then
      TYPE=$4
  else
    echo "Option '$3' was not recognized"
    exit 1
  fi

  if [[ $5 == "-tables" ]]
  then
    TABLES=$6
  elif [[ $5 == "-output" ]]
  then
    OUTPUT=$6
  elif [[ $5 == "-dir" ]]
  then
    DIRECTORY=$6
  elif [[ $5 == "-config" ]]
  then
    CONFIG=$6
  elif [[ $5 == "-type" ]]
    then
      TYPE=$6
  else
    echo "Option '$5' was not recognized"
  fi

  if [[ $7 == "-tables" ]]
    then
      TABLES=$8
    elif [[ $7 == "-output" ]]
    then
      OUTPUT=$8
    elif [[ $7 == "-dir" ]]
    then
      DIRECTORY=$8
    elif [[ $7 == "-config" ]]
    then
      CONFIG=$8
    elif [[ $7 == "-type" ]]
      then
        TYPE=$8
    else
      echo "Option '$7' was not recognized"
    fi

  if [[ $9 == "-tables" ]]
      then
        TABLES=${10}
      elif [[ $9 == "-output" ]]
      then
        OUTPUT=${10}
      elif [[ $9 == "-dir" ]]
      then
        DIRECTORY=${10}
      elif [[ $9 == "-config" ]]
      then
        CONFIG=${10}
      elif [[ $9 == "-type" ]]
        then
          TYPE=${10}
      else
        echo "Option '$9' was not recognized"
      fi

  if [[ ! -d ${TABLES} && ! -f ${TABLES} ]]
  then
    echo "Table directory or file '${TABLES}' does not exist"
    exit 1
  fi

  # If image has not been built yet
  if [[ "$(docker images -q ${IMAGE})" == "" ]]
  then
    echo "Building Docker image"
    docker build -t ${IMAGE} . --build-arg CONFIG_FILE=${CONFIG} --no-cache
  fi

  mkdir -p ${OUTPUT}
  BASE_FILENAME=$(basename ${TABLES})
  TABLE_DIR=$(dirname $TABLES)
  docker run --rm -v ${PWD}/${DIRECTORY}:/data -v ${PWD}/${OUTPUT}:/output -v ${PWD}/${TABLE_DIR}:/tables --network linker-dev \
      --name ${CONTAINER} ${IMAGE} java -jar keywork-linker.jar link -tables /tables/${BASE_FILENAME} -output /output -dir /data -config ${CONFIG} -type ${TYPE}
else
  echo "Did not understand parameters"
fi
