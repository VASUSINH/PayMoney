#!/bin/bash
# Database per service: every microservice owns its own database.
set -e

for db in paymoney_user paymoney_wallet paymoney_payment paymoney_audit; do
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" \
       -c "CREATE DATABASE $db;"
done
