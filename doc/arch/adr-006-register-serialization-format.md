# Decision record: Register Serialization Format

## Context ##

The basic components of a Register are Items and Entries. The Items are the content of the Register and are currently expressed as JSON objects e.g.

    {
      "address": "5T8EXT7N",
      "denominations": [
        "C22"
      ],
      "dioceses": [
        "CE23"
      ],
      "headteacher": "Mr T Wilson",
      .....
    }

An Entry refers to an Item by the Item's SHA-256 hash and also contains a timestamp recording when the Item was added to the Register.

The algorithm for computing the hash involves canonicalizing the JSON by removing all whitespace between elements i.e. not in a quoted string, calculating the SHA-256 hash on the JSON encoded as a UTF-8 string and expressed as a Hex string.

The other key feature of a Register is its Root Hash which is the root element of the Merkle tree based on the sequence of Entries. The Root Hash can be used in combination with a Proof composed of a number of other hash values to verify a single Entry.

Register data is currently provided by the Register Design Authority usually in TSV format e.g.

    402378[tab]Abertillery Learning Community[tab][tab]677[tab][tab][tab]  [tab][tab][tab][tab]30[tab][tab][tab][tab][tab][tab]2016-09-01[tab]

The TSV does not fully represent a Register in that the timestamps of the Entries and the Root Hash are not included.

A Register is not the same thing as the Openregister-java application. It can be represented in an external, human readable format making the structure of the Register explicit. We call this the Register Serialization Format (RSF).

The RSF can also be used to clone an existing Register as a whole or in part.

Registers can be large - the address register currently has c34 million Entries and the food-premises-rating register c500,000. So the RSF should not need to be parsed as a whole before processing.

### Validation

To be valid:

- All Items should be referred to by at least one Entry. There should be no Orphan Items.
- All Entries should refer to an Item. In theory >1 Entry can point to the same Item. Where the RSF represents an addition to a Register, the Item may be part of the existing Register.


## Decision ##

- The requirement not to require parsing of the whole RSF rules out JSON.
- XML can be streamed but there are security objections to using it.
- The RSF will comprise Commands on separate lines, being one of those listed below.
- Each Command comprises the Command name with arguments separated by tabs.
- The Commands are applied to the register in the order they appear in the RSF.
- RSF must be encoded as UTF-8.

### add-item

#### arguments

- a canonicalized JSON string

Example

    add-item[tab]{"address":"5T8EXT7N","denominations":["C22"],"dioceses":["CE23"],"headteacher":"Mr T Wilson","maximum-age":"11","minimum-age":"3","name":"Sir John Cass's Foundation Primary School","school":"100000","school-authority":"201","school-gender":"M","school-phase":"PRI","school-type":"2","website":"http://www.sirjohncassprimary.org"}

#### Notes

- *add-item* must appear before the corresponding *append-entry*
- see the definition our canonicalization algorithm [here]( http://openregister.github.io/specification/#sha-256-item-hash)

### append-entry

#### arguments

- a timestamp in UTC in RFC 3339 format with the UTC offset of 00:00 represented by *Z*
- the hash of the corresponding Item prepended with *sha-256:*
- the *key* of the corresponding Item, being the mandatory field in the Item having the same name as the Register itself

Example

    append-entry[tab]2016-11-07T16:26:21Z[tab]sha-256:100171bf018db8598080d8d930040989d3ef788e7bc00def8ca083d25a0f40e5[tab]5T8EXT7N

#### Notes

- The reference can be to an Item that already exists in the register or one in the RSF.
- The entry number is inferred by the order that *append-entry* commands appear.
- If  *append-entry* does not reference an earlier *add-item* or Item in the register then the RSF should be rejected.

### assert-root-hash

#### arguments

- the root hash of the register prepended with *sha-256:*

Example

    assert-root-hash[tab]sha-256:100171bf018db8598080d8d930040989d3ef788e7bc00def8ca083d25a0f40e5

#### Notes

- *assert-root-hash* can exist anywhere in the file
- the argument is the root hash of the register after execution of all previous commands in the RSF on that register (whether it was initially empty or not)

## Status ##

Accepted.
