#!/bin/sh

while getopts ":c:d:t:" opt; do
    case $opt in
        c) configfile=$OPTARG
        ;;
        d) datafile=$OPTARG
        ;;
        t) type=$OPTARG
        ;;
    esac
done
shift $((OPTIND-1))

if [ -z "$configfile" ] || [ -z "$datafile" ] || [ -z "$type" ]; then
  echo "Usage: $0 -c <configfile> -d <datafile> -t <type>"
  exit 1
fi

datafilename="$(basename $datafile)"
ext="${datafilename##*.}"
type=${type:-$ext}

echo "About to bulk load data from $datafile (of type $type) with config from file $configfile.\nPress any key to continue or ctrl-c to abort."
read

java -jar ../loader/build/libs/loader.jar --configfile=$configfile --datafile=$datafile  -type=$type

