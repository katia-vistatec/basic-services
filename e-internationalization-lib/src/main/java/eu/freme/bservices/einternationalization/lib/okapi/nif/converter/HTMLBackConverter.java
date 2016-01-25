/**
 * Copyright (C) 2015 Deutsches Forschungszentrum für Künstliche Intelligenz (http://freme-project.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.freme.bservices.einternationalization.lib.okapi.nif.converter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import eu.freme.bservices.einternationalization.lib.okapi.nif.filter.RDFConstants;
import eu.freme.bservices.einternationalization.lib.okapi.nif.its.ItsRdfConstants;

/**
 * Converts a NIF file back to the original format, when the NIF file was
 * created by converting a HTML file. If the NIF file has been enriched, then
 * the enrichments are added to the original file as well. Note that in order to
 * perform the back conversion, the skeleton NIF file is needed in addition to
 * the enriched NIF file.
 * 
 */
public class HTMLBackConverter {

	/** The URI offset string prefix. */
	public static final String URI_OFFSET_PREFIX = "#char=";

	/** The triple model. */
	private Model model;

	/**
	 * Default constructor.
	 */
	public HTMLBackConverter() {

	}

	public InputStream convertBack(final InputStream skeletonFile,
			final InputStream enrichedFile) {

		return convertBack(skeletonFile, enrichedFile,
				RDFConstants.RDFSerialization.TURTLE.toRDFLang(),
				RDFConstants.RDFSerialization.TURTLE.toRDFLang());
	}

	/**
	 * Performs the back conversion.
	 * 
	 * @param skeletonFile
	 *            the skeleton NIF file.
	 * @param enrichedFile
	 *            the enriched NIF file.
	 * @param skeletonFormat
	 *            the skeleton file serialization format.
	 * @param enrichedFormat
	 *            the enriched file serialization format.
	 * @return the input stream being the original HTML file.
	 */
	public InputStream convertBack(final InputStream skeletonFile,
			final InputStream enrichedFile, final String skeletonFormat,
			final String enrichedFormat) {

		Model skeletonModel = ModelFactory.createDefaultModel();
		CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
		decoder.onMalformedInput(CodingErrorAction.IGNORE);
		InputStreamReader reader = new InputStreamReader(skeletonFile, decoder);
		skeletonModel.read(reader, null, skeletonFormat);
		Model enrichedModel = ModelFactory.createDefaultModel();
		enrichedModel.read(enrichedFile, null, enrichedFormat);
		model = skeletonModel.union(enrichedModel);
		String originalFile = convertBack();
		return new ByteArrayInputStream(originalFile.getBytes());
	}

	/**
	 * Performs the back conversion.
	 * 
	 * @return the original file string
	 */
	private String convertBack() {

		StringBuilder originalFileString = new StringBuilder();
		String skeletonContext = findSkeletonContextString();
		if (skeletonContext != null) {
			int skeletonLastIdx = 0;
			List<TextUnitResource> tuResources = listTextUnitResources();
			for (TextUnitResource tuRes : tuResources) {
				if (checkWasConvertedFromAndSetOffset(tuRes)) {
					List<Statement> enrichmentStmts = findEnrichmentStatements(tuRes);
					if (!enrichmentStmts.isEmpty()) {
						// THE ANNOTATION MATCHES THE TEXT NODE
						String parentNode = skeletonContext
								.substring(skeletonLastIdx,
										tuRes.getWasConvFromStartIdx());
						if (parentNode.isEmpty()) {
							originalFileString.append("<span ");
							for (Statement entityStmt : enrichmentStmts) {
								originalFileString
										.append(getItsAnnotationString(entityStmt));
								originalFileString.append(" ");
							}
							originalFileString.append(">");
							originalFileString.append(tuRes.getText());
							skeletonLastIdx = tuRes.getWasConvFromEndIdx();
							originalFileString.append("</span>");
						} else {
							StringBuilder annotationAttributes = new StringBuilder();
							for (Statement entityStmt : enrichmentStmts) {
								String annotation = getItsAnnotationString(entityStmt);
								if (!parentNode.contains(annotation)) {
									annotationAttributes.append(" ");
									annotationAttributes.append(annotation);
								}
							}
							int closeTagIdx = parentNode.lastIndexOf(">");
							originalFileString.append(parentNode.substring(0,
									closeTagIdx));
							originalFileString.append(annotationAttributes);
							originalFileString.append(">");
							originalFileString.append(tuRes.getText());
							skeletonLastIdx = tuRes.getWasConvFromEndIdx();

						}

					} else {
						originalFileString
								.append(skeletonContext.substring(
										skeletonLastIdx,
										tuRes.getWasConvFromStartIdx()));
						originalFileString.append(tuRes.getText());
						skeletonLastIdx = tuRes.getWasConvFromEndIdx();
					}
				} else {
					// no was converted from
					List<Statement> entityStmts = findEnrichmentStatements(tuRes);
					if (!entityStmts.isEmpty()) {
						putAnnotationInTextUnitRes(tuRes, tuResources,
								entityStmts);
					}
				}
			}

			if (skeletonLastIdx < skeletonContext.length() - 1) {
				originalFileString.append(skeletonContext
						.substring(skeletonLastIdx));
			}
		}
		return originalFileString.toString();
	}

	/**
	 * Puts enrichment annotations into a text unit resource.
	 * 
	 * @param tuResource
	 *            the text unit resource.
	 * @param tuResList
	 *            the list of text unit resources.
	 * @param enrichmentStmts
	 *            the entity statements.
	 */
	private void putAnnotationInTextUnitRes(TextUnitResource tuResource,
			List<TextUnitResource> tuResList, List<Statement> enrichmentStmts) {

		int index = tuResList.indexOf(tuResource) + 1;
		boolean found = false;
		TextUnitResource currRes = null;
		while (index < tuResList.size() && !found) {
			currRes = tuResList.get(index);
			if (currRes.getStartIdx() <= tuResource.getStartIdx()
					&& currRes.getEndIdx() >= tuResource.getEndIdx()
					&& currRes.getText().contains(tuResource.getText())) {
				found = true;
			}
			index++;
		}
		if (found) {
			StringBuilder annotatedText = new StringBuilder();
			annotatedText.append("<span");
			for (Statement stmt : enrichmentStmts) {
				annotatedText.append(" ");
				annotatedText.append(getItsAnnotationString(stmt));
			}
			annotatedText.append(">");
			StringBuilder newText = new StringBuilder();
			newText.append(currRes.getText().substring(
					0,
					tuResource.getStartIdx() - currRes.getStartIdx()
							+ currRes.getAdditionalOffset()));
			newText.append(annotatedText);
			newText.append(tuResource.getText());
			newText.append("</span>");
			newText.append(currRes.getText().substring(
					tuResource.getEndIdx() - currRes.getStartIdx()
							+ currRes.getAdditionalOffset()));
			currRes.setText(newText.toString());
			currRes.setAdditionalOffset(currRes.getAdditionalOffset()
					+ annotatedText.length() + "</span>".length());
		}

	}

	/**
	 * Finds all the enrichment statements associated to a specific resource.
	 * 
	 * @param resource
	 *            the resource
	 * @return the list of enrichment statements.
	 */
	private List<Statement> findEnrichmentStatements(TextUnitResource resource) {

		List<Statement> enrichedStmts = new ArrayList<Statement>();
		enrichedStmts.addAll(findEntityStmts(resource));
		enrichedStmts.addAll(findTermStmts(resource));
		return enrichedStmts;
	}

	/**
	 * Finds all the terminology statements associated to a specific resource
	 * 
	 * @param resource
	 *            the resource
	 * @return the list of terminology statements.
	 */
	private List<Statement> findTermStmts(TextUnitResource resource) {

		List<Statement> termStmts = new ArrayList<Statement>();
		Property termProp = model.createProperty(RDFConstants.itsrdfPrefix,
				ItsRdfConstants.TERM_INFO);
		StmtIterator stmts = model.listStatements(resource.getResource(),
				termProp, (RDFNode) null);
		while (stmts.hasNext()) {
			termStmts.add(stmts.next());
		}
		return termStmts;
	}

	/**
	 * Finds all entity statements associated to a specific resource.
	 * 
	 * @param resource
	 *            the resource
	 * @return the list of entity statements.
	 */
	private List<Statement> findEntityStmts(TextUnitResource resource) {

		List<Statement> entityStmts = new ArrayList<Statement>();
		Property identRef = model.createProperty(RDFConstants.itsrdfPrefix,
				ItsRdfConstants.TA_IDENT_REF);
		Property classRef = model.createProperty(RDFConstants.itsrdfPrefix,
				ItsRdfConstants.TA_CLASS_REF);
		Property taConfidence = model.createProperty(RDFConstants.itsrdfPrefix,
				ItsRdfConstants.TA_CONFIDENCE);
		StmtIterator identRefStmts = model.listStatements(
				resource.getResource(), identRef, (RDFNode) null);
		while (identRefStmts.hasNext()) {
			entityStmts.add(identRefStmts.next());
		}
		StmtIterator classRefStmts = model.listStatements(
				resource.getResource(), classRef, (RDFNode) null);
		while (classRefStmts.hasNext()) {
			entityStmts.add(classRefStmts.next());
		}
		StmtIterator confidenceRefStmts = model.listStatements(
				resource.getResource(), taConfidence, (RDFNode) null);
		while (confidenceRefStmts.hasNext()) {
			entityStmts.add(confidenceRefStmts.next());
		}

		return entityStmts;
	}

	/**
	 * Retrieves the list of text unit resources from the triple model.
	 * 
	 * @return the list of text unit resources.
	 */
	private List<TextUnitResource> listTextUnitResources() {

		List<TextUnitResource> tuResources = new ArrayList<TextUnitResource>();
		Property anchorOfProp = model
				.createProperty(RDFConstants.ANCHOR_OF_PROP);
		StmtIterator anchorStmts = model.listStatements(null, anchorOfProp,
				(RDFNode) null);
		// ResIterator tuResIt = model.listResourcesWithProperty(anchorOfProp);
		Statement anchorStmt = null;
		TextUnitResource unitRes = null;
		while (anchorStmts.hasNext()) {
			anchorStmt = anchorStmts.next();
			unitRes = new TextUnitResource(anchorStmt.getSubject(), anchorStmt
					.getObject().asLiteral().getString());
			if (!tuResources.contains(unitRes)) {
				tuResources.add(unitRes);
			}
		}
		Collections.sort(tuResources, new TextUnitResComparator());
		return tuResources;
	}

	/**
	 * Retrieves the skeleton context string from the triple model.
	 * 
	 * @return the skeleton context string.
	 */
	private String findSkeletonContextString() {

		Property wasConvertedFromProp = model
				.createProperty(RDFConstants.WAS_CONVERTED_FROM_PROP);
		NodeIterator wasConvFromNodes = model
				.listObjectsOfProperty(wasConvertedFromProp);
		String skeletonContextString = null;
		if (wasConvFromNodes != null && wasConvFromNodes.hasNext()) {
			RDFNode node = wasConvFromNodes.next();
			String skeletonCtxtUriPrefix = node.asResource().getURI();
			int offsetIdx = skeletonCtxtUriPrefix.indexOf(URI_OFFSET_PREFIX);
			skeletonCtxtUriPrefix = skeletonCtxtUriPrefix.substring(0,
					offsetIdx);
			Property isStringProp = model
					.createProperty(RDFConstants.IS_STRING_PROP);
			StmtIterator isStrStmts = model.listStatements(null, isStringProp,
					(RDFNode) null);
			while (isStrStmts.hasNext() && skeletonContextString == null) {
				Statement stmt = isStrStmts.next();
				if (stmt.getSubject().getURI()
						.startsWith(skeletonCtxtUriPrefix)) {
					skeletonContextString = stmt.getObject().asLiteral()
							.getString();
				}
			}

		}

		return skeletonContextString;
	}

	/**
	 * Checks if the "wasConvertedFrom" property is defined for the resource
	 * passed as parameter. If it is the case, the
	 * <code>wasConvFromStartIdx</code> and the <code>wasConvFromEndIdx</code>
	 * are properly valued.
	 * 
	 * @param resource
	 *            the text unit resource.
	 * @return <code>true</code> if the "wasConvertedFrom" property is defined
	 *         for this resource; <code>false</code> otherwise.
	 */
	private boolean checkWasConvertedFromAndSetOffset(TextUnitResource resource) {

		boolean wasConvertedFromExists = false;
		Property wasConvertedFromProp = model
				.createProperty(RDFConstants.WAS_CONVERTED_FROM_PROP);
		// NodeIterator wasConvNodesIt = model.listObjectsOfProperty(
		// resource.getResource(), wasConvertedFromProp);
		StmtIterator wasConvertedStmts = model.listStatements(
				resource.getResource(), wasConvertedFromProp, (RDFNode) null);
		if (wasConvertedStmts != null && wasConvertedStmts.hasNext()) {
			Statement wasConvStmt = wasConvertedStmts.next();
			wasConvertedFromExists = true;
			String wasConvertedURI = wasConvStmt.getObject().asResource()
					.getURI();
			// String wasConvertedURI = wasConvNodesIt.next().asResource()
			// .getURI();
			String[] wasConvOffset = getOffsetFromURI(wasConvertedURI);
			resource.setWasConvFromStartIdx(Integer.valueOf(wasConvOffset[0]));
			resource.setWasConvFromEndIdx(Integer.valueOf(wasConvOffset[1]));
		}
		return wasConvertedFromExists;
	}

	/**
	 * Gets the offset from the URI string.
	 * 
	 * @param uri
	 *            the URI string.
	 * @return an array containing the start index and the end index of the
	 *         offset.
	 */
	private String[] getOffsetFromURI(String uri) {

		int startOffsetIdx = uri.indexOf(URI_OFFSET_PREFIX);
		return uri.substring(startOffsetIdx + URI_OFFSET_PREFIX.length())
				.split(",");
	}

	/**
	 * Gets the ITS annotation string (ITS attribute name = ITS attribute value)
	 * derived from the enrichment statement passed as parameter.
	 * 
	 * @param enrichmentStmt
	 *            the enrichment statement.
	 * @return the ITS annotation string.
	 */
	private String getItsAnnotationString(Statement enrichmentStmt) {
		StringBuilder itsAnnotation = new StringBuilder();
		itsAnnotation.append("its-");
		String itsPropName = enrichmentStmt.getPredicate().getLocalName();
		for (int i = 0; i < itsPropName.length(); i++) {
			if (Character.isUpperCase(itsPropName.charAt(i))) {
				itsAnnotation.append("-");
				itsAnnotation.append(Character.toLowerCase(itsPropName
						.charAt(i)));
			} else {
				itsAnnotation.append(itsPropName.charAt(i));
			}
		}
		itsAnnotation.append("=\"");
		if (enrichmentStmt.getObject().isResource()) {
			itsAnnotation.append(enrichmentStmt.getObject().asResource()
					.getURI());
		} else {
			itsAnnotation.append(enrichmentStmt.getObject().asLiteral()
					.getString());
		}
		itsAnnotation.append("\"");

		return itsAnnotation.toString();
	}

}

/**
 * Wrapper class for text unit resources.
 */
class TextUnitResource {

	/** The resource from the triple model. */
	private Resource resource;

	/** The text associated to the resource. */
	private String text;

	/** The start index in the plain text context. */
	private int startIdx;

	/** The end index in the plain text context. */
	private int endIdx;

	/** The start index in the skeleton context. */
	private int wasConvFromStartIdx;

	/** The end index in the skeleton context. */
	private int wasConvFromEndIdx;

	private int additionalOffset;

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            the resource.
	 * @param text
	 *            the text
	 */
	public TextUnitResource(Resource resource, String text) {

		this.resource = resource;
		this.text = text;
		int offsetIdx = resource.getURI().indexOf(
				HTMLBackConverter.URI_OFFSET_PREFIX);
		String[] offset = resource
				.getURI()
				.substring(
						offsetIdx
								+ HTMLBackConverter.URI_OFFSET_PREFIX.length())
				.split(",");
		startIdx = Integer.valueOf(offset[0]);
		endIdx = Integer.valueOf(offset[1]);
	}

	/**
	 * Gets the resource.
	 * 
	 * @return the resource.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Gets the start index in the plain text context.
	 * 
	 * @return the start index in the plain text context.
	 */
	public int getStartIdx() {
		return startIdx;
	}

	/**
	 * Gets the end index in the plain text context.
	 * 
	 * @return the end index in the plain text context.
	 */
	public int getEndIdx() {
		return endIdx;
	}

	/**
	 * Gets the text associated to the resource.
	 * 
	 * @return the text associated to the resource.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text associated to the resource.
	 * 
	 * @param text
	 *            the text associated to the resource.
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the start index in the skeleton context.
	 * 
	 * @return the start index in the skeleton context.
	 */
	public int getWasConvFromStartIdx() {
		return wasConvFromStartIdx;
	}

	/**
	 * Sets the start index in the skeleton context.
	 * 
	 * @param wasConvFromStartIdx
	 *            the start index in the skeleton context.
	 */
	public void setWasConvFromStartIdx(int wasConvFromStartIdx) {
		this.wasConvFromStartIdx = wasConvFromStartIdx;
	}

	/**
	 * Gets the end index in the skeleton context.
	 * 
	 * @return the end index in the skeleton context.
	 */
	public int getWasConvFromEndIdx() {
		return wasConvFromEndIdx;
	}

	/**
	 * Sets the end index in the skeleton context.
	 * 
	 * @param wasConvFromEndIdx
	 *            the end index in the skeleton context.
	 */
	public void setWasConvFromEndIdx(int wasConvFromEndIdx) {
		this.wasConvFromEndIdx = wasConvFromEndIdx;
	}

	public int getAdditionalOffset() {
		return additionalOffset;
	}

	public void setAdditionalOffset(int additionalOffset) {
		this.additionalOffset = additionalOffset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return text + "-" + startIdx + "," + endIdx;
	}

	@Override
	public boolean equals(Object obj) {

		boolean retValue = false;
		if (obj instanceof TextUnitResource) {
			TextUnitResource unit = (TextUnitResource) obj;
			retValue = text.equals(unit.getText())
					&& startIdx == unit.getStartIdx()
					&& endIdx == unit.getEndIdx();
		} else {
			retValue = super.equals(obj);
		}
		return retValue;
	}

	@Override
	public int hashCode() {
		return 31 * (text.hashCode() + startIdx + endIdx);
	}
}

/**
 * Comparator for text unit resources.
 */
class TextUnitResComparator implements Comparator<TextUnitResource> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(TextUnitResource o1, TextUnitResource o2) {

		int retValue = 0;
		if ((o1.getStartIdx() >= o2.getStartIdx() && o1.getEndIdx() <= o2
				.getEndIdx())) {
			retValue = -1;
		} else if ((o2.getStartIdx() >= o1.getStartIdx() && o2.getEndIdx() <= o1
				.getEndIdx())) {
			retValue = 1;
		} else if (o1.getStartIdx() < o2.getStartIdx()) {
			retValue = -1;
		} else if (o2.getStartIdx() < o1.getStartIdx()) {
			retValue = 1;
		}
		return retValue;
	}

}
