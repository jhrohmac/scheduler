package db.tools;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

/**
 * Upsert market regime assets into TB_S_INTEREST_STOCK.
 * - KR indices: 0001(KOSPI), 2001(KOSDAQ), 1001(KOSPI200)
 * - US proxies: SPY, QQQ, IWM
 *
 * This tool is intentionally idempotent.
 */
public class DbUpsertMarketRegimeAssets {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: DbUpsertMarketRegimeAssets <oracle.db.properties path>");
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

            // KR indices (domestic index API codes)
            upsert(c,
                    "0001", "코스피(KOSPI)", "KOSPI", "KR", "index", "IDX", null,
                    "KRW", "Asia/Seoul", "market_regime|KOSPI", "Y");
            upsert(c,
                    "2001", "코스닥(KOSDAQ)", "KOSDAQ", "KR", "index", "IDX", null,
                    "KRW", "Asia/Seoul", "market_regime|KOSDAQ", "Y");
            upsert(c,
                    "1001", "코스피200(KOSPI200)", "KOSPI200", "KR", "index", "IDX", null,
                    "KRW", "Asia/Seoul", "market_regime|KOSPI200", "Y");

            // US market regime indices (preferred)
            upsert(c,
                    ".DJI", "다우지수(Dow Jones)", "Dow Jones", "US", "index", "INDEX", "IDX",
                    "USD", "America/New_York", "market_regime|DOW", "Y");
            upsert(c,
                    "SPX", "S&P500", "S&P 500", "US", "index", "INDEX", "IDX",
                    "USD", "America/New_York", "market_regime|SP500", "Y");
            upsert(c,
                    "COMP", "나스닥(종합)", "NASDAQ Composite", "US", "index", "INDEX", "IDX",
                    "USD", "America/New_York", "market_regime|NASDAQ", "Y");

            // Keep ETF proxies too (fallback / alternative usage)
            upsert(c,
                    "SPY", "SPDR S&P 500 ETF", "SPY", "US", "stock", "NASDAQ", "ETF",
                    "USD", "America/New_York", "market_regime_proxy|SP500", "Y");
            upsert(c,
                    "QQQ", "Invesco QQQ ETF", "QQQ", "US", "stock", "NASDAQ", "ETF",
                    "USD", "America/New_York", "market_regime_proxy|NASDAQ", "Y");
            upsert(c,
                    "IWM", "iShares Russell 2000 ETF", "IWM", "US", "stock", "NASDAQ", "ETF",
                    "USD", "America/New_York", "market_regime_proxy|RUSSELL2000", "Y");

            System.out.println("DONE");
        }
    }

    private static void upsert(Connection c,
            String code,
            String ko,
            String en,
            String country,
            String type,
            String market,
            String typeSpecs,
            String currency,
            String timezone,
            String aliasTag,
            String useDiv) throws Exception {

        // Append aliasTag to STOCK_ALIASES if not present.
        String sql = "MERGE INTO TB_S_INTEREST_STOCK T\n" +
                "USING (SELECT ? AS STOCK_CODE, ? AS STOCK_COUNTRY_CODE FROM DUAL) S\n" +
                "ON (T.STOCK_CODE = S.STOCK_CODE AND NVL(T.STOCK_COUNTRY_CODE,'KR') = S.STOCK_COUNTRY_CODE)\n" +
                "WHEN MATCHED THEN UPDATE SET\n" +
                "  T.STOCK_KO_NAME = NVL(?, T.STOCK_KO_NAME),\n" +
                "  T.STOCK_EN_NAME = NVL(?, T.STOCK_EN_NAME),\n" +
                "  T.STOCK_TYPE = NVL(?, T.STOCK_TYPE),\n" +
                "  T.STOCK_MARKET = NVL(?, T.STOCK_MARKET),\n" +
                "  T.STOCK_TYPE_SPECS = NVL(?, T.STOCK_TYPE_SPECS),\n" +
                "  T.STOCK_CURRENCY = NVL(?, T.STOCK_CURRENCY),\n" +
                "  T.STOCK_TIMEZONE = NVL(?, T.STOCK_TIMEZONE),\n" +
                "  T.USE_DIV = ?,\n" +
                "  T.STOCK_ALIASES = CASE\n" +
                "    WHEN T.STOCK_ALIASES IS NULL OR T.STOCK_ALIASES = '' THEN ?\n" +
                "    WHEN INSTR(T.STOCK_ALIASES, ?) > 0 THEN T.STOCK_ALIASES\n" +
                "    ELSE T.STOCK_ALIASES || '|' || ?\n" +
                "  END,\n" +
                "  T.MODIFY_DATE = SYSDATE\n" +
                "WHEN NOT MATCHED THEN INSERT (\n" +
                "  STOCK_SEQ, STOCK_ID, STOCK_CODE, STOCK_KO_NAME, STOCK_EN_NAME, STOCK_COUNTRY_CODE,\n" +
                "  STOCK_TYPE, STOCK_MARKET, STOCK_TYPE_SPECS, STOCK_CURRENCY, STOCK_TIMEZONE, STOCK_ALIASES,\n" +
                "  USE_DIV, MODIFY_DATE, CREATE_DATE\n" +
                ") VALUES (\n" +
                "  (SELECT NVL(MAX(STOCK_SEQ),0)+1 FROM TB_S_INTEREST_STOCK), SYS_GUID(), ?, ?, ?, ?,\n" +
                "  ?, ?, ?, ?, ?, ?,\n" +
                "  ?, SYSDATE, SYSDATE\n" +
                ")";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, code);
            ps.setString(i++, country);

            ps.setString(i++, ko);
            ps.setString(i++, en);
            ps.setString(i++, type);
            ps.setString(i++, market);
            ps.setString(i++, typeSpecs);
            ps.setString(i++, currency);
            ps.setString(i++, timezone);
            ps.setString(i++, useDiv);
            ps.setString(i++, aliasTag);
            ps.setString(i++, aliasTag);
            ps.setString(i++, aliasTag);

            ps.setString(i++, code);
            ps.setString(i++, ko);
            ps.setString(i++, en);
            ps.setString(i++, country);
            ps.setString(i++, type);
            ps.setString(i++, market);
            ps.setString(i++, typeSpecs);
            ps.setString(i++, currency);
            ps.setString(i++, timezone);
            ps.setString(i++, aliasTag);
            ps.setString(i++, useDiv);

            int rows = ps.executeUpdate();
            System.out.println("UPSERT " + code + " (" + country + ") rows=" + rows);
        }
    }
}
