package edu.uob;

public class ConditionNode extends QueryNode {
    String column;
    String operator;
    String value;

    public ConditionNode(String column, String operator, String value) {
        this.column = column;
        this.operator = operator;
        this.value = value.startsWith("'") && value.endsWith("'") ? value.substring(1, value.length() - 1) : value;
    }

    @Override
    public boolean evaluate(String[] row, String[] headers) {
        int columnIndex = findColumnIndex(headers, column);
        if (columnIndex == -1) return false;

        String rowValue = row[columnIndex].trim();
        boolean result = compare(rowValue, operator, value);

        return result;
    }

    private boolean compare(String rowValue, String operator, String value) {
        boolean isNumeric = rowValue.matches("-?\\d+(\\.\\d+)?") && value.matches("-?\\d+(\\.\\d+)?");

        if (isNumeric) {
            double rowNum = Double.parseDouble(rowValue);
            double condNum = Double.parseDouble(value);

            return switch (operator) {
                case "==" -> rowNum == condNum;
                case "!=" -> rowNum != condNum;
                case ">" -> rowNum > condNum;
                case "<" -> rowNum < condNum;
                case ">=" -> rowNum >= condNum;
                case "<=" -> rowNum <= condNum;
                default -> false;
            };
        } else {
            return switch (operator) {
                case "==" -> rowValue.equalsIgnoreCase(value);
                case "!=" -> !rowValue.equalsIgnoreCase(value);
                case "LIKE" -> rowValue.toLowerCase().contains(value.toLowerCase());
                default -> false;
            };
        }
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
