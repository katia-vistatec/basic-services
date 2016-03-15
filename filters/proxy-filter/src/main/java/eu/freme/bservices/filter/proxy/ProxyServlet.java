package eu.freme.bservices.filter.proxy;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import eu.freme.common.exception.InternalServerErrorException;


@SuppressWarnings("serial")
@Component
public class ProxyServlet extends HttpServlet {
	
	Logger logger = Logger.getLogger(ProxyServlet.class);
	
	@Autowired
	ProxyService proxyService;
	
	Map<String,String> proxies;
	
	@PostConstruct
	public void init(){
		proxies = proxyService.getProxies();
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response){
		
		try {
			HttpRequest proxy = proxyService.createProxy(request, proxies.get(request.getRequestURI()));
			proxyService.writeProxyToResponse(response, proxy);
		} catch (IOException | UnirestException e) {
			logger.error("failed", e);
			throw new InternalServerErrorException("Proxy failed");
		}
	}
}
