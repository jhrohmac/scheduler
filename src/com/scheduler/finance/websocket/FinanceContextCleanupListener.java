package com.scheduler.finance.websocket;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scheduler.finance.kis.config.KisClientFactory;

@WebListener
public class FinanceContextCleanupListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(FinanceContextCleanupListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // no-op
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            WatchlistQuoteHub.getInstance().shutdown();
        } catch (Exception e) {
            logger.warn("[FINANCE] WatchlistQuoteHub shutdown failed", e);
        }

        try {
            MarketSummaryRealtimeEndpoint.shutdown();
        } catch (Exception e) {
            logger.warn("[FINANCE] MarketSummaryRealtimeEndpoint shutdown failed", e);
        }

        try {
            KisClientFactory.shutdown();
        } catch (Exception e) {
            logger.warn("[FINANCE] KisClientFactory shutdown failed", e);
        }
    }
}
