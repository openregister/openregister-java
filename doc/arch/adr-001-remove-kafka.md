# Decision record: dropping kafka #

## Context ##

When we first started to model a register as two separate apps (mint
and presentation), we explored [Kafka][] as a means of storing the
append-only datastore, and streaming it from the mint to the
presentation database.

Kafka was initially chosen because its append-only journal model
seemed a good fit for the way we model registers as an append-only
list of entries.  Kafka also offers the ability to replay the journal
from the start, which allows you to create new ways of presenting data
and retrofit them to existing datasets.

However, as we used kafka more and more, we found certain pain points:

  - kafka is not offered as a service in the computing platforms we
    were targeting, such as Amazon or Heroku
  - kafka has zookeeper as a dependency, which further increases
    operational complexity
  - kafka and zookeeper consume a lot of memory, requiring us to
    deploy larger machines and costing more
  - one of kafka's main selling points is sheer throughput
    performance, which isn't really a need we have
  - kafka is a new technology and our team did not understand it as
    well as older technologies.  In particular, we did not have a good
    understanding of its failure modes.

This last point is key.  Because kafka is such a new technology, we
did not have confidence to rely on it as a primary data source, so we
ended up running Postgres as a primary source of truth (which is a
well-understood technology, where we have confidence we can back it up
and replicate it to mitigate risks of data loss), and using Kafka as a
means of communicating between mint and presentation.

Running both postgres and kafka to store the same data seemed
wasteful.  We ended up needing to administer both postgres and kafka,
and having all the operational complexities of both.

We considered choosing only one of these technologies to play both
roles -- being the mint's primary data store, and communicating the
stream of entries from the mint to the presentation.  We therefore
thought about what it would mean to choose either postgres alone or
kafka alone.

Using kafka as a primary data store had a few downsides.  Already
mentioned is our lack of confidence for being a primary data store.
We also did not understand its failure modes well.  In particular, if
a message is sent to Kafka and the connection times out, was it
successful?  Can we safely retry?  Will we get duplicates?

Furthermore, a key part of our domain model is an incrementing serial
number, used to identify each entry in a register.  Kafka does not
have a message-level serial number; instead it uses an index into a
stream of bytes.  If we want to assign a serial number to each entry
as it's entered into the register, we need some sort of coordination
mechanism to ensure we add these serial numbers in a strictly serial
order.  When we were using postgres and kafka together, postgres
provided this coordination mechanism.  If we removed postgres, we'd
have to add something else to meet this need.

On the presentation side of the equation, we also discovered a need
for a separate process for consuming the register stream and inserting
it into the presentation database.  Our original design had the
presentation app consume the kafka stream directly; however, this
didn't fit well with the presentation being a stateless appserver
which could be horizontally scaled, because each app instance would
independently try to consume the kafka stream, but only one would
actually be successful.

We also looked at [Amazon Kinesis][] which is similar to Kafka.
However as it discards all messages after 24 hours it did not fit our
needs.

## Decision ##

We will use postgres alone, as the mint's primary data store, and also
as a means of communicating from the mint to the presentation app.  We
will remove kafka and zookeeper from the architecture.

Postgres will be the tool which assigns serial numbers, through the
use of a [`SERIAL`][pg-serial] column type.

In order to replicate the data from the mint to the presentation
databases, an [indexer app][] will be created.

## Status ##

Accepted.

## Consequences ##

We will remove kafka and zookeeper from the system, and all the
administrative effort associated with them.

The indexer will at first be directly consuming the mint's primary
data store.  If there are security or performance concerns about this,
the indexer can always be configured to consume a postgres read
replica instead.

Our architecture will now depend heavily on heterogeneous apps
integrating on a common database: firstly, the indexer will be
consuming the mint's primary postgres data store; and secondly, the
indexer will be injecting data into the presentation's indexed data
store.  There is quite a bit written on the
[problems of integration databases][], and we should keep these
problems in mind.  That said, we can be reasonably confident that the
schema won't change for the mint (because it is so simple - a serial
number and a json entry), and that the schema can change for the
presentation (because we can create a new indexer and presentation
instance, have the indexer replay the entire stream from the start,
migrate users to use the new presentation, and turn off the original
services).

[Kafka]: http://kafka.apache.org/
[Amazon Kinesis]: https://aws.amazon.com/kinesis/
[indexer app]: https://github.com/openregister/indexer
[pg-serial]: http://www.postgresql.org/docs/9.4/static/datatype-numeric.html
[problems of integration databases]: http://martinfowler.com/bliki/IntegrationDatabase.html
