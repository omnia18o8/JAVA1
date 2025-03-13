package edu.uob;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CmdCreateDatabase extends DBCmd {
    public CmdCreateDatabase(DBServer server, String databaseName) {
        super(server);
        this.databaseName = databaseName;
    }

    @Override
    public String query(DBServer server) {
        String dbPath = server.getStorageFolderPath() + File.separator + databaseName;
        try {
            Files.createDirectories(Paths.get(dbPath));
            return "[OK]";
        } catch (Exception e) {
            return "[Error]: Could not create database.";
        }
    }
}
