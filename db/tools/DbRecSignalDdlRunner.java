package db.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DbRecSignalDdlRunner {

    private static final String[] DDL_FILES = new String[] {
        "DDL_TB_STK_MASTER.sql",
        "DDL_TB_REC_SIGNAL.sql",
        "DDL_TB_TRADE_CALENDAR.sql",
        "DDL_TB_BATCH_EXEC_LOG.sql",
        "DDL_TB_BATCH_EXEC_ITEM_LOG.sql"
    };

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: DbRecSignalDdlRunner <oracle.db.properties path> <db dir>");
            System.exit(2);
        }

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            p.load(in);
        }

        String url = trim(p.getProperty("jdbc.url"));
        String user = trim(p.getProperty("jdbc.username"));
        String pass = trim(p.getProperty("jdbc.password"));
        File dbDir = new File(args[1]);

        if (!dbDir.isDirectory()) {
            throw new IllegalStateException("db 디렉터리를 찾을 수 없습니다: " + dbDir.getAbsolutePath());
        }

        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (Throwable ignore) {
        }

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            c.setAutoCommit(true);
            for (String fileName : DDL_FILES) {
                applyFile(c, new File(dbDir, fileName));
            }
            System.out.println("DONE");
        }
    }

    private static void applyFile(Connection c, File file) throws Exception {
        if (!file.isFile()) {
            throw new IllegalStateException("DDL 파일이 없습니다: " + file.getAbsolutePath());
        }

        List<String> statements = splitStatements(file);
        for (String sql : statements) {
            executeIgnoreExists(c, sql);
        }
        System.out.println("APPLIED: " + file.getName());
    }

    private static List<String> splitStatements(File file) throws Exception {
        List<String> list = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.length() == 0) {
                    continue;
                }
                builder.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String sql = builder.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim();
                    if (sql.length() > 0) {
                        list.add(sql);
                    }
                    builder.setLength(0);
                }
            }
        }

        String rest = builder.toString().trim();
        if (rest.length() > 0) {
            list.add(rest);
        }
        return list;
    }

    private static void executeIgnoreExists(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (SQLException e) {
            if (e.getErrorCode() == 955) {
                return;
            }
            throw e;
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
