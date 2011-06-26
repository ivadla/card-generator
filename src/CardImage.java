import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class CardImage extends XMLConfigReader{
	
	private String fileName;
	private Double width = 0.0;
	private Double height = 0.0;
	private Double scale = 0.0;
	private Double shadow = 0.0;
	private boolean behindTextBackground = false;

	public CardImage(String xmlConfigFileName) throws SAXException, IOException, ParserConfigurationException {
		read(xmlConfigFileName);
	}

	protected void readNode(Node node) {
		if(node.getNodeName().equals("file")) {
			this.fileName = node.getTextContent();
		} else if(node.getNodeName().equals("width")) {
			this.width = Double.parseDouble(node.getTextContent());
		} else if(node.getNodeName().equals("height")) {
			this.height = Double.parseDouble(node.getTextContent());
		} else if(node.getNodeName().equals("scale")) {
			this.scale = Double.parseDouble(node.getTextContent());
		} else if(node.getNodeName().equals("shadow")) {
			this.shadow = Double.parseDouble(node.getTextContent());
		} else if(node.getNodeName().equals("behindTextBackground")) {
			this.behindTextBackground = Boolean.parseBoolean(node.getTextContent());
		}
	}

	public String getFileName() {
		return baseDir + File.separator + fileName;
	}

	public Double getWidth() {
		return width;
	}

	public Double getHeight() {
		return height;
	}
	public Double getScale() {
		return scale;
	}
	
	public Double getShadow() {
		return shadow;
	}

	public boolean isBehindTextBackground() {
		return behindTextBackground;
	}
	
}