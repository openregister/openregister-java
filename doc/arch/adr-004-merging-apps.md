# Decision record: merging apps together #

## Context ##

When we first created the beta register product, we created it as
three separate apps: [mint][], [indexer][], and [presentation][].
There were a number of reasons for this:

 - we have very distinct users who can read from or write to a
   register: a register has a single authoritative custodian who can
   approve writes; but anybody can request reads
 - by having one app that accepts writes to a register, and another
   that accepts reads, we can start to imagine how we might manage
   reads and writes in different network security zones
 - it allowed us to view the same domain through different lenses: for
   example, the mint database did not need to perform indexing on the
   contents of data items, because it did not need to perform the
   advanced queries that the presentation app did.

There were advantages and disadvantages to this architectural
approach:

 - (-) our primary approach to inter-app communication was via integrating
   on the database, which caused the apps to be tightly coupled
 - (+) however, by integrating on the database, we were able to give
   each app a database user restricted to only the operations it
   needed to perform
 - (-) having three separate apps made it difficult to iterate on
   anything which involved changing the structure of the domain of
   registers; as it entailed a dance around multiple commits to
   multiple apps and choreographed deployments.  One particular thing
   that was painful to integrate was the [verifiable-log][] code.

There may still be a desire to perform updates to a register in a
secure (potentially offline) environment and to be able to only
present a read-only view of that register on the internet.  However,
we now feel that a better way of achieving this would be to implement
an app that is able to mirror a register: either by reading its Read
API, or by reading a serialized format of an entire register.

## Decision ##

We will merge the three register apps together.  The [mint][],
[indexer][], and [presentation][] repositories have been merged
(preserving git history) into a single repository,
[openregister-java][].

## Status ##

Accepted.

## Consequences ##

We have already been able to delete quite a lot of duplicated code.
There is less code to manage, and it is all in one place, so we
anticipate that we will be able to move more quickly.

We need to put more thought into more sensitive registers and how to
enable offline update of registers.  The [verifiable-log][]
capabilities, along with as-yet unimplemented digital signatures, will
play a key role in this: by having the signing key offline, the
internet-facing register mirror will not be able to present bad data
without breaking the signature.

[indexer]: https://github.com/openregister/indexer
[mint]: https://github.com/openregister/mint
[openregister-java]: https://github.com/openregister/openregister-java
[presentation]: https://github.com/openregister/presentation
[verifiable-log]: https://github.com/openregister/verifiable-log
