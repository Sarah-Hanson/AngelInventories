package sarah.angelinventories;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import sarah.angelinventories.commands.*;
import sarah.angelinventories.invManagement.MySQL;
import sarah.angelinventories.invManagement.PlayerData;

import java.util.HashMap;

public final class AngelInventories extends JavaPlugin {
    HashMap<Player, PlayerData> playerData;
    HashMap<String, Inventory> staticInventories;
    MySQL sql = new MySQL(this);
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        // Config Stuff
        config.addDefault("debugging", false);
        config.addDefault("mysql.host", "127.0.0.1");
        config.addDefault("mysql.database", "AngelInventories");
        config.addDefault("mysql.username", "username");
        config.addDefault("mysql.password", "password");
        config.options().copyDefaults(true);
        saveConfig();

        // Pull data from db to memory
        playerData = sql.getPlayerInventories();
        staticInventories = sql.getStaticInventories();

        // Register Commands
        getCommand("ti").setExecutor(new CommandTI(playerData));
        getCommand("saveInventory").setExecutor(new CommandSaveInventory(staticInventories));
        getCommand("setInventory").setExecutor(new CommandSetInventory(staticInventories));
        getCommand("recoverInventory").setExecutor(new CommandRecoverInventory(playerData));
        getCommand("removeInventory").setExecutor((new CommandRemoveInventory(staticInventories)));

        // Listener hooks
        getServer().getPluginManager().registerEvents(new SaveListener(this), this);
    }

    @Override
    public void onDisable() {
        save();
    }

    private void updateAllInvs() {
        playerData.forEach((player, data) -> {
            data.SaveInv();
        });
    }

    private void setPlayersToInv0() {
        playerData.forEach((player, data) -> {
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
