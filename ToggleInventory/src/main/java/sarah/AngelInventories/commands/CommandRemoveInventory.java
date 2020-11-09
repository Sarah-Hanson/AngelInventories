package sarah.AngelInventories.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;

public class CommandRemoveInventory implements CommandExecutor {

    HashMap<String, Inventory> staticInventories;

    public CommandRemoveInventory(HashMap<String,Inventory> staticInventories){
        this.staticInventories = staticInventories;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 ) {
            if (staticInventories.containsKey(args[0])) {
                staticInventories.remove(args[0]);
            }
            else {
                sender.sendMessage("That isn't a valid inventory");
            }
        }
        return false;
    }
}
