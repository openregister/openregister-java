#!/usr/bin/env bash
(
  cd src/main/resources/assets

  mkdir -p css
  sassc --sourcemap -I govuk_frontend_toolkit/stylesheets -I govuk_template/source/assets/stylesheets/ -I sass main.scss css/main.css
)
