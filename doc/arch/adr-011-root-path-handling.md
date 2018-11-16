# Decision record: Root path handling

## Context

As part of the work to release the next major version, there are three routes
that need special handling: `/`, `/v1` and `/v2`.

This ADR proposes a behaviour for each one of them.

## Decision

### Root (`/`)

When HTML is requested, it should return the exact same HTML as per now.

Otherwise it should redirect (301) to the Register resource
(`/{version}/register`) for the requested version in JSON regardless of the
format specified.

### Version root `/v1`

When HTML is requested, it should redirect (301) to `/` (See above).

Otherwise it should redirect (301) to the Register resource
(`/{version}/register`) for the requested version in JSON regardless of the
format specified.

### Version root `/v2`

It should redirect (301) to the Register resource
(`/{version}/register`) for the requested version in JSON regardless of the
format specified.

## Status

Accepted.
