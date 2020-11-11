package sarah.angelinventories.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sarah.angelinventories.invManagement.PlayerData;

import java.util.HashMap;

public class CommandRecoverInventory implements CommandExecutor {
    HashMap<Player, PlayerData> playerData;

    public CommandRecoverInventory(HashMap<Player, PlayerData> playerData) {
        this.playerData = playerData;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player && args.length == 0) {
            Player player = (Player) sender;
            PlayerData senderData = null;
            boolean playerExists = false;

            if (playerData.containsKey(player)) {
                PlayerData data = playerData.get(player);
                if (!data.permCheckPlayer()) {
                    player.sendMessage("You do not have excess any inventories to recover!");
                }
                else {
                    player.sendMessage("Inventories dumped!");
                }
                return true;
            }
        }
        return false;
    }

}
