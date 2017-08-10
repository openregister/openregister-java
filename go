#!/bin/bash

set -ue

function ensure_db_exists {
  psql -lqt | grep -wq $1 || createdb -O postgres $1
}

function ensure_user_exists {
  psql postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$1'" | grep -q 1 || createuser $1
}

# Set up submodules

git submodule update --init

# Set up postgres
ensure_user_exists postgres
ensure_db_exists openregister_java
ensure_db_exists ft_openregister_java_multi
ensure_db_exists conformance_openregister_java

# Set up IntelliJ
if [ ! -f presentation.ipr ]; then
  ./gradlew cleanIdea idea
fi
