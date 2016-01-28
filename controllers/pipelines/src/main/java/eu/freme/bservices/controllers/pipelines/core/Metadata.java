package eu.freme.bservices.controllers.pipelines.core;

import java.util.Map;

/**
 * <p>Some metadata about the response. Only timings at this moment.</p>
 * <p>
 * <p>Copyright 2015 MMLab, UGent</p>
 *
 * @author Gerald Haesendonck
 */
public class Metadata {
	private final Map<String, Long> executionTime;
	private final long totalExecutionTime;

	public Metadata(Map<String, Long> executionTime, long totalExecutionTime) {
		this.executionTime = executionTime;
		this.totalExecutionTime = totalExecutionTime;
	}
}
