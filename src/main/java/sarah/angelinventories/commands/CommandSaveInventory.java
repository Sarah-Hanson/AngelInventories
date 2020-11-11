package sarah.angelinventories.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class CommandSaveInventory implements CommandExecutor {
    HashMap<String, Inventory> staticInventories;

    public CommandSaveInventory(HashMap<String, Inventory> staticInventories) {
        this.staticInventories = staticInventories;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && args.length == 1) {
            Player player = (Player) sender;

            staticInventories.put(args[0], player.getInventory());

            return true;
        }
        return false;
    }
}
