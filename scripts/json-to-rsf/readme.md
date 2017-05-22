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

JSON will be read from the standard input. 

The name of the field which is the Key for the item local-authority-eng in the example.
Must be passed as an argument.

    cat la.json | python3 json2rsf.py local-authority-eng
