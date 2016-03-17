# indexer
A repository to replicate registers data from master db to presentation app databases

[![Build Status](https://travis-ci.org/openregister/indexer.svg?branch=master)](https://travis-ci.org/openregister/indexer)

# Requirements

- Java 1.8+
- Postgres DB 9.4

# Build and Run project

- Install and run postgres db
- Create database mint `createdb test_indexer` to run functional_tests
- Checkout project 
- Use command `./gradlew cleanIdea idea` to generate the idea project files
- Build project using command `./gradlew clean build`
