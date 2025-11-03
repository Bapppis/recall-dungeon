from generate_tooltips import generate_spell_tooltip

# Spell with staminaCost instead of manaCost
spell = {
    "id": "test-stamina-spell",
    "name": "Stamina Strike",
    "staminaCost": 5,
    "times": 1,
    "damageDice": "1d6",
    "damageType": "SLASHING",
    "statBonuses": ["STRENGTH"],
    "critMod": "+10",
}

lines = generate_spell_tooltip(spell)
print('GENERATED TOOLTIP LINES:')
for l in lines:
    print(l)
