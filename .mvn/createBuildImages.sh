#!/usr/bin/env bash

TAGS=("3-openjdk-16" "3-jdk-11" "3-jdk-11-openj9" "3-jdk-8" "3-jdk-8-openj9" "3-openjdk-15" "3-openjdk-17")

docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
for i in "${TAGS[@]}"
do
  echo Creating build image for "$i"
  {
    echo "FROM maven:${i}"
    echo "ADD . /src/"
    echo "WORKDIR /src"
    echo "RUN mvn dependency:go-offline"
  } >>Dockerfile
  docker build -t "$CI_REGISTRY_IMAGE/maven:$i" .
  docker push "$CI_REGISTRY_IMAGE/maven:${i}"
  rm Dockerfile
done