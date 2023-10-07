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
  mkdir -p .data
  docker run --rm -v ${PWD}/.data:/data ${IMAGE}
elif [[ "$#" -eq 4 ]]
then
  TABLE=""
  OUTPUT=""

  if [[ $1 == "-table" ]]
  then
    TABLE=$2
  elif [[ $1 == "-output" ]]
  then
    OUTPUT=$2
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
  else
    echo "Option '$3' was not recognized"
    exit 1
  fi

  if [[ ! -f ${TABLE} ]]
  then
    echo "Table file '${TABLE}' does not exist"
    exit 1
  fi

  mkdir -p {OUTPUT}
  TABLE_FILENAME=$(basename ${TABLE})
  TABLE_DIR=$(dirname $TABLE)
  docker run --rm -v ${PWD}/.data:/data -v ${PWD}/${OUTPUT}:/output -v ${PWD}/${TABLE_DIR}:/table ${IMAGE} ./build/keyword-kg-linker -table /table/${TABLE_FILENAME} -output /output
fi