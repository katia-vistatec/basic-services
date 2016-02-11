package eu.freme.bservices.cloud.testservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestService {

	@Value("${server.port:default}")
	String port;
	
	@RequestMapping(value = "/e-cloud-test", method = RequestMethod.GET)
	public ResponseEntity<String> example() {
		return new ResponseEntity<String>(port, HttpStatus.OK);
	}
}
