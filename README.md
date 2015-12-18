# mint
Accepts updates to a register.

[![Build Status](https://travis-ci.org/openregister/mint.svg?branch=master)](https://travis-ci.org/openregister/mint)

# Requirements

- Java 1.8+
- Postgres DB 9.4

# Build and Run project

- Install and run postgres db
- Create database mint `createdb mint` to use application locally.
- Create database ft_mint `createdb ft_mint` to run functional tests.
- Checkout project 
- Use command `./gradlew cleanIdea idea` to generate the idea project files
- Build project using command `./gradlew clean build` 

# Bulk load data from CLI

There are 2 options:

1. Run via gradle:
-     `gradle bulkLoad -PmintUrl=<mint-url> -Pdatafile=<data-file-path> [-Ptype=jsonl|tsv|csv|yaml]`
        e.g. gradle bulkLoad -PmintUrl=http://localhost:4567/load -Pdatafile=datafile.tsv -Ptype=tsv
2. Run using the built jar
-     `java -jar <path-to-jar> --mintUrl=<mint-url> --datafile=<data-file-path> --type=<jsonl|tsv|csv|yaml>`
        e.g. java -jar loader.jar --mintUrl=http://localhost:4567/load --datafile=datafile.tsv --type=tsv
