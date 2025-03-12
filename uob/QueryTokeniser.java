package edu.uob;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryTokeniser {
    public String query;
    public String[] specialCharacters = {"(", ")", ",", ";"};  // ✅ Single-character tokens
    public String[] operators = {"==", "!=", "<=", ">=", ">", "<"}; // ✅ Multi-character operators (EXCLUDING `=`)
    public ArrayList<String> tokens = new ArrayList<>();

    public QueryTokeniser(String query) {
        this.query = query;
        setup();
    }

    private String[] tokenise(String input) {
        // ✅ Replace multi-character operators FIRST
        for (String op : operators) {
            input = input.replace(op, " " + op + " ");
        }

        // ✅ Replace single-character special symbols
        for (String specialCharacter : specialCharacters) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }

        // ✅ Handle `=` separately (after `==` is processed)
        input = handleSingleEquals(input);

        // ✅ Remove extra spaces and trim
        while (input.contains("  ")) input = input.replace("  ", " ");
        input = input.trim();

        return input.split(" ");
    }

    private String handleSingleEquals(String input) {
        // ✅ Ensure `==` is already replaced before processing single `=`
        return input.replaceAll("(?<![=!<>])=(?![=])", " = ");  // ✅ Matches only single `=`
    }

    private void setup() {
        String[] fragments = query.split("'"); // ✅ Split by single quotes

        for (int i = 0; i < fragments.length; i++) {
            if (i % 2 != 0) {
                tokens.add("'" + fragments[i] + "'");  // ✅ Preserve full strings inside quotes
            } else {
                String[] nextBatchOfTokens = tokenise(fragments[i]);
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
    }

    public List<String> getTokens() {
        return new ArrayList<>(tokens);
    }
}
