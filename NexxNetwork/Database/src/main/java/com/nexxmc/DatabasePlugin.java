package com.nexxmc;

import com.nexxmc.database.BaseDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabasePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        BaseDatabase.getInstance().init(null);

        try (Connection connection = BaseDatabase.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1;")) {
                statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("[NexxDatabase] Connection complete.");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BaseDatabase.getInstance().close();
    }
}
