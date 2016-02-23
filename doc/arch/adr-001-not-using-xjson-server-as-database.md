# Decision record: Not using xjson-server as a primary data store #

## Date ##

23/02/2016

## Context ##

We have been experimenting with the [`xjson-server`][xjson] binary
available from google's certificate-transparency repository.  This
server is, as the name suggests, experimental.  It is like a
certificate transparency log server, except that it accepts JSON
documents rather than X.509 certificates.

Our current model of a register includes a total ordering of entries,
and we want to have a cryptographic certification of those entries.
Therefore, the natural way to approach this was to submit entries to
the xjson-server and accept that the ordering of those entries is
simply the order in which they appear in the log.

However, this has caused us some problems.  In particular, it doesn't
feel right that the ordering, which is a key concept within the
register domain, is entirely within the control of the xjson-server.
Because of the asynchronous nature of the `/add-json` endpoint, the
documents added within a given update interval are merged in to the
log in an arbitrary order.

For extensibility, we want to support multiple methods of proof on a
given register.  An example might be one proof based on simple
sha256-of-canonical-JSON, with another based on [objecthash][].  Both
could be supported by xjson-server.  However both would then impose
their own ordering, and they wouldn't necessarily agree with one
another.

## Decision ##

We will not use xjson-server as a primary data store.

Specifically, we will not treat its ordering of documents as the
primary order of entries in a register; and we will not use it as the
primary data store for all data about entries.

## Status ##

Accepted.

## Consequences ##

We will need to review the way in which we handle proofs, given that
the mint is now maintaining ownership of the order of entries.

There are a number of options: we could implement our own Merkle tree
based on the ideas in certificate-transparency; or we could submit
documents to xjson-server that contain our own ordering (and accept
that our ordering and xjson-server's ordering will not be the same,
though they should broadly agree).  When doing this, we should also
refer to data items by hash rather than storing the complete item
within xjson-server.

## Reference ##

[objecthash]: https://github.com/benlaurie/objecthash
[xjson]: https://github.com/google/certificate-transparency/blob/428591e96e3ab7dc8f381389ccdffa3d98a20b04/cpp/server/xjson-server.cc
