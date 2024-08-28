#!/bin/sh
if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <AVRO SCHEMA DIR> <SCHEMA REGISRTY ADDRESS>" >&2
  exit 1
fi

docker pull armdocker.rnd.ericsson.se/aia/schema-registry-importer
docker run -it --rm -v $1://avro-schemas armdocker.rnd.ericsson.se/aia/schema-registry-importer -dir=//avro-schemas -registry=$2
