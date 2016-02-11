package eu.freme.bservices.cloud.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class EurekaServer {

	public static void main(String[] args) {
	    SpringApplication.run(EurekaServer.class, new String[]{"--server.port=5000"});
	}

}
