package com.scheduler.finance.kis.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * KIS 설정 파일(kis.properties)을 로딩/갱신하는 유틸리티.
 *
 * 실제 파일 위치 (개발 가이드):
 *   /scheduler/webapp/WEB-INF/resources/kis/kis.properties
 *   /scheduler/webapp/WEB-INF/resources/kis/properties/kis.properties
 */
public class KisConfigLoader {

    private static final String[] CLASSPATH_LOCATIONS = {
            "/kis/kis.properties",
            "/kis.properties"
    };

    private static final String[] WEBAPP_RELATIVE_PATHS = {
            "WEB-INF/resources/kis/kis.properties",
            "WEB-INF/resources/kis/properties/kis.properties"
    };

    private static final String[] PROJECT_RELATIVE_PATHS = {
            "webapp/WEB-INF/resources/kis/kis.properties",
            "webapp/WEB-INF/resources/kis/properties/kis.properties"
    };

    private static volatile KisProperties kisProperties;

    public static KisProperties getKisProperties() {
        KisProperties local = kisProperties;
        if (local == null) {
            synchronized (KisConfigLoader.class) {
                local = kisProperties;
                if (local == null) {
                    local = loadKisProperties();
                    kisProperties = local;
                }
            }
        }
        return local;
    }

    private static KisProperties loadKisProperties() {
        Properties props = new Properties();
        boolean loaded = false;

        File external = findExternalFile(false);
        if (external != null) {
            try (InputStream is = new FileInputStream(external)) {
                props.load(is);
                loaded = true;
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load KIS properties from file: " + external, e);
            }
        }

        if (!loaded) {
            for (String path : CLASSPATH_LOCATIONS) {
                try (InputStream is = KisConfigLoader.class.getResourceAsStream(path)) {
                    if (is != null) {
                        props.load(is);
                        loaded = true;
                        break;
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to load KIS properties from " + path, e);
                }
            }
        }

        if (!loaded) {
            throw new IllegalStateException(
                    "KIS properties file not found. Checked classpath: /kis/kis.properties, /kis.properties, and file paths under webapp.root or project webapp.");
        }

        return new KisProperties(props);
    }

    /**
     * 설정을 다시 읽고 싶은 경우에 사용.
     */
    public static synchronized void reload() {
        kisProperties = null;
        getKisProperties();
    }

    /**
     * 새로 발급받은 AppToken 정보를 kis.properties 파일에 저장한다.
     *
     * @param token        AccessToken 문자열
     * @param expiredAtStr 만료시간 문자열 (GetTokenResult.getAccessTokenTokenExpired() 그대로)
     */
    public static synchronized void updateAppToken(String token, String expiredAtStr) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        File external = findExternalFile(true);
        if (updateAppTokenInFile(external, token, expiredAtStr)) {
            return;
        }

        for (String path : CLASSPATH_LOCATIONS) {
            URL url = KisConfigLoader.class.getResource(path);
            if (url == null) {
                continue;
            }

            File file = toFile(url);
            if (updateAppTokenInFile(file, token, expiredAtStr)) {
                return;
            }
        }

        System.err.println("[KisConfigLoader] Failed to update kis.properties (no writable file found).");
    }

    private static boolean updateAppTokenInFile(File file, String token, String expiredAtStr) {
        if (file == null || !file.exists() || !file.isFile() || !file.canWrite()) {
            return false;
        }

        Properties props = new Properties();
        try (InputStream is = new FileInputStream(file)) {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        props.setProperty("kis.app.tokenKey", token);
        if (expiredAtStr != null && !expiredAtStr.trim().isEmpty()) {
            props.setProperty("kis.app.tokenExpiredAt", expiredAtStr.trim());
        }

        try (OutputStream os = new FileOutputStream(file)) {
            props.store(os, "KIS AppToken auto updated");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (kisProperties != null) {
            kisProperties.setAppTokenKey(token);
            kisProperties.setAppTokenExpiredAt(expiredAtStr);
        }

        return true;
    }

    private static File findExternalFile(boolean requireWritable) {
        String webappRoot = System.getProperty("webapp.root");
        File file = findInBase(webappRoot, WEBAPP_RELATIVE_PATHS, requireWritable);
        if (file != null) {
            return file;
        }
        return findInBase(null, PROJECT_RELATIVE_PATHS, requireWritable);
    }

    private static File findInBase(String basePath, String[] relPaths, boolean requireWritable) {
        if (relPaths == null) {
            return null;
        }
        for (String rel : relPaths) {
            File f = (basePath == null) ? new File(rel) : new File(basePath, rel);
            if (f.exists() && f.isFile() && (!requireWritable || f.canWrite())) {
                return f;
            }
        }
        return null;
    }

    private static File toFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            return new File(url.getPath());
        }
    }

    private KisConfigLoader() {
    }
}