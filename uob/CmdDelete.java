package edu.uob;

import java.util.*;

public class CmdDelete extends DBCmd {
    private String tableName;
    private QueryNode conditionRoot;
    private ReadWrite readWrite;

    public CmdDelete(DBServer server, QueryParser parser) throws Exception {
        super(server);
        parser.parseDelete();

        this.tableName = parser.getTableName();
        this.readWrite = new ReadWrite();
        readWrite.setCurrentDatabase(server.getDatabaseName());

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
        List<String> updatedTable = new ArrayList<>();
        updatedTable.add(String.join("\t", headers)); // ✅ Keep headers

        // ✅ Iterate over rows and delete those matching the condition
        for (int i = 1; i < tableContent.size(); i++) {
            String[] rowValues = tableContent.get(i).split("\t");

            boolean shouldDelete = (conditionRoot != null && conditionRoot.evaluate(rowValues, headers));

            if (!shouldDelete) { // ✅ Keep only rows that do not match the condition
                updatedTable.add(String.join("\t", rowValues));
            }
        }

        return readWrite.writeTableToDB(tableName, updatedTable);
    }
}
