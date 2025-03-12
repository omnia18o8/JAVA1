package edu.uob;

import java.util.List;
import java.util.ArrayList;

public class QueryLexer {
    public  List<String> tokens;
    public final List<TokenType> tokenTypes = new ArrayList<>();

    public QueryLexer(List<String> tokens) {
        this.tokens = tokens;
    }

    public void tokenize() {
        for (String token : tokens) {
            if (isKeyword(token)) {
                tokenTypes.add(TokenType.KEYWORD);
            } else if (isSymbol(token)) {
                tokenTypes.add(TokenType.SYMBOL);
            } else if (isLogicalOperator(token)) {
                tokenTypes.add(TokenType.LOGICAL_OPERATOR);
            } else if (isOperator(token)) {
                tokenTypes.add(TokenType.OPERATOR);
            } else if (isNumber(token)) {
                tokenTypes.add(TokenType.NUMBER);
            } else if (isString(token)) {
                tokenTypes.add(TokenType.STRING);
            } else if (isIdentifier(token)) { // ✅ Identifier validation
                tokenTypes.add(TokenType.IDENTIFIER);
            } else {
                System.out.println("[DEBUG] Invalid Token: " + token); // ✅ Debugging
                throw new IllegalArgumentException("[ERROR] Invalid token detected: " + token);
            }
            System.out.println("[DEBUG] Token: " + token + " -> " + tokenTypes.get(tokenTypes.size() - 1)); // ✅ Debugging output
        }
    }


    public boolean isKeyword(String token) {
        return List.of("USE", "SELECT", "FROM", "WHERE", "SET", "INSERT", "UPDATE", "DELETE", "CREATE",
                        "DROP", "TABLE", "DATABASE", "ALTER", "INTO", "VALUES", "ADD", "JOIN", "ON")
                .contains(token.toUpperCase());
    }

    public boolean isIdentifier(String token) {
        return token.matches("[a-zA-Z0-9_]+");
    }

    public boolean isLogicalOperator(String token) {
        return token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR");
    }

    public boolean isSymbol(String token) {
        return List.of("(", ")", ",", ";", "*","=").contains(token);
    }


    public boolean isOperator(String token) {
        return List.of(">", "<", ">=", "<=", "==", "!=").contains(token);
    }

    public boolean isNumber(String token) {
        return token.matches("[+-]?\\d+(\\.\\d+)?");
    }

    public boolean isString(String token) {
        return token.startsWith("'") && token.endsWith("'");
    }

    public List<TokenType> getTokenTypes() {
        return tokenTypes;
    }
}
