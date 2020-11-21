package sarah.angelinventories.invManagement;

import sarah.angelinventories.AngelInventories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
