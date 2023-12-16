package dataExtractions;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Cell {
	private String content;
	private List<String> citediIn = new ArrayList<String>();
	
	public String getContent() {
		return this.content;
	}
	public void setContent(String text) {
		this.content = text;
	}
	
	
	
	public List<String> getCitediIn() {
		return this.citediIn;
	}
	
	
	public void setCitedIn(String term, Document document) throws XPathExpressionException {
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();

		term = term.replaceAll("'", "");
		

		String xpathExpression = "//p[contains(.,'" + term + "')] | //p[contains(.,'" + term.toLowerCase() + "')] | "
				+ "//p[contains(.,'" + term.toUpperCase() + "')] ";
		XPathExpression expr = xpath.compile(xpathExpression);
		
		NodeList citationsNodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
		String extractedContent = "";
		String extractedContentNode = "";

		for(int i = 0; i<citationsNodes.getLength(); i++) {
			Node node = citationsNodes.item(i);
			if(node!=null){ 
				extractedContentNode = this.serializeNodeToString(node);
				extractedContent = extractedContentNode.replaceAll("<\\?xml.*\\?>", "");
				this.citediIn.add(extractedContent);
			}
		}

	}
	
	private String serializeNodeToString(Node node) {
		try {
			// Usa un Transformer per la serializzazione
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();

			// Configura l'output per indentare la stringa XML
			transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "no");

			// Crea un DOMSource dal nodo
			DOMSource source = new DOMSource(node);

			// Crea uno StringWriter per la stringa di output
			StringWriter stringWriter = new StringWriter();

			// Crea un StreamResult per la stringa di output
			StreamResult result = new StreamResult(stringWriter);

			// Esegue la trasformazione
			transformer.transform(source, result);

			// Restituisci la stringa risultante
			return stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
