package sarah.angelinventories.invManagement;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import sarah.angelinventories.AngelInventories;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class MySQL extends Database {
    private final AngelInventories plugin;
    private Connection connection;
    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;

    public MySQL(AngelInventories plugin) {
        super(plugin);
        this.plugin = plugin;
        host = plugin.getConfig().getString("mysql.host");
        database = plugin.getConfig().getString("mysql.database");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
        port = plugin.getConfig().getInt("mysql.port");
    }

    @Override
    public Connection getSQLConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "MySQL Exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the MySQL JDBC library, good luck.", ex);
        }
        return null;
    }

    public HashMap<Player, PlayerData> getPlayerInventories() {

        ResultSet rs = null;
        PreparedStatement pst;
        HashMap<Player, PlayerData> playerInventories = new HashMap<>();

        try {
            pst = connection.prepareStatement("SELECT * FROM player_inventories;");
            rs = pst.executeQuery();
            while (rs.next()) {
                //Pull row
                String uuid = rs.getString("player_uuid");
                String invString = rs.getString("inventory_string");
                Player player = Bukkit.getPlayer(UUID.fromString(uuid));

                //See if that uuid is already in dataset, if so add it to their inventories, otherwise add a new player
                if (playerInventories.containsKey(player)) {
                    PlayerData data = playerInventories.get(player);
                    data.inventories.add(InventorySerializer.fromBase64(invString));
                } else {
                    ArrayList<Inventory> inventory = new ArrayList<>();
                    inventory.add(InventorySerializer.fromBase64(invString));
                    assert player != null;
                    playerInventories.put(player, new PlayerData(inventory, player));
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
        return playerInventories;
    }

    public HashMap<String, Inventory> getCustomInventories() {
        ResultSet rs = null;
        PreparedStatement ps;
        HashMap<String, Inventory> invs = new HashMap<String, Inventory>() {
            private static final long serialVersionUID = -7474154515428322552L;
        };

        try {
            ps = connection.prepareStatement("SELECT * FROM static_inventories;");
            rs = ps.executeQuery();
            while (rs.next()) {
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
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to get custom inventories.");
            }

        }
        return invs;
    }

    public void setPlayerInventories(HashMap<Player, PlayerData> players) {
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
                } catch (SQLException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to set player inventories.");
                }
            }
        });

    }

    public void setCustomInventories(HashMap<String, Inventory> invs) {
        invs.forEach((invName, inv) -> {
            String query = "REPLACE into player_inventories (inv_name, inventory_string) values(?, ?)";
            try {
                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, invName);
                preparedStmt.setString(2, InventorySerializer.toBase64(inv));
                preparedStmt.execute();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "Failed to set custom inventories.");
            }
        });

    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement statement = connection.createStatement();
            // Stores player inventories using composite key of UUID + Inventory Index
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS `player_inventories` (" +
                            "`uuid` UUID NOT NULL, " +
                            "`index` UUID NOT NULL, " +
                            "`inventory` TEXT NOT NULL, " +
                            "PRIMARY KEY (`uuid`, `id`));"
            );

            // Stores custom inventories saved by server owners to be applied to other players in special situations
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS `custom_inventories` (" +
                            "`name` STRING NOT NULL, " +
                            "`inventory` TEXT NOT NULL, " +
                            // If the inventory can be toggled out of using /ti
                            "`locked_inventory` INTEGER NOT NULL, " +
                            // slots the player can't modify, required locked_inventory to be true
                            "`locked_slots` TEXT, " +
                            // enumerator decides if the player can /ti away from this custom inv or if the inventory overwrites the current player inventory (doesn't set current_inv).
                            "`setting` INTEGER NOT NULL, " +
                            "PRIMARY KEY (`name`));"
            );
            statement.execute(
                    "CREATE TABLE IF NOT EXISTS `player_data` (" +
                            "`uuid` UUID NOT NULL, " +
                            // This stores the current player inv index.
                            "`current_player_inv` INTEGER  NOT NULL DEFAULT 0, " +
                            // This stores the current custom inv name if they have a custom inv equipped.
                            "`current_custom_inv` STRING, " +
                            "PRIMARY KEY (`uuid`));"
            );
            statement.execute(
                    "DELIMITER $$" +
                            "CREATE PROCEDURE fn_load_player(" +
                            "IN uuid UUID," +
                            "OUT inventory TEXT)" +
                            "BEGIN" +
                            "SELECT `current_custom_inv`, `current_player_inv`" +
                            "FROM player_data;" +
                            "WHERE player_data.uuid = uuid" +
                            "IF current_custom_inv IS NOT NULL THEN" +
                            "set inventory = custom_inventories.inventory" +
                            "WHERE custom_inventories.uuid = uuid" +
                            "ELSE" +
                            "set inventory = player_inventories.inventory" +
                            "WHERE player_inventories.uuid = uuid" +
                            "END IF;" +
                            "END $$" +
                            "DELIMITER ;"
            );
            statement.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "MySQL tables failed to create, check MySQL.java 175-202", e);
        }
        initialize();
    }
}
