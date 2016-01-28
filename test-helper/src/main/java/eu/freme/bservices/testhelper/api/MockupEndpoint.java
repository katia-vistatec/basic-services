package eu.freme.bservices.testhelper.api;

import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFSerializationFormats;
import eu.freme.common.exception.FileNotFoundException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

@RestController
public class MockupEndpoint {

	@Autowired
	RDFSerializationFormats rdfSerializationFormats;

	public static final String path = "/mockups/file";

	// use regEx to include the file extension
	@RequestMapping(path+"/{filename:.+}")
	public ResponseEntity<String> sendRDFfileContent(
			@RequestHeader( value="outformat", required=false) String outformat,
			@RequestHeader( value="accept", required=false) String accept,
			@PathVariable String filename

	) throws IOException{
		String fileContent;
		File file;
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			file = new File(classLoader.getResource("mockup-endpoint-data/" + filename.split("\\?")[0]).getFile());
			//File file = new File("src/main/resources/mockup-endpoint-data/"+filename);
			fileContent = FileUtils.readFileToString(file);
		}catch (Exception ex){
			throw new FileNotFoundException("could not load file: "+filename);
		}
		HttpHeaders headers = new HttpHeaders();

		// accept can contain a list
		String contentType = (outformat==null) ? ((accept == null) ? RDFConstants.RDFSerialization.TURTLE.contentType() : accept.split(",")[0]) : rdfSerializationFormats.get(outformat).contentType();
		headers.add("Content-Type", contentType);
		headers.add("content-length", file.length()+"");

		ResponseEntity<String> response = new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
		return response;
	}
}
