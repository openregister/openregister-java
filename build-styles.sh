#!/usr/bin/env bash
mkdir -p src/main/resources/assets/css
sassc -I src/main/govuk_frontend_toolkit/stylesheets -I src/main/govuk_template/source/assets/stylesheets/ -I src/main/sass/ main.scss src/main/resources/assets/css/main.css