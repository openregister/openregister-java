# Decision record: Prefer arrays for collection resources

## Context

V1 of the registers specification represented collections of entries, items (now called blobs), and records using
different data structures, including nested arrays and associative arrays. This decision was influenced by the concept
of indexes, an experimental feature which we later removed (see [ADR 10: remove indexes](doc/arch/adr-010-remove-indexes.md)).

For a long time openregister-java was also out of sync with the registers specification in various ways. One
manifestation of this was that the specification described an `/items` endpoint which was not actually available.
Although [ADR 8](doc/arch/adr-008-items-resource.md) outlined an approach for this, we never fully implemented it.

We currently have an undocumented, incomplete version of this endpoint at `/next/blobs`.

## Decision

1. The JSON representation for any collection resource should be an array of objects, unless there is a good reason to
   break this rule
2. The special value `_id` should be used to include identifiers in the objects

[RFC 25](https://github.com/openregister/registers-rfcs/blob/master/content/snapshot-resource/index.md) established this
pattern for records specifically.

[RFC 27](https://github.com/openregister/registers-rfcs/blob/master/content/blob-collection-resource/index.md) extends
this to the blobs resource.

We will still follow the decision in ADR 8 to order blobs by their order of arrival to the database.

The entries resource is consistent with the first point, but not the second point, because it uses "entry-number" to
refer to identifiers. We will not change this in V2 of the API.

## Status

Accepted.

## Consequences

The JSON representation of a collection resource will not contain additional structure that is not present in the CSV
representation.

The core collections provided by the API (entries, items, records) can be processed the same way by API clients.
