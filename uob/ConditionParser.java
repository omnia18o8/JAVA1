package edu.uob;

import java.util.*;

public class ConditionParser {
    private List<String> tokens;
    private int index;

    public ConditionParser(List<String> tokens) {
        this.tokens = tokens;
        this.index = 0;
    }

    public QueryNode parseConditions() {
        return parseOrExpression(); // Start parsing with OR expressions
    }

    // Handle OR expressions first (lowest precedence)
    private QueryNode parseOrExpression() {
        QueryNode left = parseAndExpression();

        while (index < tokens.size() && tokens.get(index).equalsIgnoreCase("OR")) {
            index++;
            QueryNode right = parseAndExpression();
            left = new LogicalNode("OR", left, right);
        }

        return left;
    }

    // Handle AND expressions (higher precedence)
    private QueryNode parseAndExpression() {
        QueryNode left = parseCondition();

        while (index < tokens.size() && tokens.get(index).equalsIgnoreCase("AND")) {
            index++;
            QueryNode right = parseCondition();
            left = new LogicalNode("AND", left, right);
        }

        return left;
    }

    // Parse individual conditions or nested conditions in parentheses
    private QueryNode parseCondition() {
        if (tokens.get(index).equals("(")) {
            index++; // Skip '('
            QueryNode condition = parseOrExpression(); // Recursively parse nested conditions
            index++; // Skip ')'
            return condition;
        }

        // Extract column, operator, and value
        String column = tokens.get(index++);
        String operator = tokens.get(index++);
        String value = tokens.get(index++);

        return new ConditionNode(column, operator, value);
    }
}
