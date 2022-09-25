# mparticle-audiences-batch

## What is this server?

mParticle has an *audiences* feature.  Using something like Split targeting rules, an mParticle customer can define groups of users.  This is useful when an mParticle partner wants to work with an audience.  For example, Braze does marketing campaigns and can consume mParticle audiences for outreach.

Dozens of companies have audience integration with mParticle.

Split segments are similar to mParticle, but more static.  In order to sync audiences with mParticle, Split must have an endpoint that can receive requests to build a new segment, add and remove keys from it, and remove the segment.  That endpoint is a node.js lambda you can find here:  https://github.com/splitio/mparticle-audiences

This server is to address a performance problem.  mParticle gives keys to Split one MPID at a time.  Thus, Split was getting flooded with one off requests to change segments, creating a costly performance problem.

To address the proble, this server was created.  This Java server takes MPIDs from the lambda endpoint and caches them in memory.  At a configurable interval, the server uses a separate thread to flush any cached keys to their corresponding Split segment.  This gives control over how often each segment flush takes place.

In addition, the server has /uptime and /ping requests for maintainability. 

## How to Build

```
git clone https://github.com/splitio/mparticle-audiences-batch

mvn clean compile assembly:single

java -jar target/audiences-0.0.1-SNAPSHOT-jar-with-dependencies.jar audiences.config
```

audiences.config is a JSON configuration file included with the repository.  You can choose the port on which the server will listen, an authorization token to weed out garbage transactions, and the rate at which MPIDs should be flushed to Split in seconds.

## How the AWS lambda uses it..

 * api key,
 * workspace id, 
 * environment id, 
 * traffic type id, 
 * segment name, 
 * and list of MPIDs

```
{
    "apiToken": "5c3f****",
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

Verb can be add or delete.

Designed to be called from 

https://github.com/splitio/mparticle-audiences

... a node.js lambda that is in transactional conversation with mParticle.

## How it works

mParticle registers a new integration endpoint, one at which the mparticle-audiences node.js lambda resides.  When mParticle asks to create a segment or delete it, the lambda can handle itself.  When mParticle asks to add or delete an MPID to a segment (it always does this one at a time), the lambda POSTs the work to this server -- mparticle-audiences-batch -- and the Java HTTP server maintains a cache of MPIDs per segment.

At a specified interval, a batch server thread calls the Split API to add or delete MPIDs from the corresponding segment.

Both the lambda and the batch server are multi-tenant.  They can support any number of Split customers that wish to use the integration.

## Configuration

```
{
  "port" : 5010,
  "authToken" : "foo",
  "segmentsFlushRateInSeconds" : 10
  "keyFile" : "yourkey.jks" 
}
```

A file with this configuration is meant to be supplied by command line path.

The auth token works like other Split servers; you must give the correct autho token or your transaction will be ignored.

The flush rate determines how often the cache will be emptied to Split.

The key file is expected in JKS format.  The one included with the source code is self-signed... 

## Questions?

david.martin@split.io


