package eu.freme.bservices.filter.proxy;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import eu.freme.bservices.filter.proxy.exception.BadGatewayException;
import eu.freme.common.exception.ExceptionHandlerService;

@Controller("ProxyController")
public class ProxyController{

	Logger logger = Logger.getLogger(ProxyController.class);

	@Autowired
	ProxyService proxyService;

	@Autowired
	ExceptionHandlerService exceptionHandlerService;

	Map<String, String> proxies;

	@PostConstruct
	public void init() {
		proxies = proxyService.getProxies();
	}

	private ResponseEntity<String> doProxy(HttpServletRequest request) {
		try {
			HttpRequest proxy = proxyService.createProxy(request,
					proxies.get(request.getServletPath()));
			return proxyService.createResponse(proxy);
		} catch (IOException | UnirestException e) {
			logger.error("failed", e);
			throw new BadGatewayException("Proxy failed: " + e.getMessage());
		}
	}

	@RequestMapping
	public ResponseEntity<String> doPost(HttpServletRequest request) {
		return doProxy(request);
	}
}
