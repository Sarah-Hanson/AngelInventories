package sarah.angelinventories;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import sarah.angelinventories.commands.*;
import sarah.angelinventories.invManagement.MySQL;

import java.util.HashMap;

public final class AngelInventories extends JavaPlugin {
    public PlayerRoster playerRoster;
    public HashMap<String, Inventory> staticInventories;
    public MySQL mysql;
    public FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        playerRoster = new PlayerRoster();

        // Config Stuff
        config.addDefault("debugging", false);
        config.addDefault("mysql.host", "127.0.0.1");
        config.addDefault("mysql.database", "AngelInventories");
        config.addDefault("mysql.username", "username");
        config.addDefault("mysql.password", "password");
        config.addDefault("mysql.port", "password");
        config.options().copyDefaults(true);
        saveConfig();

        // Pull data from db to memory
        staticInventories = mysql.getCustomInventories();

        // Register Commands
        getCommand("ti").setExecutor(new CommandTI(playerRoster));
        getCommand("recoverInventory").setExecutor(new CommandRecoverInventory(playerRoster));
        getCommand("saveCustomInventory").setExecutor(new CommandSaveInventory(staticInventories));
        getCommand("equipCustomInventory").setExecutor(new CommandSetInventory(staticInventories));
        getCommand("removeCustomInventory").setExecutor((new CommandRemoveInventory(staticInventories)));

        // Listener hooks
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // Database
        mysql = new MySQL(this);
        mysql.load();
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

        mysql.setPlayerInventories(playerData);
        mysql.setCustomInventories(staticInventories);
    }

}
