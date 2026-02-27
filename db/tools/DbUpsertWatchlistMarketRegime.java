package db.tools;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * Ensure market regime assets are present in watchlist group 0000.
 * Uses MERGE logic similar to oracle_MarketSummary.xml#insertWatchlistItem.
 */
public class DbUpsertWatchlistMarketRegime {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DbUpsertWatchlistMarketRegime <oracle.db.properties path>");
            System.exit(2);
        }

        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            p.load(in);
        }

        String url = p.getProperty("jdbc.url").trim();
        String user = p.getProperty("jdbc.username").trim();
        String pass = p.getProperty("jdbc.password").trim();

        Class.forName("oracle.jdbc.OracleDriver");

        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            c.setAutoCommit(true);

            String[] codes = new String[] {
                    // KR
                    "0001", "2001", "1001",
                    // US indices
                    ".DJI", "SPX", "COMP"
            };

            for (int i = 0; i < codes.length; i++) {
                upsert(c, "0000", codes[i]);
            }

            System.out.println("DONE");
        }
    }

    private static void upsert(Connection c, String groupId, String stockCode) throws Exception {
        String sql = "MERGE INTO TB_S_INTEREST_WATCHLIST T\n" +
                "USING (SELECT ? AS STOCK_GROUP, ? AS STOCK_CODE FROM DUAL) S\n" +
                "ON (T.STOCK_GROUP = S.STOCK_GROUP AND T.STOCK_CODE = S.STOCK_CODE)\n" +
                "WHEN MATCHED THEN\n" +
                "  UPDATE SET T.MODIFY_DATE = SYSDATE, T.STOCK_GROUP_DIV = NVL(T.STOCK_GROUP_DIV, 'normal')\n" +
                "WHEN NOT MATCHED THEN\n" +
                "  INSERT (STOCK_SEQ, STOCK_CODE, STOCK_GROUP, STOCK_GROUP_DIV, STOCK_CLOSE, STOCK_30MIN_SMA_LINES, STOCK_SMA_LINES, MODIFY_DATE, CREATE_DATE)\n" +
                "  VALUES ((SELECT NVL(MAX(STOCK_SEQ),0)+1 FROM TB_S_INTEREST_WATCHLIST), S.STOCK_CODE, S.STOCK_GROUP, 'normal', NULL, NULL, NULL, SYSDATE, SYSDATE)";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, groupId);
            ps.setString(2, stockCode);
            int rows = ps.executeUpdate();
            System.out.println("WATCHLIST UPSERT group=" + groupId + " code=" + stockCode + " rows=" + rows);
        }
    }
}
