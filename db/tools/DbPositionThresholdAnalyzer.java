package db.tools;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class DbPositionThresholdAnalyzer {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DbPositionThresholdAnalyzer <oracle.db.properties path>");
            System.exit(2);
        }

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            p.load(in);
        }

        String url = p.getProperty("jdbc.url", "").trim();
        String user = p.getProperty("jdbc.username", "").trim();
        String pass = p.getProperty("jdbc.password", "").trim();

        Class.forName("oracle.jdbc.OracleDriver");

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Connected");

            printDistribution(c);
            printAvgByType(c);
            printBoundaryCounts(c);
        }
    }

    private static void printDistribution(Connection c) throws Exception {
        String sql = "SELECT EVENT_TYPE, COUNT(*) CNT FROM TB_S_POSITION_EVENT WHERE EVENT_TIME >= SYSDATE - 30 GROUP BY EVENT_TYPE ORDER BY CNT DESC";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println("\n[Distribution 30d]");
            while (rs.next()) {
                System.out.println(rs.getString(1) + "\t" + rs.getLong(2));
            }
        }
    }

    private static void printAvgByType(Connection c) throws Exception {
        String expr = "TO_NUMBER(REGEXP_SUBSTR(JSON_PARAMS, '\"pnl_pct\":(-?[0-9]+\\.?[0-9]*)', 1, 1, NULL, 1))";
        String sql = "SELECT EVENT_TYPE, ROUND(AVG(" + expr + "),4) AVG_PCT, COUNT(*) CNT "
                + "FROM TB_S_POSITION_EVENT WHERE EVENT_TIME >= SYSDATE - 30 AND JSON_PARAMS IS NOT NULL "
                + "GROUP BY EVENT_TYPE ORDER BY CNT DESC";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println("\n[Avg pnl_pct by event type 30d]");
            while (rs.next()) {
                System.out.println(rs.getString(1) + "\tavg=" + rs.getString(2) + "\tcnt=" + rs.getLong(3));
            }
        }
    }

    private static void printBoundaryCounts(Connection c) throws Exception {
        String expr = "TO_NUMBER(REGEXP_SUBSTR(JSON_PARAMS, '\"pnl_pct\":(-?[0-9]+\\.?[0-9]*)', 1, 1, NULL, 1))";
        String sql = "SELECT "
                + "SUM(CASE WHEN EVENT_TYPE='RISK_OFF' AND " + expr + " BETWEEN -4 AND -2 THEN 1 ELSE 0 END) RO_BOUND,"
                + "SUM(CASE WHEN EVENT_TYPE='TREND_FOLLOW' AND " + expr + " BETWEEN 0 AND 2 THEN 1 ELSE 0 END) TF_BOUND "
                + "FROM TB_S_POSITION_EVENT WHERE EVENT_TIME >= SYSDATE - 30 AND JSON_PARAMS IS NOT NULL";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("\n[Boundary counts 30d]");
                System.out.println("RISK_OFF(-4~-2): " + rs.getLong(1));
                System.out.println("TREND_FOLLOW(0~2): " + rs.getLong(2));
            }
        }
    }
}
