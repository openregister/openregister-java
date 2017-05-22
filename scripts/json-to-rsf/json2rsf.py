import sys
from datetime import datetime
import json
import hashlib


def print_rsf(item, key_field):
    key = item[key_field]
    timestamp = datetime.now().isoformat(sep='T')
    item_str = json.dumps(item, separators=(',', ':'), sort_keys=True)
    item_hash = hashlib.sha256(item_str.encode("utf-8")).hexdigest()
    item_line = "add-item\t" + item_str
    entry_line = "append-entry\t{0}\t{1}\tsha-256:{2}".format(
            key, timestamp, item_hash)
    print(item_line)
    print(entry_line)


item_arr = json.load(sys.stdin)
if len(sys.argv) < 2:
    sys.exit("Usage: cat foo.json | python json2rsf.py [key field name]")
if not isinstance(item_arr, list):
    sys.exit("Error: input must be json array")
key = sys.argv[1]
[print_rsf(item, key) for item in item_arr]
