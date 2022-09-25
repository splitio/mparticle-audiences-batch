# mparticle-audiences-batch

## Build

```
git clone https://github.com/splitio/mparticle-audiences-batch

mvn clean compile assembly:single

java -jar target/audiences-0.0.1-SNAPSHOT-jar-with-dependencies.jar audiences.config
```

audiences.config is a JSON configuration file included with the repository.  You can choose the port on which the server will listen, an authorization token to weed out garbage transactions, and the rate at which MPIDs should be flushed to Split in seconds.

## How to use it..


Java HTTP server... key transaction expects the 
 * api key,
 * workspace id, 
 * environment id, 
 * traffic type id, 
 * segment name, 
 * and list of MPIDs

Designed to be called from 

https://github.com/splitio/mparticle-audiences

... a node.js lambda that is in transactional conversation with mParticle.

## How it works

mParticle registers a new integration endpoint, one at which the mparticle-audiences node.js lambda resides.  When mParticle asks to create a segment or delete it, the lambda can handle itself.  When mParticle asks to add or delete an MPID to a segment (it always does this one at a time), the lambda POSTs the work to this project -- mparticle-audiences-batch -- and the Java HTTP server maintains a cache of MPIDs per segment.

At a specified interval, the batch server calls the Split API to add or delete MPIDs from the corresponding segment.

Both the lambda and the batch server are multi-tenant.  They can support any number of Split customers that wish to use the integration.

## Configuration

```
{
  "port" : 5010,
  "authToken" : "foo",
  "segmentsFlushRateInSeconds" : 10
}
```

A file with this configuration is meant to be supplied by command line path.

The auth token works like other Split servers; you must give the correct autho token or your transaction will be ignored.

The flush rate determines how often the cache will be emptied to Split.

## Questions?

david.martin@split.io

```
{
    "apiToken": "5c3f****"
    "workspaceId": "c02d****",
    "environmentId": "c02f****",
    "trafficTypeId": "5ecf****",
    "verb" : "add",
    "mpids" : [
        "001",
        "002",
        "003"
    ],
    "segment": "yuki"
}
```

Sample POST body to add (or delete) MPIDs from an in-memory cache on the batch server.

These requests are meant to be made by the node.js lambda audiences code.



