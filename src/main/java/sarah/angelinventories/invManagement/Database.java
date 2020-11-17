package sarah.angelinventories.invManagement;

import sarah.angelinventories.AngelInventories;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {
    AngelInventories plugin;
    Connection connection;
    String host;
    String database;
    String username;
    String password;
    int port;

    public Database(AngelInventories plugin) {
        this.plugin = plugin;
        host = plugin.getConfig().getString("mysql.host");
        database = plugin.getConfig().getString("mysql.database");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
        port = plugin.getConfig().getInt("mysql.port");
    }

    public void initialize() {
        connection = getSQLConnection();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM `player_inventories` LIMIT 1;");
            ResultSet rs = ps.executeQuery();
            close(ps, rs);
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    protected abstract Connection getSQLConnection();

    public void loadPlayer(PlayerData playerData) {
        UUID uuid = playerData.player.getUniqueId();
        PreparedStatement ps;
        try {
            String query =
                    "SELECT *" +
                            "FROM player_data" +
                            "WHERE uuid = ?" +
                            "INNER JOIN player_inventories" +
                            "ON player_data.uuid = player_inventories.uuid" +
                            "ORDER BY player_inventories.index ASC;";
            ps = connection.prepareStatement(query);
            ps.setObject(1, uuid);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                playerData.inventories.add(InventorySerializer.fromBase64(rs.getString("inventory")));
                if (playerData.currentPlayerInvIndex == null) {
                    playerData.currentPlayerInvIndex = rs.getInt("current_player_inv");
                }
                if (playerData.currentCustomInvName == null) {
                    playerData.currentCustomInvName = rs.getString("current_custom_inv");
                }
            }
        } catch (SQLException | IOException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
        }
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }
}
