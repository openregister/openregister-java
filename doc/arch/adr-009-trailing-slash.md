# Decision record: Trailing slash in HTTP resources

## Context

Currently, HTTP resources have many URLs representing the same thing. This is
not an ideal scenario for search engine indexing or caching. For example, the
record `GB` resource can be accessed via:

* `GET /records/GB`
* `GET /records/GB/`
* `GET /records/GB.json`
* `GET /records/GB.json/`
* `GET /records/GB.csv`
* `GET /records/GB.csv/`

This ADR proposes a canonical URL and a way to consolidate around it.

## Decision

The canonical path for a resource MUST NOT have a trailing slash. Aliases MUST
redirect (301) to the canonical path.

For example, the `GB` record has the canonical path:

```
/records/GB
```

And the canonical JSON resource:

```
/records/GB.json
```

Aliases like `/records/GB/` and `/records/GB.json/` MUST redirect to their
respective canonical resources.

### Paginated resources

The first page for the records list has the canonical path:

```
/records?page-index=1&page-size=100
```

Possible aliases of the above are:

* `/records`
* `/records/`
* `/records/?page-index=1&page-size=100`

Similarly, the canonical JSON resource:

```
/records.json?page-index=1&page-size=100
```

Has these possible aliases:

* `/records.json`
* `/records.json/`
* `/records.json/?page-index=1&page-size=100`


## Status

Proposed.

## Consequences

This change is expected to make caching and caching invalidation easier
without disrupting any user.

