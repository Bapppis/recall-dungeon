#!/usr/bin/env python3
"""Format all JSON files under src/main/resources/data with 2-space indent.

This is intentionally simple and avoids changing key order.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
data_dir = root / 'src' / 'main' / 'resources' / 'data'

changed = []
for p in data_dir.rglob('*.json'):
    try:
        text = p.read_text(encoding='utf-8')
        # load and dump to normalize formatting
        obj = json.loads(text)
        new_text = json.dumps(obj, ensure_ascii=False, indent=2) + "\n"
        if new_text != text:
            p.write_text(new_text, encoding='utf-8')
            changed.append(str(p.relative_to(root)))
    except Exception as e:
        print(f"Skipping {p}: {e}")

if changed:
    print('Reformatted files:')
    for c in changed:
        print(c)
else:
    print('No JSON files needed formatting.')
