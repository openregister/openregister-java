# openregister-java
A java implementation of a register

[![Build Status](https://travis-ci.org/openregister/openregister-java.svg?branch=master)](https://travis-ci.org/openregister/openregister-java)

## Running locally

You can spin up a local copy of `openregister-java` using Docker with the following:

    ./run-application.sh

This will build the application from source (inside a container), start and configure Postgres, and run the application (listens on `127.0.0.1:8080`). To load some sample data:

    ./load-school-data.sh

## Development

### Requirements

- Java 1.8+
- Postgres DB 9.5+
- Python 3 (and in particular the `pyvenv` script needs to be on your
  path)

### Build and Run project

- Install and run postgres db
- Checkout project
- Run the `./go` script to set everything up
- Build project using command `./gradlew clean build`

### Working on the frontend

[![Standard - JavaScript Style Guide](https://cdn.rawgit.com/feross/standard/master/badge.svg)](https://github.com/feross/standard)

### Updating the styles

Recompile the `.scss` files to `.css` via Gradle task:

    ./gradlew compassCompile

If you want to make iterative changes, you can start a compass watch
task to automatically update a running app with scss changes:

    ./gradlew compassWatch

While compassWatch is running, any changed `.scss` file will
automatically be compiled to `.css` and picked up by the running app.


### Update the govuk_frontend_toolkit and govuk_template

    $ git submodule update
