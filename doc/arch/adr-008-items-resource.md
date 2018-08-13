# Decision record: Items resource implementation

## Context

The [Items resource](https://openregister.github.io/specification/#items-resource)
imposes an implementation challenge regarding how to order them in a
pagination scenario. This ADR defines a way to create a deterministic order
for this implementation.

## Decision

The Items set has no natural order. Items are identified by their hash. A way
to define an order is to assign an index or timestamp based on order of
arrival to the database.

```sql
CREATE TABLE IF NOT EXISTS item (
  n serial,
  sha256hex VARCHAR PRIMARY KEY,
  content JSONB
);

CREATE INDEX item_idx ON item USING btree (n);

SELECT sha256hex, content
FROM item
WHERE n > ?cursor
ORDER BY n ASC
FETCH NEXT ?size ROWS ONLY;
```

Where `?cursor` and `?size` are defined by the request. Defaults are `0` and
`100` respectively.

The notion of "cursor" is to be clear it is an obscure pointer rather than an
actual page. This is commonly known as _seek method_ or _keyset pagination_.

The result set should not suface the `n` field except as the next `cursor`
(see next section).

### Changes to the API

Implement the `/items` endpoint as defined in the specification. This should
look like:

```http
GET /items HTTP/1.1
Host: country.register.gov.uk
Accept: application/json
```

```http
HTTP/1.1 200 OK
Content-Type: application/json
Link: <?cursor=3>; rel="next"

{
  "sha-256:e94c4a9ab00d951dadde848ee2c9fe51628b22ff2e0a88bff4cca6e4e6086d7a": {
    "end-date":"1991-12-25",
    "country":"SU",
    "official-name":"Union of Soviet Socialist Republics",
    "name":"USSR",
    "citizen-names":"Soviet citizen"
  },
  "sha-256:e03f97c2806206cdc2cc0f393d09b18a28c6f3e6218fc8c6f3aa2fdd7ef9d625": {
    "end-date":"1990-10-02",
    "country":"DE",
    "official-name":"Federal Republic of Germany",
    "name":"West Germany",
    "citizen-names":"West German"
  }
}
```

## Status

Accepted.

## Consequences

This change has no consequences besides offering a new endpoint.
