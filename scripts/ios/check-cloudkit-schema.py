#!/usr/bin/env python3
"""Fail when iosApp/cloudkit/schema.ckdb and the CloudKit record code disagree.

TestFlight and App Store builds use the CloudKit production schema, where record types and fields are not created on
the fly, so a field the code writes but the schema lacks fails every save of that record on a real device.
"""
import re
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[2]
schema = (root / "iosApp/cloudkit/schema.ckdb").read_text()
code = (root / "iosApp/Jackdaw/SDK/Sync/CloudRecords.swift").read_text()

schema = re.sub(r"//[^\n]*", "", schema)
declared = {}
for match in re.finditer(r"RECORD TYPE (\w+)\s*\((.*?)\n\s*\);", schema, re.S):
    fields = {}
    for line in match.group(2).split("\n"):
        line = line.strip().rstrip(",")
        if not line or line.startswith("GRANT") or line.startswith('"___'):
            continue
        name, kind = line.split()[:2]
        fields[name] = kind
    declared[match.group(1)] = fields

types = dict(re.findall(r'static let (\w+) = "(\w+)"', code))
used = {}
for key, function in (("pack", "pack: PackWire"), ("review", "review: ReviewWire"), ("setting", "setting: SettingWire")):
    start = code.index(f"static func fill(_ record: CKRecord, {function}")
    body = code[start : code.index("\n    }\n", start)]
    fields = {}
    for kind, name in re.findall(r'record\.set(String|Int)\("(\w+)"', body):
        fields[name] = "STRING" if kind == "String" else "INT64"
    for name in re.findall(r'record\["(\w+)"\]', body):
        fields[name] = "ASSET"
    used[types[key]] = fields

failed = False
for record_type, fields in used.items():
    if record_type not in declared:
        print(f"schema.ckdb has no record type {record_type}")
        failed = True
        continue
    if declared[record_type] != fields:
        print(f"{record_type}: schema.ckdb has {declared[record_type]}, the code writes {fields}")
        failed = True
for record_type in declared:
    if record_type not in used:
        print(f"schema.ckdb declares {record_type}, which the code never writes")
        failed = True
sys.exit(1 if failed else 0)
