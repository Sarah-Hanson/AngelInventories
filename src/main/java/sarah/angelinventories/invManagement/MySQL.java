package sarah.angelinventories.invManagement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sarah.angelinventories.AngelInventories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MySQL {
    private AngelInventories plugin;
    private Connection connection;
    private String host, database, username, password;
    private int port = 3306;

    public MySQL(AngelInventories plugin) {
        this.plugin = plugin;
        host = plugin.getConfig().getString("mysql.host");
        database = plugin.getConfig().getString("mysql.database");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
    }

    private void openConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            synchronized (this) {
                if (connection != null && !connection.isClosed()) {
                    return;
                }
                Class.forName("com.mysql.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            }
        } catch (Exception e) {
            //TODO: Exception handling
        }
    }

    public HashMap<Player, PlayerData> getPlayerInventories() {
        openConnection();

        ResultSet rs = null;
        PreparedStatement pst;
        HashMap<Player, PlayerData> invs = new HashMap<>();

        try {
            pst = connection.prepareStatement("SELECT * FROM player_inventories;");
            rs = pst.executeQuery();
            while (rs.next()) {
                //Pull row
                String uuid = rs.getString("player_uuid");
                String invString = rs.getString("inventory_string");
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));

                //See if that uuid is already in dataset, if so add it to their inventories, otherwise add a new player
                if (invs.containsKey(player)) {
                    PlayerData data = invs.get(player);
                    data.inventories.add(InventorySerializer.fromBase64(invString));
                } else {
                    ArrayList<Inventory> inventory = new ArrayList<>();
                    inventory.add(InventorySerializer.fromBase64(invString));
                    invs.put(player, new PlayerData(inventory, player));
                }
            }
        } catch (Exception e) {
            //TODO: Log Errors
        } finally {
            try {
                connection.close();
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                //TODO: Iunno what to do if closing fails
            }

        }
        return invs;
    }

    public HashMap<String, Inventory> getStaticInventories() {
        openConnection();

        ResultSet rs = null;
        PreparedStatement pst;
        HashMap<String, Inventory> invs = new HashMap<String, Inventory>() {
            private static final long serialVersionUID = -7474154515428322552L;
        };

        try {
            pst = connection.prepareStatement("SELECT * FROM static_inventories;");
            rs = pst.executeQuery();
            while (rs.next()) {
                //Pull row
                String invName = rs.getString("inventory_name");
                String invString = rs.getString("inventory_string");

                invs.put(invName, InventorySerializer.fromBase64(invString));
            }
        } catch (Exception e) {
            //TODO: Log Errors
        } finally {
            try {
                connection.close();
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                //TODO: Iunno what to do if closing fails
            }

        }
        return invs;
    }

    public void setInventories(HashMap<Player, PlayerData> players) {
        openConnection();

        players.forEach((player, data) -> {
            String player_uuid = player.getUniqueId().toString();
            for (Inventory inv : data.inventories) {
                int inventory_num = data.inventories.indexOf(inv);

                try {
                    String query = "REPLACE into player_inventories (player_uuid, inventory_num, inventory_string) values(?, ?, ?)";

                    PreparedStatement preparedStmt = connection.prepareStatement(query);
                    preparedStmt.setString(1, player_uuid);
                    preparedStmt.setInt(2, inventory_num);
                    preparedStmt.setString(3, InventorySerializer.toBase64(inv));

                    preparedStmt.execute();
                } catch (Exception e) {
                    //TODO: Exception Handling
                }
            }
            try {
                connection.close();
            } catch (Exception e) {
                //TODO: Iunno what to do if closing fails
            }
        });

    }

    public void setStaticInventories(HashMap<String, Inventory> invs) {
        openConnection();

        invs.forEach((invName, inv) -> {
            try {
                String query = "REPLACE into player_inventories (inv_name, inventory_string) values(?, ?)";

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, invName);
                preparedStmt.setString(2, InventorySerializer.toBase64(inv));

                preparedStmt.execute();
            } catch (Exception e) {
            }
        });

    }

}
