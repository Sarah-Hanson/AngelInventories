package sarah.angelinventories;

import org.bukkit.entity.Player;
import sarah.angelinventories.invManagement.PlayerData;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRoster {
    HashMap<UUID, PlayerData> players;

    public void Add(UUID uuid) {
        players.put(uuid, new PlayerData())
    }

    public void Add(Player player) {
        UUID uuid = player.getUniqueId();
        players.put(uuid, new PlayerData());
    }

    public void Remove(UUID uuid) {
        players.remove(uuid);
    }

    public void Remove(Player player) {
        UUID uuid = player.getUniqueId();
        players.remove(uuid);
    }
}
