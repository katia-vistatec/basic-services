package eu.freme.bservices.einternationalization.filter;

import eu.freme.bservices.einternationalization.lib.api.EInternationalizationAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 1/20/16.
 */
@Configuration
public class EInternationalizationFilterConfig {

    @Bean
    public EInternationalizationAPI getEInternationalizationApi() {
        return new EInternationalizationAPI();
    }
}
