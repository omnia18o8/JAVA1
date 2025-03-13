package edu.uob;

import java.io.File;

public class CmdDrop extends DBCmd {
    private final String objectType;
    private final String objectName;

    public CmdDrop(DBServer server, QueryParser parser) throws Exception {
        super(server);
        parser.parseDrop();

        if (parser.isDropTable()) {
            if (server.getDatabaseName() == null) {
                throw new Exception("[ERROR] No database selected. Use a database first.");
            }
            this.objectType = "TABLE";
            this.objectName = parser.getTableName();
        } else {
            this.objectType = "DATABASE";
            this.objectName = parser.getDatabaseName();
        }

        ReadWrite readWrite = new ReadWrite();
        readWrite.setCurrentDatabase(server.getDatabaseName());
    }

    @Override
    public String query(DBServer server) {
        if (objectType.equals("TABLE")) {
            return dropTable(server);
        } else {
            return dropDatabase(server);
        }
    }

    private String dropTable(DBServer server) {
        String tablePath = server.getStorageFolderPath() + File.separator+ server.getDatabaseName() + "/" + objectName;
        File tableFile = new File(tablePath);

        if (tableFile.exists()) {
            if (tableFile.delete()) {
                server.removeTable(objectName);
                return "[OK]";
            } else {
                return "[ERROR] Could not delete table '" + objectName + "'.";
            }
        }
        return "[ERROR] Table '" + objectName + "' does not exist.";
    }

    private String dropDatabase(DBServer server) {
        String dbPath = server.getStorageFolderPath() + File.separator + objectName;
        File dbFolder = new File(dbPath);

        File[] files = dbFolder.listFiles();
        boolean allFilesDeleted = true;

        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    allFilesDeleted = false;
                }
            }
        }

        if (allFilesDeleted && dbFolder.delete()) {
            if (server.getDatabaseName().equals(objectName)) {
                server.setDatabaseName(null);
            }
            return "[OK]";
        }
        return "[ERROR] Could not delete some files in database '" + objectName + "'.";
    }

}
