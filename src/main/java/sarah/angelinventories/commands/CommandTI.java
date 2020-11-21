package sarah.angelinventories.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sarah.angelinventories.AngelInventories;
import sarah.angelinventories.invManagement.PlayerData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class CommandTI implements CommandExecutor {
    HashMap<UUID, PlayerData> playerData;
    AngelInventories plugin;

    public CommandTI(AngelInventories plugin, HashMap<UUID, PlayerData> playerData) {
        this.plugin = plugin;
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            UUID uuid = player.getUniqueId();
            PlayerData playerData;

            if (this.playerData.containsKey(uuid)) {
                playerData = this.playerData.get(uuid);
            } else {
                try {
                    playerData = new PlayerData(plugin, uuid);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return false;
                }
            }
            if (playerData != null) {
                // Just /TI command
                if (args.length == 0) {
                    //tODO: Handle unchanged index
                    playerData.ToggleInv();
                    sender.sendMessage("Inventory set to: " + (playerData.currentPlayerInvIndex + 1));
                    return true;
                } else if (args.length == 1) {
                    int invNum;
                    try {
                        invNum = Integer.parseInt(args[0]) - 1;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Argument must be an inventory index.");
                        return false;
                    }
                    playerData.SetInv(invNum);
                    sender.sendMessage("Inventory set to: " + (invNum + 1));
                    return true;
                }
            }
        }
        return false;
    }
}
