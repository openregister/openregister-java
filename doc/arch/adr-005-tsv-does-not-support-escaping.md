# Decision record: TSV does not support escaping #

## Context ##

Registers support multiple different formats for data.  One of them is
TSV or tab-separated values.  Superficially, TSV is similar to CSV
(comma-separated values) but they fit different use cases.

TSV is useful because it can be munged by simple command-line tools
such as `grep`, `awk`, or `cut`.

CSV is useful because it can be used by programs such as Excel.

As a result, they often have different rules for escaping.  CSV often
allows data which includes commas or newlines by quoting: as long as a
newline or a comma is within double quotes, it does not terminate the
current value or record.

If we took this approach with TSV, then tools like `awk` and `cut`
would become dramatically less useful because they would not be able
to distinguish between quoted and unquoted tabs and newlines.

There is prior art for this approach.  The original [IANA TSV][]
assignment document says:

> Note that fields that contain tabs are not allowable in this encoding.

## Decision ##

We do not support any rules for escaping in TSV formats.  It is not
possible to express a tab or a newline character in TSV, since a tab
always ends the current field, and a newline always ends the current
row.

## Status ##

Accepted.

## Consequences ##

There is no way to express data containing newlines or tabs when
loading in through the [loader][].

We do not perform any checks for if a register happens to contain
newlines or tabs, so we may generate invalid TSV values if a register
contains newlines or tabs.

[IANA TSV]: https://www.iana.org/assignments/media-types/text/tab-separated-values
[loader]: https://github.com/openregister/loader
