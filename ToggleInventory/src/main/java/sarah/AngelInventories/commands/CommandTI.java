package sarah.AngelInventories.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sarah.AngelInventories.invManagement.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandTI implements CommandExecutor {
    HashMap<Player,PlayerData> playerData;

    public CommandTI(HashMap<Player,PlayerData> playerData){
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData senderData = null;
            boolean playerExists = false;

            if(playerData.containsKey(player)){
                senderData = playerData.get(player);
            }
            else {
                ArrayList<Inventory> inv = new ArrayList<Inventory>();
                inv.add(player.getInventory());

                senderData = new PlayerData(inv, player);

                playerData.put(player, senderData);
            }
            if(senderData != null) {
                // Just /TI command
                if (args.length == 0) {
                    senderData.ToggleInv();
                }
                else if (args.length == 1) {
                    int invNum;
                    try {
                        invNum = Integer.parseInt(args[0]);
                    }
                    catch (NumberFormatException e) {
                        return false;
                    }
                    senderData.SetInv(invNum);
                }
            }
        }

        // If the player (or console) uses our command correct, we can return true
        return true;
    }
}
