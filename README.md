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

- Add the toolkit and template as remotes
```
    git remote add -f govuk-frontend-toolkit git@github.com:alphagov/govuk_frontend_toolkit.git
    git remote add -f govuk-template git@github.com:alphagov/govuk_template.git
```
- Update the toolkit and template to the latest version (using git subtree)
```
    git subtree pull --prefix src/main/govuk_frontend_toolkit govuk-frontend-toolkit -m "Update govuk_frontend_toolkit" master --squash
    git subtree pull --prefix src/main/govuk_template govuk-template -m "Update govuk_template" master --squash
```
