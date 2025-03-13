package edu.uob;
import java.util.ArrayList;
import java.util.List;

public abstract class DBCmd {
    String databaseName;
    List<String> tableNames;
    List<String> columnNames;

    public DBCmd(DBServer server) {
        this.databaseName = server.getDatabaseName();
        this.tableNames = new ArrayList<>();
        this.columnNames = new ArrayList<>();
    }

    public abstract String query(DBServer server);
}
