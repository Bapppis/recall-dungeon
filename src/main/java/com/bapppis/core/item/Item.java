
package com.bapppis.core.item;

import com.bapppis.core.item.itemEnums.EquipmentSlot;
import com.bapppis.core.item.itemEnums.ItemType;
import com.bapppis.core.property.Property;
import java.util.List;


public interface Item {
    String getName();
    String getDescription();
    String getTooltip();
    ItemType getType();
    int getId();
    EquipmentSlot getSlot();
    void setSlot(EquipmentSlot slot);
    String getDroppedSprite(); // Sprite to use when item is dropped on ground

    List<Property> getProperties();
    void setProperties(List<Property> properties);

    default void onApply(com.bapppis.core.creature.Creature creature) {
        List<Property> props = getProperties();
        if (props != null) {
            for (Property p : props) {
                if (p != null) creature.addProperty(p);
            }
        }
    }
    default void onRemove(com.bapppis.core.creature.Creature creature) {
        List<Property> props = getProperties();
        if (props != null) {
            for (Property p : props) {
                if (p != null) creature.removeProperty(p.getId());
            }
        }
    }
}
