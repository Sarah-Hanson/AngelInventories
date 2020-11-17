package sarah.angelinventories.invManagement;

public enum PublicInventorySetting {
    NORMAL,
    // Replace inventories don't set the player's current inventory, therefore overwriting the player's current personal inventory.
    // Used for first join.
    REPLACE,
    // Locked inventories prevent the player from simply toggling away with /ti.
    // Used for jail, creative uniform etc.
    LOCKED
}
