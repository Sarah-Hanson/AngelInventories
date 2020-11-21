package sarah.angelinventories.invManagement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import sarah.angelinventories.AngelInventories;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerData {
    AngelInventories plugin;
    Player player;
    ArrayList<Inventory> inventories;
    Integer currentPlayerInvIndex;
    String currentCustomInvName;

    public PlayerData(AngelInventories plugin, UUID uuid) throws IOException, SQLException {
        PlayerData playerData = plugin.mysql.getPlayerData(uuid);
        this.plugin = plugin;
        player = Bukkit.getPlayer(uuid);
        inventories = playerData.inventories;
        currentPlayerInvIndex = playerData.currentPlayerInvIndex;
        currentCustomInvName = playerData.currentCustomInvName;
    }

    public PlayerData(AngelInventories plugin, UUID uuid, ArrayList<Inventory> inventories, Integer currentPlayerInvIndex, String currentCustomInvName) {
        this.plugin = plugin;
        player = Bukkit.getPlayer(uuid);
        this.inventories = inventories;
        this.currentPlayerInvIndex = currentPlayerInvIndex;
        this.currentCustomInvName = currentCustomInvName;
    }

    public void SaveInv() throws Exception {
        if (currentCustomInvName.equals(null)) {
            if (player.isOnline()) {
                Inventory inventory = player.getInventory();
                inventories.set(currentPlayerInvIndex, inventory);
                //TODO: tell database to save this
            } else {
                //TODO: Better exceptions
                throw new Exception("Can't get an offline player's inventory.");
            }
        } else {
            //TODO: Better exceptions
            // Player has a custom inventory equipped and this would wipe their numbered inventory.
            throw new Exception("Please don't overwrite player's inventories with a custom inventory.");
        }
    }

    //Increments the player's inventory by one, or resets it if over max
    public void ToggleInv() {
        ToggleInv(false);
    }

    public void ToggleInv(Boolean backward) {
        SaveInv();
        if (GetMaxInv() == 1) {
            player.sendMessage("You don't have permission to toggle inventories.");
        } else {
            if (backward) {
                currentPlayerInvIndex--;
                if (currentPlayerInvIndex < 0) {
                    currentPlayerInvIndex = GetMaxInv();
                }
            } else {
                currentPlayerInvIndex++;
                if (currentPlayerInvIndex > GetMaxInv()) {
                    currentPlayerInvIndex = 0;
                }
            }
            player.getInventory().setContents(inventories.get(currentPlayerInvIndex).getContents());
        }
    }

    //Used to set players to a specific one of their inventories
    public void SetInv(int inv_num) {
        SaveInv();
        //Adjust the index to start at 0 instead of 1
        player.getInventory().setContents(inventories.get(inv_num).getContents());
    }

    //Dumps the last x inventories from the player onto the ground and removes them from the player
    //TODO: No.
//    public void DumpInvs(int dumpCount) {
//        for (int i = 0; i < dumpCount; i++) {
//            dump(inventories.size() - 1); //Dump Last inventory on ground and delete
//        }
//    }

    //Dumps specified inventory number on ground then removes it from tracking
    private void dump(int invToDump) {
        if (invToDump < inventories.size() && invToDump >= 0) {
            for (ItemStack stack : inventories.get(invToDump).getContents()) {
                if (stack != null) {
                    player.getWorld().dropItemNaturally(player.getLocation(), stack);
                }
            }
            inventories.remove(invToDump);
        } else {
            throw new IndexOutOfBoundsException("That inventory does not exist.");
        }
    }

    // Counts down permissions until it finds one the player has, meaning they don't have to have every permission from 2 to whatever amount we want them to have.
    public int GetMaxInv() {
        int maxInventories = 5;
        while (maxInventories > 1 && !player.hasPermission("AngelInventories.Inventories." + maxInventories)) {
            maxInventories--;
        }
        return maxInventories;
    }

    public int GetInvCount() {
        return inventories.size();
    }

    //Re-checks player's permissions and then handles the inventory recovery
    public boolean permCheckPlayer() {
        Integer max_inv = GetMaxInv();
        if (GetInvCount() > GetMaxInv()) {
            int overage = GetInvCount() - GetMaxInv();
            DumpInvs(overage);
            return true;
        }
        return false;
    }
}
