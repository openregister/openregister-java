# openregister-java
A java implementation of a register

[![Build Status](https://travis-ci.org/openregister/openregister-java.svg?branch=master)](https://travis-ci.org/openregister/openregister-java)

# Requirements

- Java 1.8+
- Postgres DB 9.5+
- Python 3 (and in particular the `pyvenv` script needs to be on your
  path)

# Build and Run project

- Install and run postgres db
- Checkout project
- Run the `./go` script to set everything up
- Build project using command `./gradlew clean build`

# Working on the frontend

## Updating the styles

Recompile the `.scss` files to `.css` via Gradle task:

    ./gradlew compassCompile

## Running the server

Run a local server with:

    ./run-application.sh

To load some sample data:

    ./load-school-data.sh

# Update the govuk_frontend_toolkit and govuk_template

    $ git submodule update
