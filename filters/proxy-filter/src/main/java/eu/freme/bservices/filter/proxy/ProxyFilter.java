//package eu.freme.bservices.filter.proxy;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.bind.RelaxedPropertyResolver;
//import org.springframework.context.EnvironmentAware;
//import org.springframework.core.env.Environment;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.GenericFilterBean;
//
//import com.mashape.unirest.request.HttpRequest;
//
//@Component
//public class ProxyFilter extends GenericFilterBean implements EnvironmentAware {
//
//	HashMap<String, String> proxies = new HashMap<String, String>();
//
//	Logger logger = Logger.getLogger(ProxyFilter.class);
//
//	/**
//	 * proxy the request when it matches the proxies map
//	 */
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response,
//			FilterChain chain) throws IOException, ServletException {
//
//		if (!(request instanceof HttpServletRequest)
//				|| !(response instanceof HttpServletResponse)) {
//			chain.doFilter(request, response);
//		}
//
//		HttpServletRequest httpReq = (HttpServletRequest) request;
//		if (!proxies.containsKey(httpReq.getRequestURI())) {
//			chain.doFilter(request, response);
//		} else {
//			HttpRequest proxy = ProxyService.createProxy(httpReq,
//					proxies.get(httpReq.getRequestURI()));
//			ProxyResponseWrapper prw = new ProxyResponseWrapper(
//					(HttpServletResponse) response, proxy);
//
//		}
//	}
//
//	/**
//	 * parse proxy configuration properties
//	 */
//	@Override
//	public void setEnvironment(Environment environment) {
//		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(
//				environment);
//		Map<String, Object> map = propertyResolver.getSubProperties("proxy.");
//
//		class Proxy {
//			String servletUrl;
//			String targetUrl;
//		}
//
//		HashMap<String, Proxy> tempProxies = new HashMap<String, Proxy>();
//
//		for (String key : map.keySet()) {
//			String[] parts = key.split("\\.");
//			if (parts.length != 2) {
//				throw new RuntimeException("bad parameter: \"proxy." + key
//						+ "\"");
//			}
//
//			if (!tempProxies.containsKey(parts[0])) {
//				tempProxies.put(parts[0], new Proxy());
//			}
//
//			if (parts[1].equals("servlet_url")) {
//				tempProxies.get(parts[0]).servletUrl = (String) map.get(key);
//			} else if (parts[1].equals("target_url")) {
//				tempProxies.get(parts[0]).targetUrl = (String) map.get(key);
//			}
//		}
//
//		for (String key : tempProxies.keySet()) {
//			Proxy proxy = tempProxies.get(key);
//			if (proxy.servletUrl == null || proxy.targetUrl == null) {
//				throw new RuntimeException(
//						"Bad parameter configuration for proxy \"" + key + "\"");
//			}
//			proxies.put(proxy.servletUrl, proxy.targetUrl);
//		}
//	}
//}
