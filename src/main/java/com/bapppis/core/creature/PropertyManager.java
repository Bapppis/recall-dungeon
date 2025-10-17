package com.bapppis.core.creature;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bapppis.core.property.Property;

/**
 * Manages per-creature properties (buffs, debuffs, traits): storage, apply/remove,
 * ticking and formatting helpers. Kept in the same package so it can call
 * package-private Creature helpers when necessary.
 */
public class PropertyManager {
    private final Creature owner;
    private final HashMap<Integer, Property> buffs = new HashMap<>();
    private final HashMap<Integer, Property> debuffs = new HashMap<>();
    private final HashMap<Integer, Property> traits = new HashMap<>();

    public PropertyManager(Creature owner) {
        this.owner = owner;
    }

    public HashMap<Integer, Property> getBuffs() {
        return buffs;
    }

    public HashMap<Integer, Property> getDebuffs() {
        return debuffs;
    }

    public HashMap<Integer, Property> getTraits() {
        return traits;
    }

    /** Apply a Property instance (creates per-creature copy when available). */
    public void add(Property property) {
        if (property == null) return;
        int id = property.getId();

        // Avoid double-applying same id
        if ((id >= 1000 && id < 2333 && buffs.containsKey(id))
                || (id >= 2333 && id < 3666 && debuffs.containsKey(id))
                || (id >= 3666 && id < 5000 && traits.containsKey(id))) {
            return;
        }

        Property instanceToApply = property;
        try {
            if (property != null) {
                instanceToApply = property.copy();
            }
        } catch (Exception ignored) {
            instanceToApply = property;
        }

        if (id >= 1000 && id < 2333) {
            buffs.put(id, instanceToApply);
        } else if (id >= 2333 && id < 3666) {
            debuffs.put(id, instanceToApply);
        } else if (id >= 3666 && id < 5000) {
            traits.put(id, instanceToApply);
        }

        try {
            instanceToApply.onApply(owner);
        } catch (Exception ignored) {
        }
    }

    /** Lookup by id and apply. Returns true if applied. */
    public boolean addById(int id) {
        try {
            Property prop = com.bapppis.core.property.PropertyLoader.getProperty(id);
            if (prop != null) {
                add(prop);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Lookup by human name (case-insensitive). Returns true if applied. */
    public boolean addByName(String name) {
        if (name == null || name.isBlank()) return false;
        String t = name.trim();
        try {
            int id = Integer.parseInt(t);
            Property p = com.bapppis.core.property.PropertyLoader.getProperty(id);
            if (p != null) {
                add(p);
                return true;
            }
        } catch (NumberFormatException ignored) {
        }

        Property p = com.bapppis.core.property.PropertyLoader.getPropertyByName(name);
        if (p != null) {
            add(p);
            return true;
        }
        return false;
    }

    /** Remove property by id and call onRemove. */
    public void remove(int id) {
        Property property = null;
        if (id >= 1000 && id < 2333) {
            property = buffs.remove(id);
        } else if (id >= 2333 && id < 3666) {
            property = debuffs.remove(id);
        } else if (id >= 3666 && id < 5000) {
            property = traits.remove(id);
        }
        if (property != null) {
            try {
                property.onRemove(owner);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Remove a currently-applied property by human name (case-insensitive)
     * or by numeric id string. Returns true if a property was found and removed.
     */
    public boolean removeByName(String name) {
        if (name == null || name.isBlank()) return false;
        String t = name.trim();
        // Try numeric id first
        try {
            int id = Integer.parseInt(t);
            if (buffs.containsKey(id) || debuffs.containsKey(id) || traits.containsKey(id)) {
                remove(id);
                return true;
            }
            return false;
        } catch (NumberFormatException ignored) {
        }

        try {
            Property p = com.bapppis.core.property.PropertyLoader.getPropertyByName(name);
            if (p == null) return false;
            int id = p.getId();
            if (buffs.containsKey(id) || debuffs.containsKey(id) || traits.containsKey(id)) {
                remove(id);
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Tick all properties: onTick then decrement durations and remove expired. */
    public void tick() {
        // Regen resources first
        try {
            owner.modifyHp(owner.getHpRegen());
            owner.modifyMana(owner.getManaRegen());
            owner.modifyStamina(owner.getStaminaRegen());
        } catch (Exception ignored) {
        }

        if ((buffs == null || buffs.isEmpty()) && (debuffs == null || debuffs.isEmpty())) {
            return;
        }

        List<Integer> toRemove = null;

        for (Map.Entry<Integer, Property> e : buffs.entrySet()) {
            Property prop = e.getValue();
            try { prop.onTick(owner); } catch (Exception ignored) {}
            Integer d = prop.getDuration();
            if (d != null && d > 0) {
                prop.setDuration(d - 1);
                if (prop.getDuration() != null && prop.getDuration() <= 0) {
                    if (toRemove == null) toRemove = new java.util.ArrayList<>();
                    toRemove.add(e.getKey());
                }
            }
        }

        for (Map.Entry<Integer, Property> e : debuffs.entrySet()) {
            Property prop = e.getValue();
            try { prop.onTick(owner); } catch (Exception ignored) {}
            Integer d2 = prop.getDuration();
            if (d2 != null && d2 > 0) {
                prop.setDuration(d2 - 1);
                if (prop.getDuration() != null && prop.getDuration() <= 0) {
                    if (toRemove == null) toRemove = new java.util.ArrayList<>();
                    toRemove.add(e.getKey());
                }
            }
        }

        if (toRemove != null) {
            for (int id : toRemove) remove(id);
        }
    }

    /** Pretty-print status effects to stdout (keeps existing behavior). */
    public void printStatusEffects() {
        System.out.println("Buffs:");
        for (Property buff : buffs.values()) System.out.println(" - " + buff);
        System.out.println("Debuffs:");
        for (Property debuff : debuffs.values()) System.out.println(" - " + debuff);
        System.out.println("Traits:");
        for (Property trait : traits.values()) System.out.println(" - " + trait);
    }

    public String printProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append("Buffs:\n");
        for (Property buff : buffs.values()) {
            sb.append("  ").append(formatPropertySummary(buff)).append("\n");
            String tt = buff.getTooltip();
            if (tt != null && !tt.isBlank()) {
                String[] parts = tt.split("\\n");
                for (String line : parts) sb.append("    tooltip: ").append(line).append("\n");
            }
        }
        sb.append("Debuffs:\n");
        for (Property debuff : debuffs.values()) {
            sb.append("  ").append(formatPropertySummary(debuff)).append("\n");
            String tt = debuff.getTooltip();
            if (tt != null && !tt.isBlank()) {
                String[] parts = tt.split("\\n");
                for (String line : parts) sb.append("    tooltip: ").append(line).append("\n");
            }
        }
        sb.append("Traits:\n");
        for (Property trait : traits.values()) {
            sb.append("  ").append(formatPropertySummary(trait)).append("\n");
            String tt = trait.getTooltip();
            if (tt != null && !tt.isBlank()) {
                String[] parts = tt.split("\\n");
                for (String line : parts) sb.append("    tooltip: ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public String formatPropertiesInColumns(java.util.Collection<Property> props, int columns) {
        StringBuilder sb = new StringBuilder();
        if (props == null || props.isEmpty()) {
            sb.append("  (none)\n");
            return sb.toString();
        }

        java.util.List<String> cells = new java.util.ArrayList<>();
        int maxCellLen = 0;
        for (Property p : props) {
            String s = formatPropertySummary(p);
            try {
                String tt = p.getTooltip();
                if (tt != null && !tt.isBlank()) {
                    String first = tt.split("\\n")[0];
                    String snippet = first.length() > 40 ? first.substring(0, 37) + "..." : first;
                    s = s + " - " + snippet;
                }
            } catch (Exception ignored) {}
            cells.add(s);
            if (s.length() > maxCellLen) maxCellLen = s.length();
        }

        int idx = 0;
        for (String cell : cells) {
            String padded = String.format("  %-%ds", maxCellLen).replace("%-%d", "%-" + maxCellLen);
            // fallback safe formatting
            padded = String.format("  %" + "-" + maxCellLen + "s", cell);
            sb.append(padded);
            idx++;
            if (idx % columns == 0) sb.append('\n'); else sb.append("   ");
        }
        if (idx % columns != 0) sb.append('\n');
        return sb.toString();
    }

    public String formatPropertySummary(Property p) {
        if (p == null) return "<null>";
        StringBuilder s = new StringBuilder();
        String name = p.getName() == null ? "<unnamed>" : p.getName();
        s.append(name).append(" (").append(p.getId()).append(")");
        String desc = p.getDescription();
        if (desc != null && !desc.isBlank()) s.append(": ").append(desc);
        Integer d = p.getDuration(); if (d != null) s.append(" [dur=").append(d).append("]");
        Integer hr = p.getHpRegen(); if (hr != null && hr != 0) s.append(" [hpRegen=").append(hr).append("]");
        Integer mr = p.getManaRegen(); if (mr != null && mr != 0) s.append(" [manaRegen=").append(mr).append("]");
        Integer sr = p.getStaminaRegen(); if (sr != null && sr != 0) s.append(" [staminaRegen=").append(sr).append("]");
        try {
            if (p.getStatModifiers() != null && !p.getStatModifiers().isEmpty()) s.append(" stats=").append(p.getStatModifiers().toString());
            if (p.getResistanceModifiers() != null && !p.getResistanceModifiers().isEmpty()) s.append(" resists=").append(p.getResistanceModifiers().toString());
            if (p.getVisionRangeModifier() != null) s.append(" visionRange=").append(p.getVisionRangeModifier());
        } catch (Exception ignored) {}
        return s.toString();
    }
}
