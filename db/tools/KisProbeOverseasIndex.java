package db.tools;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.scheduler.kis_client.KisClient;
import com.scheduler.finance.kis.config.KisClientFactory;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceApi;
import com.scheduler.kis_api.api.rest.quotations.InquireOverseasDailyChartPriceResult;

/**
 * Probe KIS overseas index chart API codes.
 */
public class KisProbeOverseasIndex {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static void main(String[] args) throws Exception {
        String[] codes = new String[] {
                ".DJI", "DJI", "DJIA",
                ".INX", "INX", "SPX", "^GSPC", "GSPC",
                ".IXIC", "IXIC", "^IXIC", "NASD",
                ".COMP", "COMP", "CCMP", "NASCOMP", "NASDAQ", "NDX"
        };

        KisClient client = KisClientFactory.getClient();

        LocalDate end = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate start = end.minusDays(30);

        for (String code : codes) {
            try {
                InquireOverseasDailyChartPriceApi api = new InquireOverseasDailyChartPriceApi();
                api.setFidCondMrktDivCode("N");
                api.setFidInputIscd(code);
                api.setFidInputDate1(start.format(YYYYMMDD));
                api.setFidInputDate2(end.format(YYYYMMDD));
                api.setFidPeriodDivCode("D");

                InquireOverseasDailyChartPriceResult r = client.execute(api);
                String rt = (r == null ? "null" : r.getRtCd());
                String msg = (r == null ? "null" : r.getMsg1());
                int n = (r != null && r.getOutput2() != null) ? r.getOutput2().size() : 0;
                System.out.println(code + "\t rt_cd=" + rt + "\t n=" + n + "\t msg=" + msg);
            } catch (Exception e) {
                System.out.println(code + "\t EX=" + e.getClass().getSimpleName() + ":" + e.getMessage());
            }
        }
    }
}
