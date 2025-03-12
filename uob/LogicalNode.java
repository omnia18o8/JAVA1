package edu.uob;

public class LogicalNode extends QueryNode {
    String logicalOperator;
    QueryNode left;
    QueryNode right;

    public LogicalNode(String logicalOperator, QueryNode left, QueryNode right) {
        this.logicalOperator = logicalOperator;
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean evaluate(String[] row, String[] headers) {
        boolean leftResult = left.evaluate(row, headers);
        boolean rightResult = right.evaluate(row, headers);

        // Debugging output
        System.out.println("[DEBUG] Evaluating: (" + leftResult + " " + logicalOperator + " " + rightResult + ")");

        if (logicalOperator.equalsIgnoreCase("AND")) {
            return leftResult && rightResult;
        } else if (logicalOperator.equalsIgnoreCase("OR")) {
            return leftResult || rightResult;
        }
        return false; // Default safeguard
    }
}
