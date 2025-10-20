package com.bapppis.core.item;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

import com.bapppis.core.item.itemEnums.ItemType;
import com.bapppis.core.item.itemEnums.WeaponType;

import java.util.ArrayList;

public class ItemLoader {
    private static final HashMap<Integer, Item> itemIdMap = new HashMap<>();
    private static final HashMap<String, Item> itemNameMap = new HashMap<>();
    private static boolean loaded = false;

    public static void loadItems() {
        if (loaded)
            return;
        forceReload();
    }

    /**
     * Force reload of all items, even if already loaded. Use in individual tests.
     */
    public static void forceReload() {
        itemIdMap.clear();
        itemNameMap.clear();
        loaded = false;

        // Ensure properties are loaded first so we can resolve them in items
        try {
            com.bapppis.core.property.PropertyLoader.loadProperties();
        } catch (Exception e) {
            // ignore if properties already loaded or loading fails
        }

        com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                .registerTypeAdapter(com.bapppis.core.Resistances.class,
                        new com.bapppis.core.util.ResistancesDeserializer())
                .create();
        try (ScanResult scanResult = new ClassGraph()
                .acceptPaths("data/items") // Scan all item folders
                .scan()) {
            for (Resource resource : scanResult.getAllResources()) {
                if (resource.getPath().endsWith(".json")) {
                    try (Reader reader = new InputStreamReader(resource.open())) {
                        // First, parse as a generic map to get the itemType
                        com.google.gson.JsonObject jsonObj = gson.fromJson(reader, com.google.gson.JsonObject.class);
                        ItemType type = ItemType.EQUIPMENT;
                        if (jsonObj.has("itemType")) {
                            try {
                                type = ItemType.valueOf(jsonObj.get("itemType").getAsString().toUpperCase());
                            } catch (Exception ignored) {
                            }
                        }
                        // Re-parse as the correct class
                        Item item = null;
                        switch (type) {
                            case WEAPON:
                                // Use weaponType to select subclass
                                String weaponTypeStr = jsonObj.has("weaponType") ? jsonObj.get("weaponType").getAsString().toUpperCase() : "";
                                WeaponType weaponType = null;
                                try {
                                    weaponType = WeaponType.valueOf(weaponTypeStr);
                                } catch (Exception ignored) {}
                                if (weaponType != null) {
                                    switch (weaponType) {
                                        case SLASH:
                                            item = gson.fromJson(jsonObj, com.bapppis.core.item.melee.slashweapon.SlashWeapon.class);
                                            break;
                                        case PIERCE:
                                            item = gson.fromJson(jsonObj, com.bapppis.core.item.melee.piercingweapon.PiercingWeapon.class);
                                            break;
                                        case BLUNT:
                                            item = gson.fromJson(jsonObj, com.bapppis.core.item.melee.bluntweapon.BluntWeapon.class);
                                            break;
                                        case STAFF:
                                            item = gson.fromJson(jsonObj, com.bapppis.core.item.magic.staff.Staff.class);
                                            break;
                                        case ARCANE:
                                            item = gson.fromJson(jsonObj, com.bapppis.core.item.magic.arcaneweapon.ArcaneWeapon.class);
                                            break;
                                        case MAGIC_PHYSICAL:
                                            item = gson.fromJson(jsonObj, com.bapppis.core.item.magic.magicphysicalweapon.MagicPhysicalWeapon.class);
                                            break;
                                        default:
                                            // fallback to Weapon if unknown
                                            item = null;
                                            break;
                                    }
                                }
                                break;
                            case CONSUMABLE:
                                item = gson.fromJson(jsonObj, Consumable.class);
                                break;
                            case MISC:
                                item = gson.fromJson(jsonObj, Misc.class);
                                break;
                            case EQUIPMENT:
                            default:
                                item = gson.fromJson(jsonObj, Equipment.class);
                                break;
                        }
                        if (item != null) {
                            // Resolve properties from property names to actual Property objects
                            List<String> propertyNames = null;
                            if (item instanceof Equipment) {
                                propertyNames = ((Equipment) item).getPropertyNames();
                            }

                            if (propertyNames != null && !propertyNames.isEmpty()) {
                                List<com.bapppis.core.property.Property> resolvedProps = new ArrayList<>();
                                for (String propNameOrId : propertyNames) {
                                    com.bapppis.core.property.Property prop = null;
                                    // Try as ID first
                                    try {
                                        int propId = Integer.parseInt(propNameOrId);
                                        prop = com.bapppis.core.property.PropertyLoader.getProperty(propId);
                                    } catch (NumberFormatException e) {
                                        // Not a number, try as name
                                        prop = com.bapppis.core.property.PropertyLoader.getPropertyByName(propNameOrId);
                                    }
                                    if (prop != null) {
                                        resolvedProps.add(prop.copy());
                                    }
                                }
                                if (!resolvedProps.isEmpty()) {
                                    item.setProperties(resolvedProps);
                                }
                            }

                            int id = item.getId();
                            String name = item.getName();
                            if (id > 0) {
                                itemIdMap.put(id, item);
                            }
                            if (name != null && !name.isEmpty()) {
                                String key = name.trim().toLowerCase();
                                String keyNoSpace = key.replaceAll("\\s+", "");
                                itemNameMap.put(key, item);
                                // register space-free variant if unique
                                if (!itemNameMap.containsKey(keyNoSpace)) {
                                    itemNameMap.put(keyNoSpace, item);
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("[ItemLoader] Error loading item from: " + resource.getPath());
                        e.printStackTrace();
                    }
                }
            }
            // Debug prints removed
        }
        loaded = true;
    }

    public static Item getItemById(int id) {
        return itemIdMap.get(id);
    }

    public static Item getItemByName(String name) {
        if (name == null)
            return null;
        String key = name.trim().toLowerCase();
        Item it = itemNameMap.get(key);
        if (it != null)
            return it;
        String keyNoSpace = key.replaceAll("\\s+", "");
        return itemNameMap.get(keyNoSpace);
    }

    public static List<Item> getAllItems() {
        return new ArrayList<>(itemIdMap.values());
    }

    public static java.util.List<String> getAllItemNames() {
        java.util.List<String> names = new java.util.ArrayList<>(itemNameMap.keySet());
        java.util.Collections.sort(names);
        return names;
    }
}
