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
import eu.freme.common.exception.InternalServerErrorException;

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
			for( String prefix : proxies.keySet() ){
				if( request.getServletPath().startsWith(prefix)){
					
					String pathParam = request.getServletPath().substring(prefix.length());
					
					String targetUrl = proxies.get(prefix);
					if( pathParam.length() > 0){
						if( !targetUrl.endsWith("/")){
							targetUrl += "/";
						}
						
						if( pathParam.startsWith("/")){
							pathParam = pathParam.substring(1);
						}
					}
					pathParam += targetUrl;
					
					HttpRequest proxy = proxyService.createProxy(request, targetUrl);
					return proxyService.createResponse(proxy);					
				}
			}
			
			throw new InternalServerErrorException();
			
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
