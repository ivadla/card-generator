import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGStylable;
import org.w3c.dom.svg.SVGTSpanElement;
import org.w3c.dom.svg.SVGTextElement;


public class TextCompositor {
	
	private String defaultLineStyle;
	
	private class NotEnoughLinesException extends Exception{
		private static final long serialVersionUID = 1L;
	}

	private CardCompositor cardCompositor;
	private CardTemplate template;

	public TextCompositor(CardCompositor cardCompositor, CardTemplate template) {
		this.cardCompositor = cardCompositor;
		this.template = template;
	}

	public void setCardText(SVGDocument document,
			List<TextLine> originalCardText, Double maxTextWidth)
			throws IOException {
		Element textNode = (SVGTextElement) document.getElementById("cardText");
		if(originalCardText == null || originalCardText.size() == 0) {
			if(textNode != null) {
				textNode.getParentNode().removeChild(textNode);
			}
			return;
		}
		
		defaultLineStyle = ((Element) textNode.getFirstChild())
				.getAttribute("style");
		
		List<TextLine> cardText = new ArrayList<TextLine>(originalCardText);
		int numberOfNeededTextLines = -1;

		while (numberOfNeededTextLines == -1) {
			try {
				numberOfNeededTextLines = placeText(maxTextWidth, cardText, textNode);
			} catch (NotEnoughLinesException e) {
				adjustLineNumbersAndHeight(textNode, textNode.getChildNodes().getLength() + 1);
			}
		}
		
		verticalAlignTextWhichHasFewerLines(textNode, numberOfNeededTextLines);
		
		removeUnneededCardTextLines(numberOfNeededTextLines, textNode);
		insertCoinImages(textNode);
		insertVictoryPointImages(textNode);
	}

	private void verticalAlignTextWhichHasFewerLines(Element textNode,
			int numberOfNeededTextLines) {
		if(textNode.getChildNodes().getLength() - numberOfNeededTextLines > 3) {
			Double newY = Double.parseDouble(textNode.getAttribute("y")) + calculateLineHeight(textNode.getChildNodes()) * (textNode.getChildNodes().getLength() - numberOfNeededTextLines -3) / 2.0;
			textNode.setAttribute("y", newY.toString());
			repositionLines(textNode, textNode.getChildNodes(), calculateLineHeight(textNode.getChildNodes()));
		}
	}
	
	private int placeText(Double maxTextWidth, List<TextLine> cardText,
			Element textNode) throws IOException, NotEnoughLinesException {
		int renderedLineNumber = 0;
		for (int currentOriginalLineNumber = 0; currentOriginalLineNumber < cardText
		.size(); currentOriginalLineNumber++) {
			TextLine currentTextLine = cardText.get(currentOriginalLineNumber);
			renderedLineNumber = placeTextLine((SVGTextElement) textNode, maxTextWidth,
					renderedLineNumber, currentTextLine);
			renderedLineNumber++;
		}
		return renderedLineNumber;
	}
	
	private int placeTextLine(SVGTextElement textNode, Double maxTextWidth,
			int currentLineNumber, TextLine currentTextLine) throws IOException, NotEnoughLinesException {
		SVGTSpanElement currentNode = (SVGTSpanElement) textNode
				.getChildNodes().item(currentLineNumber);

		// not enough lines
		if (currentNode == null) {
			throw new NotEnoughLinesException();
		}

		String lineText = currentTextLine.getText();
		
		if(currentTextLine.isSeparator()) {
			insertSeparatorLine(textNode, maxTextWidth, currentNode);
			lineText = "";
		}
		

		if (currentTextLine.isItalic()) {
			currentNode.setAttribute("font-style", "italic");
		}
		if(currentTextLine.isBold()) {
			makeBold(currentNode);
		}

		currentNode.setTextContent(lineText);
		if (currentNode.getComputedTextLength() > maxTextWidth) {
			int charactersThatFitInMaxTextWidth = 1;
			while (currentNode.getSubStringLength(0,
					charactersThatFitInMaxTextWidth + 1) < maxTextWidth) {
				charactersThatFitInMaxTextWidth++;
			}
			charactersThatFitInMaxTextWidth = lineText.lastIndexOf(" ",
					charactersThatFitInMaxTextWidth);
			TextLine restOfText = new TextLine(lineText
					.substring(charactersThatFitInMaxTextWidth + 1),
					currentTextLine.isItalic(), currentTextLine.isBold());
			currentNode.setTextContent(lineText.substring(0,
					charactersThatFitInMaxTextWidth));
			return placeTextLine(textNode, maxTextWidth, currentLineNumber + 1,
					restOfText);
		}
		return currentLineNumber;
	}

	private void insertSeparatorLine(SVGTextElement textNode,
			Double maxTextWidth, SVGTSpanElement currentNode) {
		
		// get an existing line if available and just move it
		Element separatorLine = textNode.getOwnerDocument().getElementById("separatorLine");
		if(separatorLine == null) {
			separatorLine = textNode.getOwnerDocument().createElement("line");
			separatorLine.setAttribute("id", "separatorLine");
			separatorLine.setAttribute("style", "stroke:#000000;stroke-width:1.5;stroke-linecap:round;stroke-opacity:1");
			textNode.getParentNode().appendChild(separatorLine);
		}
		Double x1 = textNode.getX().getBaseVal().getItem(0).getValue() - maxTextWidth * 0.4;
		Double x2 = textNode.getX().getBaseVal().getItem(0).getValue() + maxTextWidth * 0.4;
		separatorLine.setAttribute("x1", x1.toString());
		separatorLine.setAttribute("x2", x2.toString());
		separatorLine.setAttribute("y1", currentNode.getAttribute("y"));
		separatorLine.setAttribute("y2", currentNode.getAttribute("y"));
	}

	private void adjustLineNumbersAndHeight(Element textNode,
			int numberOfNeededTextLines) {
		NodeList lineNodes = textNode.getChildNodes();

		double lineHeight = calculateLineHeight(lineNodes);

		if (lineNodes.getLength() < numberOfNeededTextLines) {
			double newScale = adjustFontSize(textNode, ((double) lineNodes
					.getLength() / (double) numberOfNeededTextLines));
			lineHeight = lineHeight * newScale;
		}

		addCardTextLines(numberOfNeededTextLines, textNode, lineNodes,
				lineHeight);
		
		repositionLines(textNode, lineNodes, lineHeight);
	}

	private void repositionLines(Element textNode, NodeList lineNodes,
			double lineHeight) {
		Double lineYpos = Double.parseDouble(textNode.getAttribute("y"));
		for (int i = 0; i < lineNodes.getLength(); i++) {
			((Element)lineNodes.item(i)).setAttribute("y", lineYpos.toString());
			lineYpos += lineHeight;
		}
	}


	
	private void removeUnneededCardTextLines(int numberOfLines, Element textNode) {
		while (numberOfLines < textNode.getChildNodes().getLength()) {
			textNode.removeChild(textNode.getLastChild());
		}
	}

	private double calculateLineHeight(NodeList lineNodes) {
		double lineHeight = Double.parseDouble(((Element) lineNodes.item(1))
				.getAttribute("y"))
				- Double.parseDouble(((Element) lineNodes.item(0))
						.getAttribute("y"));
		return lineHeight;
	}
	
	private void addCardTextLines(int numberOfLines, Element textNode,
			NodeList lineNodes, Double lineHeight) {

		// create a new Node if we need more lines
		for (int currentLine = lineNodes.getLength(); currentLine < numberOfLines; currentLine++) {
			Element newLine = (Element) lineNodes.item(0).cloneNode(true);
			newLine.setAttribute("id", "line" + (currentLine + 1));
			newLine.setAttribute("style", defaultLineStyle);
			newLine.setTextContent("");
			textNode.appendChild(newLine);
		}

	}
	private double adjustFontSize(Element textNode, double scale) {
		Double originalFontSize = getFontsize(textNode);
		int newFontSize = (int) (originalFontSize * scale);
		setFontSize(textNode, (double)newFontSize);
		return ((double) newFontSize) / originalFontSize;
	}
	
	public static Double getFontsize(Node text) {
		String propertyValue = ((SVGStylable) text).getStyle()
				.getPropertyValue("font-size");
		return Double.parseDouble(propertyValue.substring(0,
				getPositionOfFirstNonNumericCharacter(propertyValue)));
	}
	
	public static void setFontSize(Element text, Double newFontSize) {
		String unit = getFontSizeUnit(text);
		((SVGStylable) text).getStyle().setProperty("font-size",
				newFontSize.toString() + unit, "");
	}

	public static String getFontSizeUnit(Node text) {
		String propertyValue = ((SVGStylable) text).getStyle()
		.getPropertyValue("font-size");
		return propertyValue.substring(getPositionOfFirstNonNumericCharacter(propertyValue));
	}

	private static int getPositionOfFirstNonNumericCharacter(
			String propertyValue) {
		Matcher matcher = Pattern.compile("[^0-9.]").matcher(propertyValue);
		matcher.find();
		int firstNonNumericChar = matcher.start();
		return firstNonNumericChar;
	}

	private void insertCoinImages(Element textNode) throws IOException {
		insertInlineImages(textNode, "\\$(.)\\$", "$1", template.getCoinImageFile(), true);
	}

	private void insertVictoryPointImages(Element textNode) throws IOException {
		// the replacement char is an en quad, a space which is as wide as an N.
		// This is necessary, because inkscape and batik disagree about the
		// width of several normal spaces put together
		insertInlineImages(textNode, "%VP%", "\u2000",
				template.getVictoryPointImageFile(), false);
	}

	private void insertInlineImages(Element textNode, String searchPattern, String replacement, String imageFile, boolean makeImageTextBold) throws IOException {
		
		NodeList lineNodes = textNode.getChildNodes();
		for (int i = 0; i < lineNodes.getLength(); i++) {
			SVGTSpanElement currentNode = (SVGTSpanElement) lineNodes.item(i);
			while (currentNode.getTextContent().matches(".*" + searchPattern + ".*")) {
				placeInlineImage(currentNode, searchPattern, replacement, imageFile, makeImageTextBold);
			}
		}
	}

	private void placeInlineImage(SVGTSpanElement lineNode, String searchPattern, String replacement, String imageFile, boolean makeImageTextBold) throws IOException {
		String lineText = lineNode.getTextContent();
		Matcher matcher = Pattern.compile(searchPattern).matcher(lineText);
		matcher.find();
		int imageValuePosition = matcher.start();
		lineText = matcher.replaceFirst(replacement);
		
		String textBeforeImage = lineText.substring(0, imageValuePosition);
		String imageText = lineText.substring(imageValuePosition,
				imageValuePosition + 1);
		String textAfterImage = lineText.substring(imageValuePosition + 1);
		if (textBeforeImage.length() > 0) {
			textBeforeImage = textBeforeImage + " ";
			imageValuePosition++;
		}
		if (textAfterImage.length() > 0) {
			textAfterImage = " " + textAfterImage;
		}
		lineText = textBeforeImage + imageText + textAfterImage;

		// to place the image correctly the node needs to contain
		// the correct text
		lineNode.setTextContent(lineText);

		SVGRect rect = lineNode.getExtentOfChar(imageValuePosition);
		Float x = rect.getX();
		Float y = rect.getY();
		Float oldWidth = rect.getWidth();
		Float oldHeight = rect.getHeight();

		// TODO: make the scale configurable
		float scale = 1.4F;

		Float height = oldHeight * scale;
		Float width = height;

		y -= (height - oldHeight) / 2;
		x -= (width - oldWidth) / 2;
		
		Node referenceNode;
		
		if(makeImageTextBold) {
			referenceNode = placeImageText(lineNode.getParentNode(),imageText, rect.getX() + oldWidth / 2, Float.parseFloat(lineNode.getAttribute("y")));
		} else {
			referenceNode = lineNode.getParentNode();
		}

		cardCompositor.embedImage(imageFile, referenceNode,
				(double) x, (double) y, (double) width, (double) height, true, false);

	}

	private Element placeImageText(Node referenceTextNode, String imageText,
			Float x, Float y) {
		Element imageTextNode = (Element) referenceTextNode.cloneNode(false);

		makeBold(imageTextNode);
		imageTextNode.setAttribute("x", x.toString());
		imageTextNode.setAttribute("y", y.toString());

		Element imageTextLine = imageTextNode.getOwnerDocument().createElement(
				"tspan");
		imageTextLine.setAttribute("x", x.toString());
		imageTextLine.setAttribute("y", y.toString());
		imageTextLine.setTextContent(imageText);

		imageTextNode.appendChild(imageTextLine);
		referenceTextNode.getParentNode().appendChild(imageTextNode);

		return imageTextNode;
	}

	private void makeBold(Element textNode) {
		((SVGStylable) textNode).getStyle().setProperty("font-weight",
				"600", "");
	}
	
	public void increaseHeightOfTextElement(Element textElement, Double newY) {
		Double lineHeight = calculateLineHeight(textElement.getChildNodes());
		Double oldY = Double.parseDouble(textElement.getAttribute("y"));
		int lines = (int) ((oldY - newY) / lineHeight)
				+ textElement.getChildNodes().getLength();

		textElement.setAttribute("y", newY.toString());

		addCardTextLines(lines, textElement, textElement.getChildNodes(),
				lineHeight);
		repositionLines(textElement, textElement.getChildNodes(), lineHeight);
	}

}
