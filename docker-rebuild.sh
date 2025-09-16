#!/usr/bin/env bash
docker compose down -v --rmi local --remove-orphans
docker compose build
docker compose up