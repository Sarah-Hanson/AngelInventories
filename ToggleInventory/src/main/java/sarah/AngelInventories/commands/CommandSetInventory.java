package sarah.AngelInventories.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sarah.AngelInventories.invManagement.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandSetInventory  implements CommandExecutor {
    HashMap<String, Inventory> staticInventories;

    public CommandSetInventory(HashMap<String,Inventory> staticInventories){
        this.staticInventories = staticInventories;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 2) {
            Player target = Bukkit.getPlayer(args[0]);
            Inventory inv = staticInventories.get(args[1]);

            target.getInventory().setContents(inv.getContents());
        }
        return true;
    }
}
