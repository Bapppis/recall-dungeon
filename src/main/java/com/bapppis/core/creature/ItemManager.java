package com.bapppis.core.creature;

import com.bapppis.core.item.*;
import java.util.Map;
import com.bapppis.core.Resistances;
import com.bapppis.core.Stats;

class ItemManager {

    static boolean equip(Creature creature, Item item, boolean requestTwoHanded) {
        if (item == null || creature == null)
            return false;

        EquipmentSlot slot = item.getSlot();
        Item oldItem = null;
        boolean willBeTwoHanded = false;

        try {
            if (item instanceof Weapon) {
                Weapon w = (Weapon) item;
                if (w.isTwoHanded()) {
                    willBeTwoHanded = true;
                } else if (w.getVersatile() && requestTwoHanded) {
                    willBeTwoHanded = true;
                }
            }
        } catch (Exception ignored) {
        }

        if (willBeTwoHanded) {
            oldItem = creature.removeEquipped(EquipmentSlot.WEAPON);
            if (oldItem != null) {
                removeEffects(creature, oldItem);
            }
            oldItem = creature.removeEquipped(EquipmentSlot.OFFHAND);
            if (oldItem != null) {
                removeEffects(creature, oldItem);
            }
            creature.putEquipped(EquipmentSlot.WEAPON, item);
            creature.putEquipped(EquipmentSlot.OFFHAND, item);
        } else if (slot != null) {
            oldItem = creature.removeEquipped(slot);
            if (oldItem != null) {
                removeEffects(creature, oldItem);
            }
            creature.putEquipped(slot, item);
        }

        try {
            creature.getInventory().removeItem(item);
        } catch (Exception ignored) {
        }

        applyEffects(creature, item);

        return true;
    }

    static boolean equip(Creature creature, Item item) {
        return equip(creature, item, false);
    }

    static void unequip(Creature creature, EquipmentSlot slot) {
        if (creature == null || slot == null)
            return;

        Item item = creature.removeEquipped(slot);
        if (item == null)
            return;

        try {
            if (item instanceof Weapon && ((Weapon) item).isTwoHanded()) {
                EquipmentSlot other = (slot == EquipmentSlot.WEAPON) ? EquipmentSlot.OFFHAND
                        : EquipmentSlot.WEAPON;
                Item otherItem = creature.getEquipped(other);
                if (otherItem != null && otherItem == item) {
                    creature.removeEquipped(other);
                }
            }
        } catch (Exception ignored) {
        }

        removeEffects(creature, item);

        boolean alreadyInInventory = false;
        try {
            int id = item.getId();
            for (Item it : creature.getInventory().getWeapons())
                if (it.getId() == id) {
                    alreadyInInventory = true;
                    break;
                }
            if (!alreadyInInventory)
                for (Item it : creature.getInventory().getOffhands())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : creature.getInventory().getHelmets())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : creature.getInventory().getArmors())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : creature.getInventory().getLegwear())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : creature.getInventory().getConsumables())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
            if (!alreadyInInventory)
                for (Item it : creature.getInventory().getMisc())
                    if (it.getId() == id) {
                        alreadyInInventory = true;
                        break;
                    }
        } catch (Exception e) {
            alreadyInInventory = false;
        }

        if (!alreadyInInventory) {
            boolean added = creature.getInventory().addItem(item);
            if (!added) {
                System.out.println("Inventory full! Could not add " + item.getName() + " back to inventory.");
            }
        }
    }

    static void applyEffects(Creature creature, Item item) {
        if (item instanceof Equipment) {
            Equipment eq = (Equipment) item;
            if (eq.getStats() != null) {
                for (Map.Entry<String, Integer> entry : eq.getStats().entrySet()) {
                    String key = entry.getKey();
                    if (key.equalsIgnoreCase("VISION_RANGE")) {
                        creature.setVisionRange(creature.getVisionRange() + entry.getValue());
                    } else {
                        try {
                            if (key.equalsIgnoreCase("ACCURACY")) {
                                creature.adjustEquipmentAccuracy(entry.getValue());
                            } else if (key.equalsIgnoreCase("MAGIC_ACCURACY")) {
                                creature.adjustEquipmentMagicAccuracy(entry.getValue());
                            } else {
                                Stats stat = Stats.valueOf(key);
                                creature.adjustEquipmentStat(stat, entry.getValue());
                                creature.modifyStat(stat, entry.getValue());
                            }
                        } catch (IllegalArgumentException e) {
                            // ignore unknown stats
                        }
                    }
                }
            }
            if (eq.getResistances() != null) {
                for (Map.Entry<String, Integer> entry : eq.getResistances().entrySet()) {
                    try {
                        Resistances res = Resistances.valueOf(entry.getKey());
                        creature.adjustEquipmentResist(res, entry.getValue());
                        creature.modifyResistance(res, entry.getValue());
                    } catch (IllegalArgumentException e) {
                    }
                }
            }

            if (eq.getCrit() != null && eq.getCrit() != 0f) {
                creature.adjustEquipmentCrit(eq.getCrit());
            }
            if (eq.getDodge() != null && eq.getDodge() != 0f) {
                creature.adjustEquipmentDodge(eq.getDodge());
            }
            if (eq.getBlock() != null && eq.getBlock() != 0f) {
                creature.adjustEquipmentBlock(eq.getBlock());
            }
            if (eq.getMagicResist() != null && eq.getMagicResist() != 0f) {
                creature.adjustEquipmentMagicResist(eq.getMagicResist());
            }
            if (eq.getAccuracy() != null && eq.getAccuracy() != 0) {
                creature.adjustEquipmentAccuracy(eq.getAccuracy());
            }
            if (eq.getMagicAccuracy() != null && eq.getMagicAccuracy() != 0) {
                creature.adjustEquipmentMagicAccuracy(eq.getMagicAccuracy());
            }
        }
        item.onApply(creature);
        creature.recalcDerivedStats();
    }

    static void removeEffects(Creature creature, Item item) {
        if (item instanceof Equipment) {
            Equipment eq = (Equipment) item;
            if (eq.getStats() != null) {
                for (Map.Entry<String, Integer> entry : eq.getStats().entrySet()) {
                    String key = entry.getKey();
                    if (key.equalsIgnoreCase("VISION_RANGE")) {
                        creature.setVisionRange(creature.getVisionRange() - entry.getValue());
                    } else {
                        try {
                            if (key.equalsIgnoreCase("ACCURACY")) {
                                creature.adjustEquipmentAccuracy(-entry.getValue());
                            } else if (key.equalsIgnoreCase("MAGIC_ACCURACY")) {
                                creature.adjustEquipmentMagicAccuracy(-entry.getValue());
                            } else {
                                Stats stat = Stats.valueOf(key);
                                creature.adjustEquipmentStat(stat, -entry.getValue());
                                creature.modifyStat(stat, -entry.getValue());
                            }
                        } catch (IllegalArgumentException e) {
                            // ignore unknown stats
                        }
                    }
                }
            }
            if (eq.getResistances() != null) {
                for (Map.Entry<String, Integer> entry : eq.getResistances().entrySet()) {
                    try {
                        Resistances res = Resistances.valueOf(entry.getKey());
                        creature.adjustEquipmentResist(res, -entry.getValue());
                        creature.modifyResistance(res, -entry.getValue());
                    } catch (IllegalArgumentException e) {
                    }
                }
            }

            if (eq.getCrit() != null && eq.getCrit() != 0f) {
                creature.adjustEquipmentCrit(-eq.getCrit());
            }
            if (eq.getDodge() != null && eq.getDodge() != 0f) {
                creature.adjustEquipmentDodge(-eq.getDodge());
            }
            if (eq.getBlock() != null && eq.getBlock() != 0f) {
                creature.adjustEquipmentBlock(-eq.getBlock());
            }
            if (eq.getMagicResist() != null && eq.getMagicResist() != 0f) {
                creature.adjustEquipmentMagicResist(-eq.getMagicResist());
            }
            if (eq.getAccuracy() != null && eq.getAccuracy() != 0) {
                creature.adjustEquipmentAccuracy(-eq.getAccuracy());
            }
            if (eq.getMagicAccuracy() != null && eq.getMagicAccuracy() != 0) {
                creature.adjustEquipmentMagicAccuracy(-eq.getMagicAccuracy());
            }
        }
        item.onRemove(creature);
        creature.recalcDerivedStats();
    }
}
