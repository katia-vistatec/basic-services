package eu.freme.bservices.testhelper;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public abstract class AbstractTestHelper implements ApplicationContextAware{

	ApplicationContext context;

	Logger logger = Logger.getLogger(AbstractTestHelper.class);
	
	/**
	 * Returns the base url of the API given the spring application context, e.g. http://localhost:8080
	 * @return
	 */
	public String getAPIBaseUrl(){
		String port = context.getEnvironment().getProperty("server.port");
		if( port == null){
			port = "8080";
		}
		return "http://localhost:" + port;
	}
	
	/**
	 * Return the username of the administrator user of the REST API.
	 * 
	 * @param context
	 * @return
	 */
	public String getAdminUsername(){
		return context.getEnvironment().getProperty("admin.username");
	}
	
	/**
	 * Return the password of the administrator user of the REST API.
	 * 
	 * @param context
	 * @return
	 */
	public String getAdminPassword(){
		return context.getEnvironment().getProperty("admin.password");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.context = applicationContext;
		
	}

	//Reads a text file line by line. Use this when testing API with examples from /test/resources/
	public static String readFile(String file) throws IOException {
		StringBuilder bldr = new StringBuilder();
		for (String line: Files.readAllLines(Paths.get(file), StandardCharsets.UTF_8)) {
			bldr.append(line).append('\n');
		}
		return bldr.toString();
	}
}
