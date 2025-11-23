package com.bapppis.core.game;

import com.bapppis.core.dungeon.Coordinate;

/**
 * Callback interface for showing loot transfer UI from commands
 */
public interface LootTransferCallback {
    void showLootTransferDialog(Coordinate tileCoordinate);
}
