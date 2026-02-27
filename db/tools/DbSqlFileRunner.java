package db.tools;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DbSqlFileRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: DbSqlFileRunner <oracle.db.properties> <sql-file>");
            System.exit(2);
        }

        String propPath = args[0];
        String sqlPath = args[1];

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(propPath)) {
            p.load(in);
        }

        String url = p.getProperty("jdbc.url", "").trim();
        String user = p.getProperty("jdbc.username", "").trim();
        String pass = p.getProperty("jdbc.password", "").trim();

        Class.forName("oracle.jdbc.OracleDriver");

        String raw = new String(Files.readAllBytes(Paths.get(sqlPath)), StandardCharsets.UTF_8);
        List<String> stmts = splitStatements(raw);

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            c.setAutoCommit(true);
            int ok = 0;
            for (String s : stmts) {
                try (Statement st = c.createStatement()) {
                    st.execute(s);
                    ok++;
                }
            }
            System.out.println("DONE statements=" + ok + " file=" + sqlPath);
        }
    }

    private static List<String> splitStatements(String sql) {
        String[] lines = sql.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        List<String> out = new ArrayList<>();
        for (String line : lines) {
            String t = line.trim();
            if (t.startsWith("--") || t.isEmpty()) continue;
            sb.append(line).append('\n');
            if (t.endsWith(";")) {
                String s = sb.toString().trim();
                if (s.endsWith(";")) s = s.substring(0, s.length() - 1);
                if (!s.isEmpty()) out.add(s);
                sb.setLength(0);
            }
        }
        String rest = sb.toString().trim();
        if (!rest.isEmpty()) out.add(rest);
        return out;
    }
}
