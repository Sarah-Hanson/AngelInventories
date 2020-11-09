package sarah.AngelInventories;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import sarah.AngelInventories.commands.*;
import sarah.AngelInventories.invManagement.MySQL;
import sarah.AngelInventories.invManagement.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;

public final class AngelInventories extends JavaPlugin {
    HashMap<Player,PlayerData> playerData;
    HashMap<String,Inventory> staticInventories;
    MySQL sql = new MySQL();

    @Override
    public void onEnable() {
        // Pull data from db to memory
        playerData = sql.getPlayerInventories();
        staticInventories = sql.getStaticInventories();

        // Register Commands
        this.getCommand("ti").setExecutor(new CommandTI(playerData));
        this.getCommand("saveInventory").setExecutor(new CommandSaveInventory(staticInventories));
        this.getCommand("setInventory").setExecutor(new CommandSetInventory(staticInventories));
        this.getCommand("recoverInventory").setExecutor(new CommandRecoverInventory(playerData));
        this.getCommand("removeInventory").setExecutor((new CommandRemoveInventory(staticInventories)));

        // Listener hooks
        getServer().getPluginManager().registerEvents(new SaveListener(this), this);
    }

    @Override
    public void onDisable() {
        save();
    }

    private void updateAllInvs () {
        playerData.forEach( (player,data) -> {
            data.SaveInv();
        });
    }

    private void setPlayersToInv0() {
        playerData.forEach( (player,data) -> {
            data.SetInv(1);
        });
    }

    public void save() {
        updateAllInvs();
        setPlayersToInv0(); // Workaround for me not storing current inventory anywhere,
        // can be replaced with another db table to store that, only caveat of this method is that it sets all players
        // back to their default inventory on plugin disable which some people could find mildly irritating

        sql.setInventories(playerData);
        sql.setStaticInventories(staticInventories);
    }

}
