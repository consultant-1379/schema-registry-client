#!/bin/sh
docker run -it --rm -v //c//Users//eguoduu//ericsson//src//schema-registry-client//src//test//resources//test-schema-registry-cluster//step-3-import-data//import-from-10012//sample-schema://avro-schemas armdocker.rnd.ericsson.se/aia/schema-registry-importer -dir=//avro-schemas -registry=http://192.168.99.100:10012/

