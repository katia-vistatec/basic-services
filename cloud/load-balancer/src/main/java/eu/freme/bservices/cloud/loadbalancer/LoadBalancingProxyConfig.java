package eu.freme.bservices.cloud.loadbalancer;

public class LoadBalancingProxyConfig {

	String localEndpoint;
	String targetEndpoint;
	String serviceName;
	
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
