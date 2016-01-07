package eu.freme.bservices.testhelper.api;

import java.util.HashMap;

import org.springframework.context.ConfigurableApplicationContext;

import eu.freme.common.starter.FREMEStarter;

/**
 * Setup integration tests across multiple test classes. Each class can call
 * IntegrationTestSetup.getContext(). The first starts the application context. All
 * integration tests can get the application context through
 * IntegrationTestSetup.
 * 
 * The class can register multiple application contexts. It stores all
 * application contexts in a hashmap with the path to the package as key.
 * 
 * @author Jan Nehring - jan.nehring@dfki.de
 */
public class IntegrationTestSetup {

	private static HashMap<String, ConfigurableApplicationContext> applicationContexts;

	public static ConfigurableApplicationContext getContext(String packagePath) {

		if (applicationContexts == null) {
			applicationContexts = new HashMap<String, ConfigurableApplicationContext>();
		}

		if (applicationContexts.containsKey(packagePath)) {
			return applicationContexts.get(packagePath);
		} else {
			ConfigurableApplicationContext applicationContext = FREMEStarter
					.startPackageFromClasspath(packagePath);
			applicationContexts.put(packagePath, applicationContext);
			return applicationContext;
		}
	}
}
