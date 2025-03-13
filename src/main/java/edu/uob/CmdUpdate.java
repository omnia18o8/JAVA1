package edu.uob;

import java.util.*;

public class CmdUpdate extends DBCmd {
    private String tableName;
    private List<String> columnNames;
    private List<String> values;
    private QueryNode conditionRoot;
    private ReadWrite readWrite;

    public CmdUpdate(DBServer server, QueryParser parser) throws Exception {
        super(server);
        parser.parseUpdate();

        this.tableName = parser.getTableName();
        this.columnNames = parser.getColumnNames();
        this.values = parser.getValues();
        this.readWrite = new ReadWrite();
        this.readWrite.setCurrentDatabase(server.getDatabaseName());

        if (parser.hasWhereClause()) {
            ConditionParser conditionParser = new ConditionParser(parser.getConditionTokens());
            conditionRoot = conditionParser.parseConditions();
        }
    }

    @Override
    public String query(DBServer server) {
        List<String> tableContent = readWrite.readTableFromFile(tableName);
        if (tableContent.isEmpty()) {
            return "[ERROR] Table '" + tableName + "' is empty.";
        }

        String[] headers = tableContent.get(0).split("\t");
        List<Integer> columnIndexes = getColumnIndexes(headers);

        if (columnIndexes == null) {
            return "[ERROR] One or more columns do not exist.";
        }

        List<String> updatedTable = new ArrayList<>();
        updatedTable.add(String.join("\t", headers));


        for (int i = 1; i < tableContent.size(); i++) {
            String[] rowValues = tableContent.get(i).split("\t");

            boolean shouldUpdate = (conditionRoot == null || conditionRoot.evaluate(rowValues, headers));
            List<String> updatedRow = new ArrayList<>(Arrays.asList(rowValues));

            if (shouldUpdate) {
                for (int j = 0; j < columnIndexes.size(); j++) {
                    updatedRow.set(columnIndexes.get(j), values.get(j));
                }
            }

            updatedTable.add(String.join("\t", updatedRow));
        }

        return readWrite.writeTableToDB(tableName, updatedTable);
    }

    private List<Integer> getColumnIndexes(String[] headers) {
        List<Integer> indexes = new ArrayList<>();

        for (String column : columnNames) {
            int index = findColumnIndex(headers, column);
            if (index == -1) {
                System.err.println("[ERROR] Column '" + column + "' does not exist in table.");
                return null;
            }
            indexes.add(index);
        }
        return indexes;
    }

    private int findColumnIndex(String[] headers, String columnName) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }
        return -1;
    }
}
