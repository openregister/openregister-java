import sys
from datetime import datetime
import json
import hashlib


def print_rsf(item, entry_type, key_field):
    key = item[key_field] if entry_type == 'user' else key_field
    timestamp = datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')
    item_str = json.dumps(item, separators=(',', ':'), sort_keys=True)
    item_hash = hashlib.sha256(item_str.encode("utf-8")).hexdigest()
    item_line = "add-item\t" + item_str
    entry_line = "append-entry\t{0}\t{1}\t{2}\tsha-256:{3}".format(
            entry_type, key, timestamp, item_hash)
    print(item_line)
    print(entry_line)

item_arr = json.load(sys.stdin)

if len(sys.argv) < 2:
    sys.exit("Usage: cat foo.json | python json2rsf.py [entry-type key-field-name]")
if not isinstance(item_arr, list):
    sys.exit("Error: input must be json array")
entryType = sys.argv[1]
key = sys.argv[2]
[print_rsf(item, entryType, key) for item in item_arr]
