package eu.freme.bservices.einternationalizationfilter;

import eu.freme.i18n.api.EInternationalizationAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 1/20/16.
 */
@Configuration
public class EInternationalizationConfig {

    @Bean
    public EInternationalizationAPI getEInternationalizationApi() {
        return new EInternationalizationAPI();
    }
}
