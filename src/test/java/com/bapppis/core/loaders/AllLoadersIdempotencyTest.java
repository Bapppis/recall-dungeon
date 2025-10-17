package com.bapppis.core.loaders;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.bapppis.core.AllLoaders;
import com.bapppis.core.property.PropertyLoader;
import com.bapppis.core.item.ItemLoader;

public class AllLoadersIdempotencyTest {

    @Test
    public void testLoadAllIsIdempotent() {
        AllLoaders.loadAll();
        int propertyCount1 = PropertyLoader.getAllProperties().size();
        int itemCount1 = ItemLoader.getAllItems().size();

        // call again should not change sizes
        AllLoaders.loadAll();
        int propertyCount2 = PropertyLoader.getAllProperties().size();
        int itemCount2 = ItemLoader.getAllItems().size();

        assertEquals(propertyCount1, propertyCount2, "Properties should not duplicate on repeated loadAll()");
        assertEquals(itemCount1, itemCount2, "Items should not duplicate on repeated loadAll()");
    }
}
