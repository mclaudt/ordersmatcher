# Stock Matcher

## Intro

Reads client.txt and orders.txt from resources.
Produces result.txt with final account states.

## How to run
```
sbt run
```



## Features
* self-orders are not allowed (covered by unit-test also).
* Negative stock accounts are allowed for simplicity.
* Debug is on by default, to play market evolution.
* Implemented in Akka Actors to make parallelization of operations possible.
