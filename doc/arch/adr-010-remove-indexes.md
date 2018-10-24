# Decision record: Remove indexes

## Context

Currently, it is possible to configure an "index" for a particular register. An index
is a transformation of a register entry log to create a new entry log against a different
key value. The appropriate API structure for accessing an index is (after a very long time) 
still unconfirmed and therefore indexes are not used. 

Because it is not used, the code that exists for indexes is therefore technical debt. To solve
this technical debt we had two options:

* Decide and implement an API structure for accessing indexes as well and it's impact on the 
current data model and specification.
* Remove indexes as a feature (and code).

Note that although current users cannot access indexes, they are affected by some changes we made 
to the existing API (see [decision record](adr-007-read-api-changes.md).

## Decision

We decided to remove the indexes feature and any corresponding code. This is because we do not
have evidence of the need for this feature from our users. To decide and implement the API structure
and resulting changes to the specification would be a significant amount of work which we cannot 
currently prioritise. We will have the opportunity to implement indexes again later, if we decide 
there is a need for it.

## Status

Accepted

## Consequenses

Remove all code related to indexes and remove `/index/*` endpoints. We will plan to revert the
changes made to the registers API in a way that minimises disruption to existing users.
