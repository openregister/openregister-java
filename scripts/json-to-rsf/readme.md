## JSON to RSF converter

A Python 3 script to convert JSON to RSF. JSON must be an array of objects, the objects being the Items to be included. The script will 'canonicalize' the JSON so the ordering of the fields and the formatting does not matter e.g.:

```
[
    {
        "local-authority-eng": "BAS",
        "local-authority-type": "UA",
        "name": "Bath and North East Somerset",
        "official-name": "Bath and North East Somerset Council",
        "start-date": "1996-04-01"
    },
    {
        "local-authority-eng": "BBD",
        "local-authority-type": "UA",
        "name": "Blackburn",
        "official-name": "Blackburn",
        "start-date": "1996-04-01"
    }
]

```

### Usage

JSON will be read from the standard input and there are two required arguments for the Python script. 

entry-type: The type of the entry, either `user` or `system`
key-field-name: If the entry-type is `user`, this should be the name of the primary key field in the JSON (also the name of the register). If the entry-type is `system`, this should be the key of the entry.

    cat la.json | python3 json2rsf.py user local-authority-eng
    cat la-field.json | python3 json2rsf.py system field:local-authority-eng
