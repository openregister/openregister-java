# mint
Accepts updates to a register.

[![Build Status](https://travis-ci.org/openregister/mint.svg?branch=master)](https://travis-ci.org/openregister/mint)

# Requirements

- Java 1.8+
- Postgres DB 9.4
- Apache Kafka
- Zookeeper

# Build and Run project

- Install and run Rabbit MQ
- Install and run postgres db
- Create database mint `createdb mint` to use application locally.
- Create database test_mint `createdb test_mint` to run unit tests.
- Create database ft_mint `createdb ft_mint` to run functional tests.
- Checkout project 
- Use command `./gradlew cleanIdea idea` to generate the idea project files
- Build project using command `./gradlew clean build` 

# Bulk load data from CLI

There are 2 options:

1. Run via gradle:
-     `gradle bulkLoad -PmintUrl=<mint dataload url> -Pdatafile=<loadfile.json> [-Ptype=jsonl|tsv|csv]`
        e.g. gradle bulkLoad -PmintUrl=http://localhost:4567/load -Pdatafile=datafile.tsv -Ptype=tsv
2. Run using the built jar
-     `java -jar <path_to_jar> LoaderApplication --mintUrl=<mint dataload url> --datafile=<loadfile.json> --type=<jsonl|tsv|csv>`
        e.g. java -jar loader.jar uk.gov.admin.LoaderApplication --mintUrl=http://localhost:4567/load --datafile=datafile.tsv --type=tsv
