#!/bin/sh
docker run --network host -v $PWD/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
