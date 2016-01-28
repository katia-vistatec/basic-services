package eu.freme.bservices.controllers.pipelines.core;

import java.util.Map;

/**
 * <p>A PipelineResponse with some extra stats.</p>
 * <p>
 * <p>Copyright 2015 MMLab, UGent</p>
 *
 * @author Gerald Haesendonck
 */
public class WrappedPipelineResponse {
	private final Metadata metadata;
	private final PipelineResponse content;

	public WrappedPipelineResponse(PipelineResponse content, Map<String, Long> executionTime, long totalExecutionTime) {
		this.content = content;
		metadata = new Metadata(executionTime, totalExecutionTime);
	}

	@SuppressWarnings("unused")
	public PipelineResponse getContent() {
		return content;
	}
}
