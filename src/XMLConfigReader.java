import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public abstract class XMLConfigReader {

	protected String baseDir;
	
	public String getBaseDir() {
		return baseDir;
	}
	
	protected XMLConfigReader() {
		
	}

	public XMLConfigReader(String xmlConfigFileName) throws SAXException, IOException, ParserConfigurationException {
			read(xmlConfigFileName);
	}

	protected void read(String xmlConfigFileName) throws SAXException,
			IOException, ParserConfigurationException {
		this.baseDir = new File(xmlConfigFileName).getParent();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(xmlConfigFileName));
		NodeList nodes = doc.getDocumentElement().getChildNodes();
		for(int i = 0; i< nodes.getLength(); i++) {
			Node node = nodes.item(i);
			readNode(node);
		}
	}

	protected abstract void readNode(Node node);

}
