package edu.uob;

public abstract class QueryNode {
    public abstract boolean evaluate(String[] row, String[] headers);
}
