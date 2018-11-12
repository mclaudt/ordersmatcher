# Stock Orders Matcher

## Intro

Reads client.txt and orders.txt from resources.
Produces result.txt with final account states.

## How to run
```
sbt run
```

## Features
* Self-orders prevention (covered by unit-test also).
* Only full match (price and quantity) is allowed for simplicity/
* FIFO guarantees for the same price and quantity.
* Negative stock accounts are allowed for simplicity.
* Implemented in Akka Actors to make parallelization of operations possible.
* Features `become` and `stash` patterns from Akka Actors.
* Debug level is on by default, to play with market evolution.