package eu.freme.bservices.filters.internationalizationfilter;

import eu.freme.bservices.internationalization.api.InternationalizationAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 1/20/16.
 */
@Configuration
public class InternationalizationFilterConfig {

    @Bean
    public InternationalizationAPI getEInternationalizationApi() {
        return new InternationalizationAPI();
    }
}
