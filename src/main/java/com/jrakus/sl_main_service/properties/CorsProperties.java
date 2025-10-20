package com.jrakus.sl_main_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cors")
public class CorsProperties {

    private String trustOrigin;

    public String getTrustOrigin() {
        return trustOrigin;
    }

    public void setTrustOrigin(String trustOrigin) {
        this.trustOrigin = trustOrigin;
    }
}
