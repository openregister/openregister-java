# openregister-java
A java implementation of a register

[![Build Status](https://travis-ci.org/openregister/openregister-java.svg?branch=master)](https://travis-ci.org/openregister/openregister-java)

## Running locally

You can spin up a local copy of `openregister-java` using Docker with the following:

    ENVIRONMENT=alpha ./run-application.sh

This will do the following in Docker containers:

- build the application from source.
- start and configure the DB.
- run the "basic" registers: register, field and datatype with data cloned from the specified phase (listens on `127.0.0.1:8081`).
- run a register configured with `config.docker.register.yaml` (listens on `127.0.0.1:8080`).

### Experimenting locally

If you want to view/change the basic registers you can do that by sending requests to `127.0.0.1:8081` with an appropriate `Host` header:

    curl 127.0.0.1:8081/records -H "Host: field"

If the basic registers change you can restart your register so that it sees the changes:

    docker restart openregister-register

### Running on Cloud Foundry/PaaS

- [Configure PaaS access](https://docs.cloud.service.gov.uk/#quick-setup-guide).
- Build the application (`./gradlew assemble`)
- Configure [a Postgres service](https://docs.cloud.service.gov.uk/#using-database-services). (NB: A PaaS provided Postgres instance requires the manual addition of `CREATE SCHEMA` permissions for the user with credentials in `VCAP_SERVICES`. You will need to submit a ticket with PaaS.)
- Tweak `manifest.yml` so it has appropriate values.
- Deploy (`cf push {name} -p deploy/openregister-java.jar`).

## Development

### Requirements

- Java 1.8+
- Postgres DB 9.5+ (and in particular `psql` and `createuser` need to be on your PATH)
- Python 3 (and in particular the `pyvenv` script needs to be on your
  PATH)

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
