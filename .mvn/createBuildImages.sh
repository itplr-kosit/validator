#!/usr/bin/env bash

#
# Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

TAGS=("3-openjdk-16" "3-jdk-11" "3-jdk-11-openj9" "3-jdk-8" "3-jdk-8-openj9" "3-openjdk-15" "3-openjdk-17", "3-openjdk-18", "3-eclipse-temurin-19")

docker login -u "$CI_REGISTRY_USER" -p "$CI_REGISTRY_PASSWORD" "$CI_REGISTRY"
for i in "${TAGS[@]}"
do
  echo Creating build image for "$i"
  {
    echo "FROM maven:${i}"
  } >>Dockerfile
  docker build -t "$CI_REGISTRY_IMAGE/maven:$i" .
  docker push "$CI_REGISTRY_IMAGE/maven:${i}"
  rm Dockerfile
done