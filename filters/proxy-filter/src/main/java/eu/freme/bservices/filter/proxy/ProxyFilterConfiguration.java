package eu.freme.bservices.filter.proxy;

import org.mitre.dsmiley.httpproxy.ProxyServlet;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class ProxyFilterConfiguration implements EnvironmentAware{

	  @Bean
	  public ServletRegistrationBean servletRegistrationBean(){
	    ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new ProxyServlet(), propertyResolver.getProperty("servlet_url"));
	    servletRegistrationBean.addInitParameter("targetUri", propertyResolver.getProperty("target_url"));
	    servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, propertyResolver.getProperty("logging_enabled", "false"));
	    return servletRegistrationBean;
	  }

	  private RelaxedPropertyResolver propertyResolver;

	  @Override
	  public void setEnvironment(Environment environment) {
	    this.propertyResolver = new RelaxedPropertyResolver(environment, "proxy.test.");
	  }
}
