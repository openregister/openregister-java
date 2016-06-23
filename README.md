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
- Ensure database user 'postgres' exists. If not `createuser postgres`.
- Checkout project 
- Use command `./gradlew cleanIdea idea` to generate the idea project files
- Build project using command `./gradlew clean build` 

