package com.timedfly.managers;

import com.timedfly.utilities.SqlProcessor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLManager {

    private String table = "timedfly_fly_time";
    private final SqlProcessor sqlProcessor;

    public MySQLManager(SqlProcessor sqlProcessor) {
        this.sqlProcessor = sqlProcessor;
    }

    private boolean playerExists(UUID uuid) {
        Connection connection = this.sqlProcessor.getConnection();

        try {
            if (connection == null || connection.isClosed()) return false;

            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `" + table + "` WHERE UUID=?;");
            statement.setString(1, uuid.toString());
            ResultSet results = statement.executeQuery();
            boolean containsPlayer = results.next();
            statement.close();
            results.close();
            return containsPlayer;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (playerExists(uuid)) return;
        this.sqlProcessor.queueAsync(connection -> {
            try {
                if (connection == null || connection.isClosed()) return;

                PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO `" + table + "` (UUID,NAME,TIMELEFT,INITIALTIME,TimeStopped) VALUES (?,?,?,?,?);");
                insert.setString(1, uuid.toString());
                insert.setString(2, player.getName());
                insert.setInt(3, 0);
                insert.setInt(4, 0);
                insert.setBoolean(5, false);
                insert.execute();
                insert.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void saveData(Player player, int timeleft, int initial, boolean manuallyStopped) {
        String uuid = player.getUniqueId().toString();
        this.sqlProcessor.queueAsync(connection -> {
            try {
                if (connection == null || connection.isClosed()) return;

                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE `" + table + "` SET TIMELEFT = ?, INITIALTIME = ?, TimeStopped = ? WHERE UUID = ?;");

                statement.setInt(1, timeleft);
                statement.setInt(2, initial);
                statement.setBoolean(3, manuallyStopped);
                statement.setString(4, uuid);

                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public int getTimeLeft(Player player) {
        String uuid = player.getUniqueId().toString();
        Connection connection = this.sqlProcessor.getConnection();

        try {
            if (connection == null || connection.isClosed()) return 0;

            PreparedStatement statement = connection.prepareStatement("SELECT TIMELEFT FROM `" + table + "` WHERE UUID = ?;");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int timeleft = resultSet.getInt(1);
            resultSet.close();
            return timeleft;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getInitialTime(Player player) {
        String uuid = player.getUniqueId().toString();
        Connection connection = this.sqlProcessor.getConnection();

        try {
            if (connection == null || connection.isClosed()) return 0;

            PreparedStatement statement = connection.prepareStatement("SELECT INITIALTIME FROM `" + table + "` WHERE UUID = ?;");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            int initial = resultSet.getInt(1);
            resultSet.close();
            return initial;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean getManuallyStopped(Player player) {
        String uuid = player.getUniqueId().toString();
        Connection connection = this.sqlProcessor.getConnection();

        try {
            if (connection == null || connection.isClosed()) return false;

            PreparedStatement statement = connection.prepareStatement("SELECT TimeStopped FROM `" + table + "` WHERE UUID = ?;");
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            boolean initial = resultSet.getBoolean(1);
            resultSet.close();
            return initial;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
