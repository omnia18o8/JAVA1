package edu.uob;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReadWrite {
    private final String storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
    private static String currentDatabase = null;

    public ReadWrite() {
    }

    public void setCurrentDatabase(String dbName) {
        currentDatabase = dbName;
    }

    private String getTablePath(String tableName) {
        if (currentDatabase == null) {
            throw new IllegalStateException("[ERROR] No database selected.");
        }
        return storageFolderPath + File.separator + currentDatabase + File.separator + tableName;
    }

    public String writeTableToDB(String tableName, List<String> data) {
        String tablePath = getTablePath(tableName);
        File tableFile = new File(tablePath);

        try {
            if (!tableFile.exists()) {
                return "[ERROR] Table '" + tableName + "' does not exist.";
            }
            FileWriter writer = new FileWriter(tableFile, false);
            for (String line : data) {
                writer.write(line + "\n");
            }
            writer.close();
            return "[OK]";
        } catch (IOException e) {
            return "[ERROR] Could not update table.";
        }
    }

    public List<String> readTableFromFile(String tableName) {
        String tablePath = getTablePath(tableName);

        try {
            return Files.readAllLines(Paths.get(tablePath));
        } catch (IOException e) {
            return List.of();
        }
    }
}
