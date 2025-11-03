import json
from pathlib import Path

from generate_tooltips import generate_spell_tooltip

project_root = Path(__file__).resolve().parent.parent
spell_path = project_root / 'src' / 'main' / 'resources' / 'data' / 'spells' / "Serpent's Fang.json"

if not spell_path.exists():
    print(f"Spell file not found: {spell_path}")
    raise SystemExit(1)

with open(spell_path, 'r', encoding='utf-8') as f:
    spell = json.load(f)

new_tooltip = generate_spell_tooltip(spell)
spell['tooltip'] = new_tooltip

with open(spell_path, 'w', encoding='utf-8') as f:
    json.dump(spell, f, ensure_ascii=False, indent=2)
    f.write('\n')

print(f"Updated tooltip for {spell_path.name} - {len(new_tooltip)} lines.")
