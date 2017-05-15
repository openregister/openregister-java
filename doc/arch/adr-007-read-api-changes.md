# Decision record: Changes to the read API #

## Context ##

On 11 April 2017 some breaking changes were made to the read API responses. The change was made to allow
an entry to point to multiple items via their item-hashes. This change makes it possible for the 
registers data model to support multi-value indexes in the future, in addition to the current 
single-value register case.

Some additional changes to make the read API responses more consistent were also made.

## Decision ##

### Why we decided to extend entries rather than items ### 

As part of the Indexes work, two different ideas to enable a register to express the scenario where multiple items map to a key in a register were put forward.

1. Changing the structure of an item so that it contains multiple curies that link to a record in another register. Not changing the structure of the entry.
  
  ```
  append-item {"local-authority-type":"NMD","local-authorities":["local-authority-eng:LND","local-authority-eng:LEI","local-authority-eng:CHE"]}
  append-entry 2016-04-05T13:23:05Z sha-256:c1de7c598cc2b326018a66b006ad5db9a663ce012d897c223b25db36ca8382d1  NMD
  ```
  
2. Changing the structure of the entry so that it can contain a list of item-hashes rather than always only containing one. Not changing the structure or content of an item.
  
  ```
  append-item {"local-authority-eng":"LND","local-authority-type":"NMD","name":"London"}
  append-item {"local-authority-eng":"LEI","local-authority-type":"NMD","name":"Leicester"}
  append-item {"local-authority-eng":"CHE","local-authority-type":"NMD","name":"Cheshire"}
  append-entry 2016-04-05T13:23:05Z sha-256:490636974f8087e4518d222eba08851dd3e2b85095f2b1427ff6ecd3fa482435;sha-256:8b748c574bf975990e47e69df040b47126d2a0a3895b31dce73988fba2ba27d8;sha-256:eb3ee00e6149cd734a7fa7e1f01a5fbf5fb50e1b38a065fd97d6ad3017750351  NMD
  ```

The decision was made to implement the second and below are the reasons for this decision:

- Someone using an index will not need to go back to the original register to get the required information. It is in the index as the original item.
- Past versions of the index still point to the item that contains the data that caused that index-entry to be created. i.e. if the key of a past entry in the local-authority-by-type index is NMD, the item it points to from the original register will always contain local-authority-type=NMD.
- It means that there are no changes to any items, and there is therefore no need to worry about creating field-names that clash with other field-names in the field register.
- It is possible to interpret that a record has been removed from the current set of records without needing to dig into the item (item remains opaque).
- It is easier to create a index of a index, because the data from the original register is still available in the item.
- RSF for indexes remain independent of the original register.
- It is easier to self host a index as you do not need to also self host all dependent registers.

### Changes to the API ###

- Add the property `index-entry-number` to all entries. If the result comes from an index, this represents the entry-number for that entry in the index. For registers this property always returns the same value as the `entry-number` property.
- Remove the `item-hash` property from record representations because the resolved item data is also returned.
- Make sure the `key` property is returned for the entry data in record resources.
- Make sure /entries and /entry/entry-number both return an array for consistency in consuming both endoints. This also allows for a future scenario where a /entry/{entry-number} endpoint might return multiple entries.
- Make sure /records and /record/{key} both return a map object for consistency in consuming both endpoints. This also allows for a future scenario where a /record/{key} endpoint might return multiple records.

> /entry/{entry-number}.json

```
[
  {
    "index-entry-number":"1",
    "entry-number":"1",
    "entry-timestamp":"2016-04-05T13:23:05Z",
    "key":"SU",
    "item-hash": [
        "sha-256:e94c4a9ab00d951dadde848ee2c9fe51628b22ff2e0a88bff4cca6e4e6086d7a"
    ]
  }
]
```

> /entries.json

```
[
  {
    "index-entry-number":"1",
    "entry-number":"1",
    "entry-timestamp":"2016-04-05T13:23:05Z",
    "key":"SU",
    "item-hash": [
        "sha-256:e94c4a9ab00d951dadde848ee2c9fe51628b22ff2e0a88bff4cca6e4e6086d7a"
    ]
  },
  {
    "index-entry-number":"2",
    "entry-number":"2",
    "entry-timestamp":"2016-04-05T13:23:05Z",
    "key":"DE",
    "item-hash": [
        "sha-256:e03f97c2806206cdc2cc0f393d09b18a28c6f3e6218fc8c6f3aa2fdd7ef9d625"
    ]
  }
]
```

> /record/{key}.json

```
{
  "PT": {
    "index-entry-number":"147",
    "entry-number":"147",
    "entry-timestamp":"2016-04-05T13:23:05Z",
    "key":"PT",
    "item": [
      {
        "country":"PT",
        "official-name":"The Portuguese Republic",
        "name":"Portugal",
        "citizen-names":"Portuguese"
      }
    ]
  }
}
```

> /records.json

```
{
  "PT": {
    "index-entry-number":"147",
    "entry-number":"147",
    "entry-timestamp":"2016-04-05T13:23:05Z",
    "key":"PT",
    "item": [
      {
        "country":"PT",
        "official-name":"The Portuguese Republic",
        "name":"Portugal",
        "citizen-names":"Portuguese"
      }
    ]
  },
  "PW": {
    "index-entry-number":"140",
    "entry-number":"140",
    "entry-timestamp":"2016-04-05T13:23:05Z",
    "key":"PW",
    "item": [
      {
        "country":"PW",
        "official-name":"The Republic of Palau",
        "name":"Palau",
        "citizen-names":"Palauan"
      }
    ]
  }
}

```

## Status ##

Accepted.

## Consequences ##

These changes meant that some existing consumers of registers had to change their code to continue consuming the registers API.
We worked with consumers we knew about to make this as simple as possible. It was decided that it would be best to make such 
broad changes whilst we are still in alpha and don't have as many users as we would do later in beta.
