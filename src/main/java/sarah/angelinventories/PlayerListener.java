package sarah.angelinventories;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {
    AngelInventories plugin;

    public PlayerListener(AngelInventories plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin() {
        plugin.playerRoster()
    }
}
