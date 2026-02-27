package com.scheduler.kis_client.test.config;

import org.junit.jupiter.api.Test;

import com.scheduler.kis_client.config.Configuration;
import com.scheduler.kis_client.config.Credentials;

public class ConfigurationTest {

    @Test
    public void testAddCredentials() {
        Configuration config = new Configuration();
        Credentials credentials = new Credentials("myApiKey", "myApiSecret");

        config.addCredentials("myCredentials", credentials);

        assert(config.getAllCredentials().get("myCredentials").equals(credentials));
        assert(config.getCredentials("myCredentials").equals(credentials));

        try {
            config.getCredentials("notExist");
            assert(false);
        } catch (IllegalArgumentException e) {
            assert(true);
        }
    }

}
