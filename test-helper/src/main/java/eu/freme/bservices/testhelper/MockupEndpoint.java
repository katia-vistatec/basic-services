package eu.freme.bservices.testhelper;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
public class MockupEndpoint {

	// use regEx to include the file extension
	@RequestMapping("/mockups/file/{filename:.+}")
	public ResponseEntity<String> sendRDFfileContent(
			@RequestHeader( value="outformat", required=false) String outformat,
			@RequestHeader( value="Content-Type", required=false) String contentType,
			@PathVariable String filename

	) throws IOException{

		File file = new File("src/test/resources/mockup-endpoint-data/"+filename);
		String fileContent = FileUtils.readFileToString(file);
		HttpHeaders headers = new HttpHeaders();

		contentType= (contentType == null) ? "text/turtle" : contentType;

		headers.add("Content-Type", contentType);
		outformat= (outformat == null) ? "turtle" : outformat;
		headers.add("outformat",outformat);

		ResponseEntity<String> response = new ResponseEntity<String>(fileContent, headers, HttpStatus.OK);
		return response;
	}
}
