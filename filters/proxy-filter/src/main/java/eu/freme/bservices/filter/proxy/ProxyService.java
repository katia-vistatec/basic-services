package eu.freme.bservices.filter.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import eu.freme.bservices.filter.proxy.exception.MethodNotSupportedException;

@Component
public class ProxyService implements EnvironmentAware{

	Logger logger = Logger.getLogger(ProxyService.class);
	Environment environment;
	
	public ProxyService(){
	}

	public HttpRequest createProxy(HttpServletRequest request, String targetUri)
			throws IOException {

		HttpRequest proxy = null;

		// url with query string
		String url = targetUri + "?" + request.getQueryString();

		// create request with method
		switch (request.getMethod().toLowerCase()) {
		case "get":
			proxy = Unirest.get(url);
			break;
		case "post":
			proxy = Unirest.post(url);
			break;
		case "put":
			proxy = Unirest.put(url);
			break;
		case "delete":
			proxy = Unirest.delete(url);
			break;
		case "options":
			proxy = Unirest.options(url);
			break;
		default:
			throw new MethodNotSupportedException(request.getMethod());
		}

		// add body if necessary
		if (request.getMethod().toLowerCase().equals("post")
				|| request.getMethod().toLowerCase().equals("put")) {
			
			StringBuilder bldr = new StringBuilder();
			BufferedReader reader = request.getReader();
			char[] cbuf = new char[1024];
			int read;
			while ((read = reader.read(cbuf)) > 0) {
				bldr.append(cbuf,0,read);
			}
			((HttpRequestWithBody)proxy).body(bldr.toString());
		}
		
		// copy headers
		Enumeration<String> headerEnum = request.getHeaderNames();
		while( headerEnum.hasMoreElements() ){
			String key = headerEnum.nextElement();
			if( !key.toLowerCase().equals("host") && !key.toLowerCase().equals("content-length")){
				proxy.header(key, request.getHeader(key));				
			}
		}
		
		return proxy;
	}
	
	public void writeProxyToResponse(HttpServletResponse response, HttpRequest proxy) throws UnirestException, IOException{
		HttpResponse<String> proxyResponse = proxy.asString();
		String body = proxyResponse.getBody();
		response.getWriter().write(body);
		response.setContentLength(body.length());
		response.setStatus(proxyResponse.getStatus());
		for( String header : proxyResponse.getHeaders().keySet()){
			response.setHeader(header, proxyResponse.getHeaders().getFirst(header));
		}
	}
	

	

	/**
	 * parse proxy configuration properties
	 */
	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	public Map<String,String> getProxies(){
		
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(
				environment);
		Map<String, Object> map = propertyResolver.getSubProperties("proxy.");

		class Proxy {
			String servletUrl;
			String targetUrl;
		}

		HashMap<String, Proxy> tempProxies = new HashMap<String, Proxy>();

		for (String key : map.keySet()) {
			String[] parts = key.split("\\.");
			if (parts.length != 2) {
				throw new RuntimeException("bad parameter: \"proxy." + key
						+ "\"");
			}

			if (!tempProxies.containsKey(parts[0])) {
				tempProxies.put(parts[0], new Proxy());
			}

			if (parts[1].equals("servlet_url")) {
				tempProxies.get(parts[0]).servletUrl = (String) map.get(key);
			} else if (parts[1].equals("target_url")) {
				tempProxies.get(parts[0]).targetUrl = (String) map.get(key);
			}
		}
		HashMap<String, String> proxies = new HashMap<String, String>();
		for (String key : tempProxies.keySet()) {
			Proxy proxy = tempProxies.get(key);
			if (proxy.servletUrl == null || proxy.targetUrl == null) {
				throw new RuntimeException(
						"Bad parameter configuration for proxy \"" + key + "\"");
			}
			proxies.put(proxy.servletUrl, proxy.targetUrl);
		}
		
		return proxies;
	}
}
