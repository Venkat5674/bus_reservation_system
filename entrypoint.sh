#!/bin/sh

# Fix Render's PostgreSQL connection string (postgres:// -> jdbc:postgresql://)
if echo "$SPRING_DATASOURCE_URL" | grep -q "^postgres://"; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${SPRING_DATASOURCE_URL#postgres://}"
  echo "Fixed SPRING_DATASOURCE_URL for Spring Boot (added jdbc: prefix)"
fi

# Run the application
exec java -jar app.jar
