#!/bin/bash

set -e

KG=$1

if [[ ! -d ${KG} ]]
then
  echo "Directory '${KG}' does not exist"
else
  export KG_DIR=${KG}
  mkdir -p output
  docker create network linking-net
fi