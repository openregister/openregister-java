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

Also, the `Link` header for aliases should add the canonical URL using the
`rel="canonical"` ([RFC6596](https://tools.ietf.org/html/rfc6596)).

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

Paginated resources behave in a similar way, trailing slashes are aliases to
the equivalent without trailing slashes.

Notice that this ADR postpones the decision of what is the canonical path for
the first page. This means that `/records` is canonical with the alias
`/records/` and `/records?page-index=1` is canonical with the alias
`/records/?page-index=1`


## Status

Approved.

## Consequences

This change is expected to make caching and caching invalidation easier
without disrupting any user.

