package sarah.angelinventories.invManagement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerData {
    Player player;
    ArrayList<Inventory> inventories;
    int cur_inv = 0;
    int max_inv;

    public PlayerData(ArrayList<Inventory> inventories, Player player) {
        this.inventories = inventories;
        this.player = player;
        max_inv = GetMaxInv();
        if (inventories.size() < max_inv) {
            for (int i = inventories.size(); i < max_inv; i++) {
                inventories.add(Bukkit.createInventory(null, InventoryType.PLAYER));
            }
        }
    }

    public void SaveInv() {
        inventories.get(cur_inv).setContents(player.getInventory().getContents());
    }

    //Increments the player's inventory by one, or resets it if over max
    public int ToggleInv() {
        SaveInv();
        if (cur_inv++ > max_inv) {
            cur_inv = 0;
        }
        player.getInventory().setContents(inventories.get(cur_inv).getContents());
        return cur_inv;
    }

    //Used to set players to a specific one of their inventories
    public void SetInv(int inv_num) {
        SaveInv();
        //Adjust the index to start at 0 instead of 1
        player.getInventory().setContents(inventories.get(inv_num).getContents());
    }

    //Dumps the last x inventories from the player onto the ground and removes them from the player
    public void DumpInvs(int dumpCount) {
        for (int i = 0; i < dumpCount; i++) {
            dump(inventories.size()-1); //Dump Last inventory on ground and delete
        }
    }

    //Dumps specified inventory number on ground then removes it from tracking
    private void dump(int invToDump) {
        for (ItemStack stack : inventories.get(invToDump).getContents()) {
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
        }
        inventories.remove(invToDump);
    }

    // Counts up on perms until it gets to one the player doesn't have, then sets max to the last number
    public int GetMaxInv() {
        for (int i = 0; i < 5; i++) {
            if (!player.hasPermission("AngelInventories.Inventories." + i)) {
                return i - 1;
            }
        }
        return 0; // Shouldn't actually get to this line, but the language needs it anyway
    }

    public int GetInvCount() {
        return inventories.size();
    }

    //Re-checks player's permissions and then handles the inventory recovery
    public boolean permCheckPlayer() {
        max_inv = GetMaxInv();
        if (GetInvCount() > GetMaxInv()) {
            int overage = GetInvCount() - GetMaxInv();
            DumpInvs(overage);
            return true;
        }
        return false;
    }
}
