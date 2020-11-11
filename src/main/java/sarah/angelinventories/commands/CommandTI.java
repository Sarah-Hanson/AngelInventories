package sarah.angelinventories.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sarah.angelinventories.invManagement.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandTI implements CommandExecutor {
    HashMap<Player, PlayerData> playerData;

    public CommandTI(HashMap<Player, PlayerData> playerData) {
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            PlayerData senderData;

            if (playerData.containsKey(player)) {
                senderData = playerData.get(player);
            } else {
                ArrayList<Inventory> inv = new ArrayList<>();
                inv.add(player.getInventory());

                senderData = new PlayerData(inv, player);

                playerData.put(player, senderData);
            }
            if (senderData != null) {
                // Just /TI command
                if (args.length == 0) {
                    sender.sendMessage("Inventory set to: " + senderData.ToggleInv()+1);
                    return true;
                } else if (args.length == 1) {
                    int invNum;
                    try {
                        invNum = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        return false;
                    }
                    senderData.SetInv(invNum);
                    sender.sendMessage("Inventory set to: " + invNum+1);
                    return true;
                }
            }
        }
        return false;
    }
}
