package eu.freme.bservices.filter.proxy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

@Configuration
public class ProxyConfiguration {

	@Autowired
	ProxyService proxyService;

	@Autowired
	ProxyController proxyServlet;

	@Bean
	public SimpleUrlHandlerMapping registerProxyController() {
		SimpleUrlHandlerMapping suhm = new SimpleUrlHandlerMapping();
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> proxies = proxyService.getProxies();
		for (String url : proxies.keySet()) {
			map.put(url, "ProxyController");
		}
		suhm.setUrlMap(map);
		return suhm;
	}
}
