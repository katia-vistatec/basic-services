package eu.freme.bservices.filter.proxy;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
