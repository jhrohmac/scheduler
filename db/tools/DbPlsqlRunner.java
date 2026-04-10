package db.tools;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * PL/SQL 블록(BEGIN...END;/)을 포함한 SQL 파일을 Oracle에 실행하는 유틸
 * 구분자: 일반 구문은 ';', PL/SQL 블록은 '/' (단독 행)
 */
public class DbPlsqlRunner {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: DbPlsqlRunner <oracle.db.properties> <sql-file>");
            System.exit(2);
        }

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            p.load(in);
        }

        String url  = p.getProperty("jdbc.url",      "").trim();
        String user = p.getProperty("jdbc.username", "").trim();
        String pass = p.getProperty("jdbc.password", "").trim();

        Class.forName("oracle.jdbc.OracleDriver");

        String raw = new String(Files.readAllBytes(Paths.get(args[1])), StandardCharsets.UTF_8);
        List<String> stmts = splitStatements(raw);

        System.out.println("파일: " + args[1] + " (" + stmts.size() + "개 구문)");

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            c.setAutoCommit(true);
            int ok = 0;
            for (String stmt : stmts) {
                String trimmed = stmt.trim();
                if (trimmed.isEmpty()) continue;
                try (Statement st = c.createStatement()) {
                    st.execute(trimmed);
                    System.out.println("  [OK] " + trimmed.substring(0, Math.min(60, trimmed.length())).replace('\n', ' '));
                    ok++;
                } catch (Exception e) {
                    System.err.println("  [FAIL] " + trimmed.substring(0, Math.min(60, trimmed.length())).replace('\n', ' '));
                    System.err.println("         " + e.getMessage());
                    throw e;
                }
            }
            System.out.println("완료: " + ok + "개 실행");
        }
    }

    /**
     * SQL 파일을 구문 단위로 분리
     * - 일반 구문: 행 끝 ';' 로 종료
     * - PL/SQL 블록: 단독 '/' 행으로 종료 (BEGIN/DECLARE 포함 블록)
     */
    private static List<String> splitStatements(String sql) {
        String[] lines = sql.split("\\r?\\n");
        List<String> result = new ArrayList<String>();
        StringBuilder buf = new StringBuilder();
        boolean inPlsql = false;

        for (String line : lines) {
            String trimmed = line.trim();

            /* PROMPT, COMMENT 행 skip */
            if (trimmed.startsWith("PROMPT") || trimmed.startsWith("--") || trimmed.isEmpty()) {
                continue;
            }

            /* PL/SQL 블록 시작 감지 */
            if (!inPlsql && (trimmed.toUpperCase().startsWith("DECLARE") ||
                             trimmed.toUpperCase().startsWith("BEGIN"))) {
                inPlsql = true;
            }

            if (inPlsql) {
                /* '/' 단독 행 = PL/SQL 블록 종료 */
                if (trimmed.equals("/")) {
                    String block = buf.toString().trim();
                    if (!block.isEmpty()) result.add(block);
                    buf.setLength(0);
                    inPlsql = false;
                } else {
                    /* PL/SQL 내부 ';' 는 구문 끝이 아님 */
                    buf.append(line).append('\n');
                }
            } else {
                buf.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String s = buf.toString().trim();
                    /* 끝 ';' 제거 */
                    if (s.endsWith(";")) s = s.substring(0, s.length() - 1).trim();
                    if (!s.isEmpty()) result.add(s);
                    buf.setLength(0);
                }
            }
        }

        String rest = buf.toString().trim();
        if (!rest.isEmpty()) result.add(rest);
        return result;
    }
}
