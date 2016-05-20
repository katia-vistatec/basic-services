package eu.freme.bservices.cloud.loadbalancer;

public class LoadBalancingProxyConfig {

	public enum PropertyName {
		LOCAL_ENDPOINT("local-endpoint"),
		TARGET_ENDPOINT("target-endpoint"),
		SERVICE_NAME("service-name");
		private String value;
		PropertyName(String key){
			this.value = key;
		}
		String getValue(){return value;}
	}

	String localEndpoint;
	String targetEndpoint;
	String serviceName;

	public LoadBalancingProxyConfig(){}

	public void setProperty(String propertyName, String value){
		if(PropertyName.LOCAL_ENDPOINT.getValue().equals(propertyName))
			localEndpoint = value;
		else if(PropertyName.TARGET_ENDPOINT.getValue().equals(propertyName))
			targetEndpoint = value;
		else if(PropertyName.SERVICE_NAME.getValue().equals(propertyName))
			serviceName = value;
		else{
			throw new RuntimeException("unknown parameter: "+ propertyName);
		}
	}

	public boolean isValid(){
		return localEndpoint!=null && targetEndpoint!=null && serviceName!=null;
	}

	public LoadBalancingProxyConfig(String localEndpoint,
			String targetEndpoint, String serviceName) {
		super();
		this.localEndpoint = localEndpoint;
		this.targetEndpoint = targetEndpoint;
		this.serviceName = serviceName;
	}
	public String getLocalEndpoint() {
		return localEndpoint;
	}
	public String getTargetEndpoint() {
		return targetEndpoint;
	}
	public String getServiceName() {
		return serviceName;
	}
}
