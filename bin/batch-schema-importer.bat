@echo off

set _argcActual=0
set _argcExpected=2

echo.

for %%i in (%*) do set /A _argcActual+=1

if %_argcActual% NEQ %_argcExpected% (

  echo "Usage: %~0 <AVRO SCHEMA DIR> <SCHEMA REGISRTY ADDRESS>"
  goto:_EOF
)

docker pull armdocker.rnd.ericsson.se/aia/schema-registry-importer
docker run -it --rm -v $1://avro-schemas armdocker.rnd.ericsson.se/aia/schema-registry-importer -dir=//avro-schemas -registry=$2

:_EOF
 