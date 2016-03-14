package eu.freme.bservices.filter.proxy;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.InternalServerErrorException;

@RestController
public class ProxyServiceTestRestController {

	@RequestMapping(value = "/proxy-destination", method = RequestMethod.POST)
	public ResponseEntity<String> proxyTarget(HttpServletRequest req,
			@RequestParam("test") String testQueryString,
			@RequestBody String body) {

		if (testQueryString == null || !testQueryString.equals("value")) {
			throw new InternalServerErrorException("error in query string");
		}

		if (body == null || !body.equals("body\nbody")) {
			throw new InternalServerErrorException("error in body");
		}

		String header = req.getHeader("my-header");
		if (header == null || !header.equals("header-value")) {
			throw new InternalServerErrorException("error in header");
		}

		return new ResponseEntity<String>("response", HttpStatus.OK);
	}
}
