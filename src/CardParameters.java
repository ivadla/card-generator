import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CardParameters extends XMLConfigReader {

	private String cardTitle;
	private String set;
	private String cardType;
	private String typeTemplate;
	private Integer cost;
	private Integer potionCost;
	private boolean costIsSpecial;
	private Integer coinValue;
	private Integer victoryPointValue;
	private List<TextLine> cardText;

	public String getCardTitle() {
		return cardTitle;
	}

	public String getSet() {
		return set;
	}

	public String getCardType() {
		return cardType;
	}

	public String getTypeTemplate() {
		return typeTemplate;
	}

	public int getCost() {
		return cost;
	}
	
	public Integer getPotionCost() {
		return potionCost;
	}
	
	public boolean isCostSpecial() {
		return costIsSpecial;
	}
	
	public Integer getCoinValue() {
		return coinValue;
	}

	public Integer getVictoryPointValue() {
		return victoryPointValue;
	}

	public List<TextLine> getCardText() {
		return cardText;
	}

	public CardParameters(String cardTitle,
			String set, String cardType, String typeTemplate, int cost, Integer potionCost,
			Integer coinValue, Integer victoryPointValue, List<TextLine> cardText)  {
		this.cardTitle = cardTitle;
		this.set = set;
		this.cardType = cardType;
		this.typeTemplate = typeTemplate;
		this.cost = cost;
		this.potionCost = potionCost;
		this.coinValue = coinValue;
		this.victoryPointValue = victoryPointValue;
		this.cardText = cardText;
	}

	public CardParameters(String cardConfigXML) throws SAXException, IOException, ParserConfigurationException {
		super(cardConfigXML);
	}

	@Override
	protected void readNode(Node node) {
		if (node.getNodeName().equals("name")) {
			this.cardTitle = node.getTextContent();
		} else if (node.getNodeName().equals("set")) {
			this.set = node.getTextContent();
		} else if (node.getNodeName().equals("type")) {
			this.cardType = node.getTextContent();
			this.typeTemplate = node.getAttributes().getNamedItem("typeTemplate").getTextContent();
		} else if (node.getNodeName().equals("cost")) {
			this.cost = Integer.parseInt(node.getTextContent());
		} else if (node.getNodeName().equals("potionCost")) {
			this.potionCost = Integer.parseInt(node.getTextContent());
		} else if (node.getNodeName().equals("costIsSpecial")) {
			this.costIsSpecial =  Boolean.parseBoolean(node.getTextContent());
		} else if (node.getNodeName().equals("coinValue")) {
			this.coinValue = Integer.parseInt(node.getTextContent());
		} else if (node.getNodeName().equals("victoryPointValue")) {
			this.victoryPointValue = Integer.parseInt(node.getTextContent());
		} else if (node.getNodeName().equals("text")) {
			readCardText(node);
		}
	}
	
	private void readCardText(Node node) {
		NodeList textNodes = node.getChildNodes();
		cardText = new ArrayList<TextLine>();

		for (int i = 0; i < textNodes.getLength(); i++) {
			Node textNode = textNodes.item(i);
			if (textNode.getNodeName().equals("line")) {
				TextLine line = new TextLine(textNode.getTextContent());
				if (textNode.hasAttributes()) {
					Node styleAttribute = textNode.getAttributes()
							.getNamedItem("style");
					if (styleAttribute != null) {
						if (styleAttribute.getTextContent().equals("italic")) {
							line.setItalic(true);
						} else if(styleAttribute.getTextContent().equals("bold")) {
							line.setBold(true);
						} else if(styleAttribute.getTextContent().equals("separator")) {
							line.setSeparator(true);
						}
					}
				}
				cardText.add(line);
			}
		}
	}
	
	public Document toXML(String filename) {
		Document xmlDocument = new DocumentImpl();
		Element root = xmlDocument.createElement("card");
		
		root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:noNamespaceSchemaLocation", "../Card.xsd");
		
		xmlDocument.appendChild(root);
		
		createElement(xmlDocument, "name", this.cardTitle);
		createElement(xmlDocument, "set", this.set);
		Element typeElement = createElement(xmlDocument, "type", this.cardType);
		typeElement.setAttribute("typeTemplate", this.typeTemplate);
		createElement(xmlDocument, "cost", this.cost.toString());
		if(this.potionCost != null && this.potionCost != 0) {
			createElement(xmlDocument, "potionCost", this.potionCost.toString());
		}
		Element textElement = createElement(xmlDocument, "text", null);
		for(TextLine line : this.cardText) {
			Element lineElement = createElement(xmlDocument, textElement, "line", line.getText());
			if(line.isItalic()) {
				lineElement.setAttribute("style", "italic");
			} else if(line.isSeparator()) {
				lineElement.setAttribute("style", "separator");
			}
		}
		return xmlDocument;
	}
	
	private Element createElement(Document xmlDocument, String elementName, String elementText) {
		return createElement(xmlDocument, xmlDocument.getDocumentElement(), elementName, elementText);
	}

	private Element createElement(Document xmlDocument, Element parent,
			String elementName, String elementText) {
		Element newElement = xmlDocument.createElement(elementName);
		newElement.appendChild(xmlDocument.createTextNode(elementText));
		parent.appendChild(newElement);
		return newElement;
	}
}
