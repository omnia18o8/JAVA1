package edu.uob;

import java.util.ArrayList;
import java.util.List;

public class CmdAlter extends DBCmd {
    private final String tableName;
    private final String alterType;
    private final String columnName;
    private final ReadWrite readWrite;

    public CmdAlter(DBServer server, QueryParser parser) throws Exception {
        super(server);
        if (server.getDatabaseName() == null) {
            throw new Exception("[ERROR] Database name is null!");
        }

        parser.parseAlter();
        this.tableName = parser.getTableName();
        this.alterType = parser.getAlterType();
        this.columnName = parser.getColumnNames().get(0);
        this.readWrite = new ReadWrite();
        readWrite.setCurrentDatabase(server.getDatabaseName());
    }

    @Override
    public String query(DBServer server) {
        List<String> tableContent = readWrite.readTableFromFile(tableName);
        if (tableContent.isEmpty()) {
            return "[ERROR] Table '" + tableName + "' does not exist.";
        }

        String[] headers = tableContent.get(0).split("\t");
        List<String> updatedHeaders = new ArrayList<>(List.of(headers));

        if (columnName.equalsIgnoreCase("id")) {
            return "[ERROR] Cannot alter key column 'id'.";
        }

        if (alterType.equalsIgnoreCase("ADD")) {
            if (updatedHeaders.contains(columnName)) {
                return "[ERROR] Column '" + columnName + "' already exists in table.";
            }
            updatedHeaders.add(columnName);

        } else if (alterType.equalsIgnoreCase("DROP")) {
            if (!updatedHeaders.contains(columnName)) {
                return "[ERROR] Column '" + columnName + "' does not exist in table.";
            }
            updatedHeaders.remove(columnName);
        }

        List<String> updatedTable = new ArrayList<>();
        updatedTable.add(String.join("\t", updatedHeaders));
        int newColumnCount = updatedHeaders.size();

        for (int i = 1; i < tableContent.size(); i++) {
            String[] rowValues = tableContent.get(i).split("\t");
            List<String> newRow = new ArrayList<>(List.of(rowValues));

            if (alterType.equalsIgnoreCase("ADD")) {
                newRow.add("NULL");

            } else if (alterType.equalsIgnoreCase("DROP")) {
                int columnIndex = List.of(headers).indexOf(columnName);
                newRow.remove(columnIndex);
            }

            while (newRow.size() < newColumnCount) {
                newRow.add("NULL");
            }
            updatedTable.add(String.join("\t", newRow));
        }
        return readWrite.writeTableToDB(tableName, updatedTable);
    }
}
