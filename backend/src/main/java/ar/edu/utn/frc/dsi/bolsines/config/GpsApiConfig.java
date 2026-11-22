package ar.edu.utn.frc.dsi.bolsines.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GpsApiConfig {

    private static volatile String configuredBaseUrl = "http://localhost:8080";

    @Value("${gps.api.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${gps.api.keys.xtr-4500l}")
    private String apiKeyXtr4500l;

    @Value("${gps.api.keys.navtrack-qx-7a}")
    private String apiKeyNavTrackQx7a;

    @Value("${gps.api.keys.geopulse-mtr-900}")
    private String apiKeyGeoPulseMtr900;

    @PostConstruct
    void initBaseUrl() {
        if (this.baseUrl != null && !this.baseUrl.isBlank()) {
            configuredBaseUrl = this.baseUrl.trim();
        }
    }

    public static String getConfiguredBaseUrl() {
        return configuredBaseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKeyXtr4500l() {
        return apiKeyXtr4500l;
    }

    public String getApiKeyNavTrackQx7a() {
        return apiKeyNavTrackQx7a;
    }

    public String getApiKeyGeoPulseMtr900() {
        return apiKeyGeoPulseMtr900;
    }

    public String[] getApiKeys() {
        return new String[]{apiKeyXtr4500l, apiKeyNavTrackQx7a, apiKeyGeoPulseMtr900};
    }
}
