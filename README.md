# presentation

A presentation layer for registers.

[![Build Status](https://travis-ci.org/openregister/presentation.svg?branch=master)](https://travis-ci.org/openregister/presentation)

# Requirements

- Java 1.8+
- Postgres DB 9.4

# Build and Run project

- Checkout project 
- Run the `./go` script to set everything up
- Build project using command `./gradlew clean build` 
- Run shell script `run-application.sh` to run the application server.   

# Update the govuk_frontend_toolkit

- Add the toolkit as a remote

    git remote add -f govuk-frontend-toolkit git@github.com:alphagov/govuk_frontend_toolkit.git

- Update the toolkit to the latest version (using git subtree)

    git subtree pull --prefix src/main/govuk_frontend_toolkit govuk-frontend-toolkit -m "Update gov_uk_frontend_toolkit" master --squash
