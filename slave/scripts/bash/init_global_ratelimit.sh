#!/usr/bin/env bash

key=justtest

redis-cli set "prime:global_rate_limit:redis:${key}:max_permits" 5
redis-cli set "prime:global_rate_limit:redis:${key}:current_permits" 0

redis-cli get "prime:global_rate_limit:redis:${key}:max_permits"
redis-cli get "prime:global_rate_limit:redis:${key}:current_permits"
