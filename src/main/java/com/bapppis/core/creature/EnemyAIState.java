package com.bapppis.core.creature;

/**
 * Represents the AI state of an enemy creature.
 *
 * PATROLLING: Enemy follows patrol route, ignores player unless they come into vision
 * CHASING: Enemy has line of sight to player and actively pursues them
 * INVESTIGATING: Enemy lost sight of player, moving to last known position
 * ALERT: Enemy reached last known position but didn't find player, briefly looks around before resuming patrol
 */
public enum EnemyAIState {
    PATROLLING,
    CHASING,
    INVESTIGATING,
    ALERT
}
