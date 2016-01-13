package eu.freme.bservices.filter.ratelimiter;

/**
 * Created by Jonathan Sauder (jonathan.sauder@student.hpi.de) on 18.11.15.
 */
public interface RateCounterInterface {

    void addToStoredRequests(String username, long timestamp, long size, String endpointURI, String userRole);

}
