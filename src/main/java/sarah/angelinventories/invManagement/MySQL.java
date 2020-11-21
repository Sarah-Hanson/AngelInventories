package sarah.angelinventories.invManagement;

import org.bukkit.inventory.Inventory;
import sarah.angelinventories.AngelInventories;

import java.io.IOException;
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
    private boolean debugging;

    public MySQL(AngelInventories plugin) {
        super(plugin);
        this.plugin = plugin;
        host = plugin.getConfig().getString("mysql.host");
        database = plugin.getConfig().getString("mysql.database");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
        port = plugin.getConfig().getInt("mysql.port");
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

    public PlayerData getPlayerData(UUID uuid) throws SQLException, IOException {
        Connection connection = getSQLConnection();
        CallableStatement cs = connection.prepareCall("{call fn_load_player (?, ?)}");
        cs.setString("uuid", uuid.toString());
        ResultSet rs = cs.executeQuery();
        ArrayList<Inventory> inventories = new ArrayList<>();
        Integer currentPlayerInvIndex;
        String currentCustomInvName;
        while (rs.next()) {
            if (currentCustomInvName.isEmpty()) {
                currentCustomInvName = rs.getString("current_custom_inv");
            }
            if (currentPlayerInvIndex == null) {
                currentPlayerInvIndex = rs.getInt("current_player_inv");
            }
            inventories.add(InventorySerializer.fromBase64(rs.getString("inventory")));
        }

        PlayerData playerData = new PlayerData(plugin, uuid, inventories, currentPlayerInvIndex, currentCustomInvName)
    }

    public void setInventory(String name, Inventory inventory) {
    }
}
