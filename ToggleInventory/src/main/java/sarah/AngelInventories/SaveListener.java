package sarah.AngelInventories;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class SaveListener implements Listener {
    AngelInventories instance;

    public SaveListener(AngelInventories instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
        instance.save();
    }
}
