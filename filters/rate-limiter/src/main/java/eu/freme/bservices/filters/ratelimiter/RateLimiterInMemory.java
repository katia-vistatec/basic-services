package eu.freme.bservices.filters.ratelimiter;

import eu.freme.bservices.filters.ratelimiter.exception.TooManyRequestsException;
import eu.freme.common.exception.InternalServerErrorException;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 18.11.15.
 */

@Component
public class RateLimiterInMemory implements RateCounterInterface {



    private int max_requests;
    private long max_size;
    private int time_frame;

    Properties rateLimiterProperties;

    private ConcurrentHashMap<String,RateCounterObject> storedRequests = new ConcurrentHashMap<>();

    public RateLimiterInMemory(){
    }

    public void refresh(String rateLimiterYaml) throws IOException{
        clear();
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        URL yamlfile = getClass().getClassLoader().getResource(rateLimiterYaml);
        try {
            FileSystemResource fileSystemResource = new FileSystemResource(yamlfile.getFile());
            yaml.setResources(fileSystemResource);
            rateLimiterProperties = yaml.getObject();
            this.time_frame = (int) rateLimiterProperties.get("time-frame") * 1000;
        } catch (NullPointerException e) {
            throw new IOException(rateLimiterYaml+ "not Found in Filesystem");
        }
    }



    @Override
    public void addToStoredRequests(String identifier, long timestamp, long size, String endpointURI, String userRole) throws TooManyRequestsException {

        String finalIdentifier;
        if (rateLimiterProperties.containsKey("rate-limits." + identifier + "." + endpointURI + "[0]")) {
            max_requests=(int)rateLimiterProperties.get("rate-limits." + identifier + "." + endpointURI + "[0]");
            max_size=(int)rateLimiterProperties.get("rate-limits." + identifier + "." + endpointURI + "[1]");
            finalIdentifier="rate-limits." + identifier + "." + endpointURI;
        } else if (rateLimiterProperties.containsKey("rate-limits." + identifier + ".default[0]")) {
            max_requests=(int)rateLimiterProperties.get("rate-limits." + identifier + ".default[0]");
            max_size=(int)rateLimiterProperties.get("rate-limits." + identifier + ".default[1]");
            finalIdentifier="rate-limits."+identifier+".default";
        }
        else if (rateLimiterProperties.containsKey("rate-limits." + userRole + "." + endpointURI + "[0]")) {
            max_requests=(int)rateLimiterProperties.get("rate-limits." + userRole + "." + endpointURI + "[0]");
            max_size=(int)rateLimiterProperties.get("rate-limits." + userRole + "." + endpointURI + "[1]");
            finalIdentifier="rate-limits." + userRole + "." + endpointURI;
        } else if (rateLimiterProperties.containsKey("rate-limits." + userRole + ".default[0]")) {
            max_requests=(int)rateLimiterProperties.get("rate-limits." + userRole + ".default[0]");
            max_size=(int)rateLimiterProperties.get("rate-limits." + userRole + ".default[1]");
            finalIdentifier="rate-limits."+userRole+".default";
        } else {
            throw new InternalServerErrorException("No identifier found for "+identifier+"with role"+ userRole + "for resource" + endpointURI);
        }

        if (max_size==0 && max_requests==0) {
            return;
        }
        try {
            storedRequests.get(finalIdentifier).add_entry(timestamp, size);
        } catch (NullPointerException e) {
            storedRequests.put(finalIdentifier, new RateCounterObject(time_frame, timestamp, max_requests, size+1, max_size));
        }

    }

    public void clear() {
        storedRequests.clear();
    }


}
