package eu.freme.bservices.cloud.loadbalancer;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class LoadBalancerConfiguration {

	@Autowired
	LoadBalancerServlet loadBalancerServlet;
	
	public HashMap<String,LoadBalancingProxyConfig> getProxyConfigurations(){
		HashMap<String,LoadBalancingProxyConfig> proxies = new HashMap<String, LoadBalancingProxyConfig>();
		
		LoadBalancingProxyConfig conf = new LoadBalancingProxyConfig("/e-capitalization", "/e-capitalization", "freme-ner");
		proxies.put(conf.getLocalEndpoint(), conf);
		
		return proxies;
	}
	
	@Bean
	public ServletRegistrationBean servletRegistrationBean(){
		
		HashMap<String,LoadBalancingProxyConfig> proxies = getProxyConfigurations();
		String[] urlMappings = new String[proxies.size()*2];
		int i=0;
		for( String url : proxies.keySet()){
			if( url.endsWith("/")){
				url = url.substring(0, url.length()-1);
			}
			urlMappings[i] = url;
			urlMappings[i*2+1] = url + "/";
		}
		
		return new ServletRegistrationBean(loadBalancerServlet, urlMappings);
	}
}
