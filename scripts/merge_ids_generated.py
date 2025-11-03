import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / 'src' / 'main' / 'resources' / 'data'
IDS_GEN = DATA_DIR / 'IDS_generated.md'
IDS = DATA_DIR / 'IDS.md'

if not IDS_GEN.exists():
    print(f'Missing {IDS_GEN}. Run generate_ids.py first.')
    raise SystemExit(1)
if not IDS.exists():
    print(f'Missing {IDS}.')
    raise SystemExit(1)

# Parse generated entries
entry_re = re.compile(r'^- (\d+) — (.*?) — (data/.*)$')
generated_entries = []
for line in IDS_GEN.read_text(encoding='utf-8').splitlines():
    m = entry_re.match(line)
    if m:
        idv = int(m.group(1))
        name = m.group(2).strip()
        path = m.group(3).strip()
        generated_entries.append((idv, name, path))

# Build map: top -> list of lines
from collections import defaultdict
gen_by_top = defaultdict(list)
for idv, name, path in generated_entries:
    # path starts with data/
    parts = path.split('/')
    top = parts[1] if len(parts) > 1 else parts[0]
    gen_by_top[top].append((idv, name, path))

# Read existing IDS.md
ids_lines = IDS.read_text(encoding='utf-8').splitlines()
# Find header indices for '## ' lines
header_indices = []
for i, line in enumerate(ids_lines):
    if line.startswith('## '):
        header_indices.append((i, line))

# Build a map from top(lower) -> (start_idx, end_idx)
section_map = {}
for idx, (i, header) in enumerate(header_indices):
    start = i
    end = header_indices[idx+1][0] if idx+1 < len(header_indices) else len(ids_lines)
    # header like '## Spells (50000-50999)'
    header_lower = header.lower()
    section_map[header_lower] = (start, end)

# Helper to find a section by top name
def find_section_for_top(top):
    # try to match header line containing top (case-insensitive)
    for header_lower, (start, end) in section_map.items():
        if f'## {top.lower()}' in header_lower:
            return start, end
        # also match plural/singular roughly
        if top.lower() in header_lower:
            return start, end
    return None

# Collect missing entries
missing_by_top = defaultdict(list)
for top, entries in gen_by_top.items():
    # Determine existing ids in that section
    section = find_section_for_top(top)
    existing_ids = set()
    if section:
        s, e = section
        for line in ids_lines[s:e]:
            m = entry_re.match(line)
            if m:
                existing_ids.add(int(m.group(1)))
    else:
        # no matching section
        existing_ids = set()
    for idv, name, path in entries:
        if idv not in existing_ids:
            missing_by_top[top].append((idv, name, path))

# If nothing missing, exit
if not any(missing_by_top.values()):
    print('No missing entries found; IDS.md is up to date with IDS_generated.md')
    raise SystemExit(0)

# Prepare to insert missing entries
# We'll insert under the corresponding header section; if no matching header, append a new section at end
new_lines = list(ids_lines)
# To avoid shifting indices, collect edits and apply from bottom to top
edits = []  # tuples (insert_index, lines_to_insert)
for top, items in missing_by_top.items():
    items_sorted = sorted(items)
    sec = find_section_for_top(top)
    insert_idx = None
    if sec:
        s, e = sec
        # find position after the header and any blank line following it
        # header is at s, so insert after header and following blank lines but before other content
        insert_idx = s + 1
        # advance past one blank line if present
        if insert_idx < len(new_lines) and new_lines[insert_idx].strip() == '':
            insert_idx += 1
        # We will insert the missing items here
    else:
        # append new section at end
        insert_idx = len(new_lines)
        # add a blank line and header
        header = f'## {top.capitalize()}'
        edits.append((insert_idx, ["", header, ""]))
        insert_idx += 3
    # build entry lines
    entry_lines = [f'- {idv} — {name} — {path}' for idv, name, path in items_sorted]
    edits.append((insert_idx, entry_lines))

# Apply edits from last to first
for insert_idx, lines_to_insert in sorted(edits, key=lambda x: x[0], reverse=True):
    for ln in reversed(lines_to_insert):
        new_lines.insert(insert_idx, ln)

# Write back IDS.md (make a backup first)
backup = IDS.with_suffix('.md.bak')
backup.write_text('\n'.join(ids_lines) + '\n', encoding='utf-8')
IDS.write_text('\n'.join(new_lines) + '\n', encoding='utf-8')

# Print summary
print('Inserted missing entries into IDS.md:')
for top, items in missing_by_top.items():
    for idv, name, path in sorted(items):
        print(f'- {idv} — {name} — {path} (section: {top})')
print(f'Backup of previous IDS.md written to {backup}')
print('Done.')
