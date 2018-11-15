# Decision record: Root path handling

## Context

As part of the work to release the next major version, there are three routes
that need special handling: `/`, `/v1` and `/v2`.

This ADR proposes a behaviour for each one of them.

## Decision

### Root (`/`)

When HTML is requested, it should redirect (301) to `https://registers.service.gov.uk/registers/{register_id}`.

Otherwise it should redirect (301) to the Register resource
(`/{version}/register`) for the current version of the API in the requested
format; JSON if unspecified.

### Version root (`/v1`, `/v2`)

When HTML is requested, it should redirect (301) to `https://registers.service.gov.uk/registers/{register_id}`.

Otherwise it should redirect (301) to the Register resource
(`/{version}/register`) for the requested version in the requested format;
JSON if unspecified.

## Status

Proposed.
