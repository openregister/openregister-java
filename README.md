# presentation

A presentation layer for registers.

[![Build Status](https://travis-ci.org/openregister/presentation.svg?branch=master)](https://travis-ci.org/openregister/presentation)

# Requirements

- Java 1.8+
- Postgres DB 9.4

# Build and Run project

- Checkout project
- Add `address.openregister.dev` as an alias for localhost to /etc/hosts
- Run the `./go` script to set everything up
- Build project using command `./gradlew clean build`

# Working on the frontend

## Updating the styles

Ensure you have sassc installed:

    brew install sassc

Recompile the `.scss` files to `.css`:

    ./build-styles.sh

## Running the server

Run a local server with:

    ./run-application.sh

# Update the govuk_frontend_toolkit and govuk_template

- Add the toolkit and template as remotes

    git remote add -f govuk-frontend-toolkit git@github.com:alphagov/govuk_frontend_toolkit.git
    git remote add -f govuk-template git@github.com:alphagov/govuk_template.git

- Update the toolkit and template to the latest version (using git subtree)

    git subtree pull --prefix src/main/govuk_frontend_toolkit govuk-frontend-toolkit -m "Update govuk_frontend_toolkit" master --squash
    git subtree pull --prefix src/main/govuk_template govuk-template -m "Update govuk_template" master --squash
