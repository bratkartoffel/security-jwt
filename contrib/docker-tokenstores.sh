#!/bin/bash

docker run --rm -d --name=memcached -p 127.0.0.1:11211:11211 memcached:alpine
docker run --rm -d --name=redis     -p 127.0.0.1:6379:6379   redis:alpine