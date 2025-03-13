package edu.uob;

import java.io.IOException;
import java.util.*;

public class CmdSelect extends DBCmd {
    private final String tableName;
    private final List<String> selectedColumns;
    private QueryNode conditionRoot;
    private final ReadWrite readWrite;

    public CmdSelect(DBServer server, QueryParser parser) throws IOException {
        super(server);
        parser.parseSelect();
        this.tableName = parser.getTableName();
        this.selectedColumns = parser.getColumnNames();
        this.readWrite = new ReadWrite();
        this.readWrite.setCurrentDatabase(server.getDatabaseName());

        if (parser.hasWhereClause()) {
            ConditionParser conditionParser = new ConditionParser(parser.getConditionTokens());
            conditionRoot = conditionParser.parseConditions();
        }
    }

    @Override
    public String query(DBServer server) {
        try {
            List<String> tableContent = readWrite.readTableFromFile(tableName);
            if (tableContent.isEmpty()) {
                return "[ERROR] Table '" + tableName + "' is empty.";
            }

            String[] headers = tableContent.get(0).split("\t");

            List<Integer> selectedColumnIndexes = getColumnIndexes(headers);

            StringBuilder result = new StringBuilder("[OK]\n");
            result.append(formatHeaders(headers, selectedColumnIndexes)).append("\n");

            for (int i = 1; i < tableContent.size(); i++) {
                String[] rowValues = tableContent.get(i).split("\t");

                if (conditionRoot == null || conditionRoot.evaluate(rowValues, headers)) {
                    result.append(formatRow(rowValues, selectedColumnIndexes)).append("\n");
                }
            }

            return result.toString().trim();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private List<Integer> getColumnIndexes(String[] headers) throws IOException {
        List<Integer> indexes = new ArrayList<>();

        if (selectedColumns.contains("*")) {
            for (int i = 0; i < headers.length; i++) {
                indexes.add(i);
            }
        } else {
            for (String column : selectedColumns) {
                int index = findColumnIndex(headers, column);

                if (index == -1) {
                    throw new IOException("[ERROR] Column '" + column + "' does not exist in table.");
                }

                indexes.add(index);
            }
        }
        return indexes;
    }

    private int findColumnIndex(String[] headers, String columnName) throws IOException {
        if (columnName == null || columnName.isEmpty()) {
            throw new IOException("[ERROR] Attempted to find a null or empty column.");
        }

        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(columnName)) {
                return i;
            }
        }

        throw new IOException("[ERROR] Column '" + columnName + "' not found in table headers.");
    }

    private String formatHeaders(String[] headers, List<Integer> selectedIndexes) {
        List<String> selectedHeaderValues = new ArrayList<>();
        for (int index : selectedIndexes) {
            selectedHeaderValues.add(headers[index]);
        }
        return String.join("\t", selectedHeaderValues);
    }

    private String formatRow(String[] rowValues, List<Integer> selectedIndexes) {
        List<String> selectedValues = new ArrayList<>();
        for (int index : selectedIndexes) {
            selectedValues.add(rowValues[index]);
        }
        return String.join("\t", selectedValues);
    }
}
