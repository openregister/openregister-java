#!/bin/bash

set -ue

function ensure_db_exists {
  psql -lqt | grep -wq $1 || createdb $1
}

function ensure_user_exists {
  psql postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$1'" | grep -q 1 || createuser $1
}

# Set up postgres
ensure_db_exists openregister_java
ensure_db_exists ft_openregister_java
ensure_user_exists postgres

# Set up IntelliJ
if [ ! -f presentation.ipr ]; then
  ./gradlew cleanIdea idea
fi
