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

datafilename="$(basename $datafile)"
ext="${datafilename##*.}"
type=${type:-$ext}

echo "About to bulk load data from $datafile (of type $type) with config from file $configfile.\nPress any key to continue or ctrl-c to abort."
read

java -jar loader.jar --configfile=$configfile --datafile=$datafile  -type=$type

