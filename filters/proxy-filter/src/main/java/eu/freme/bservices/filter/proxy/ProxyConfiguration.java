package eu.freme.bservices.filter.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.WebApplicationInitializer;

@Configuration
public class ProxyConfiguration {
	
	@Autowired 
	ProxyService proxyService;
	
	@Autowired
	ProxyServlet proxyServlet;
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
		
		Map<String,String> proxies = proxyService.getProxies();
		String[] urlMappings = new String[proxies.size()];
		int i=0;
		for( String url : proxies.keySet()){
			urlMappings[i++] = url;
		}
		
		return new ServletRegistrationBean(proxyServlet, urlMappings);
	}
	
//
//	Environment environment;
//
//	@Bean
//	public ServletRegistrationBean servletRegistrationBean() {
//
//
//	}
//
//	@Override
//	public void setEnvironment(Environment environment) {
//		this.environment = environment;
//	}
//
//	@Override
//	public void onStartup(ServletContext servletContext) throws ServletException {
//		// read properties
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(
//				environment);
//		Map<String, Object> map = propertyResolver.getSubProperties("proxy.");
//
//		class Proxy {
//			String servletUrl;
//			String targetUrl;
//		}
//
//		HashMap<String, Proxy> proxies = new HashMap<String, Proxy>();
//
//		for (String key : map.keySet()) {
//			String[] parts = key.split("\\.");
//			if (parts.length != 2) {
//				throw new RuntimeException("bad parameter: \"proxy." + key + "\"");
//			}
//
//			if (!proxies.containsKey(parts[0])) {
//				proxies.put(parts[0], new Proxy());
//			}
//
//			if (parts[1].equals("servlet_url")) {
//				proxies.get(parts[0]).servletUrl = (String) map.get(key);
//			} else if (parts[1].equals("target_url")) {
//				proxies.get(parts[0]).targetUrl = (String) map.get(key);
//			}
//		}
//
//		// create proxy servlets
//
//		for (String key : proxies.keySet()) {
//			Proxy proxy = proxies.get(key);
//			if (proxy.servletUrl == null || proxy.targetUrl == null) {
//				throw new RuntimeException(
//						"Invalid configuration in property \"proxy." + key
//								+ "\"");
//			}
//			
//			ProxyServlet proxyServlet = new ProxyServlet();
//			
//			
//			servletContext.addServlet("proxy-servlet-" + key, new ProxyServlet());
//		}
//	}
}
