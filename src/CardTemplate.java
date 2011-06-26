import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class CardTemplate extends XMLConfigReader {

	private Double maxTextWidth;
	private String coinImageFile;
	private String victoryPointImageFile;
	private Map<String, String> templates;

	public Double getMaxTextWidth() {
		return maxTextWidth;
	}

	public String getCoinImageFile() {
		return this.baseDir + File.separator + coinImageFile;
	}

	public String getVictoryPointImageFile() {
		return this.baseDir + File.separator + victoryPointImageFile;
	}

	private Map<String, String> getTemplates() {
		if (templates == null) {
			templates = new HashMap<String, String>();
		}
		return templates;
	}

	public CardTemplate(String templateConfigXML) throws SAXException, IOException, ParserConfigurationException {
		super(templateConfigXML);
	}

	@Override
	protected void readNode(Node node) {
		if (node.getNodeName().equals("maxTextWidth")) {
			this.maxTextWidth = Double.parseDouble(node.getTextContent());
		} else if (node.getNodeName().equals("coinImage")) {
			this.coinImageFile = node.getTextContent();
		} else if (node.getNodeName().equals("victoryPointImage")) {
			this.victoryPointImageFile = node.getTextContent();
		} else if (node.getNodeName().equals("template")) {
			readCardTemplate(node);
		}
	}

	private void readCardTemplate(Node templateNode) {
		String type = templateNode.getAttributes().getNamedItem("type")
				.getTextContent();
		String fileName = templateNode.getTextContent();
		getTemplates().put(type, fileName);
	}

	public String getTemplateFile(String cardType) {
		if (this.getTemplates().containsKey(cardType)) {
			return this.baseDir + File.separator
					+ this.getTemplates().get(cardType);
		} else {
			return this.baseDir + File.separator
					+ this.getTemplates().get("action");
		}
	}
}
