#!/bin/bash
../gradlew :integration-tests:copyWar
cd ../platform-integration-tests/integration-tests
docker-compose rm -f -s
docker-compose build
docker-compose up