# mint
A repository for mint project

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
-     gradle bulkLoad -Pconfigfile=<config.properties> -Pdatafile=<loadfile.json> [-Ptype=jsonl|tsv|csv]
2. Run using the built jar
-     java -jar <path_to_jar> Loader --configfile=<config.properties> --datafile=<loadfile.json> --type=<jsonl|tsv|csv>

