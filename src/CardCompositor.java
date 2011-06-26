import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.Base64EncoderStream;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGImageElement;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTSpanElement;

public class CardCompositor {

	private CardTemplate template;

	public CardTemplate getTemplate() {
		return this.template;
	}

	public CardCompositor(CardTemplate template) {
		this.template = template;
	}

	private SVGDocument parseTemplate(String fileName) throws IOException {
		SVGDocument document = (new SAXSVGDocumentFactory(
				XMLResourceDescriptor.getXMLParserClassName()))
				.createSVGDocument(new File(fileName).toURI().toASCIIString());
		initializeStyleParsing(document);
		return document;

	}

	/**
	 * this seems to be necessary to allow calls like
	 * ((SVGStylable)textNode).getStyle().getPropertyValue("font-size");
	 * Otherwise getStyle would raise a NullPointerException, because
	 * document.cssEngine is null
	 */
	private void initializeStyleParsing(SVGDocument document) {
		UserAgent userAgent;
		DocumentLoader loader;
		BridgeContext ctx;
		GVTBuilder builder;

		userAgent = new UserAgentAdapter();
		loader = new DocumentLoader(userAgent);
		ctx = new BridgeContext(userAgent, loader);
		ctx.setDynamicState(BridgeContext.DYNAMIC);
		builder = new GVTBuilder();
		builder.build(ctx, document);
	}

	private void save(Document document, String filename) throws IOException {
		FileWriter writer = new FileWriter(filename);
		DOMUtilities.writeDocument(document, writer);
		writer.flush();
		writer.close();
	}

	public void generateCard(String outputFile, CardParameters card,
			CardImage cardImage) throws IOException {
		SVGDocument document = parseTemplate(template.getTemplateFile(card
				.getTypeTemplate()));
		modify(document, card, cardImage);
		embedImages(document, template);
		save(document, outputFile);
	}

	public void modify(SVGDocument document, CardParameters card,
			CardImage cardImage) throws IOException {
		Node title = document.getElementById("cardTitle").getFirstChild();
		title.setTextContent(card.getCardTitle());
		document.getElementById("cardType").getFirstChild()
				.setTextContent(card.getCardType());
		setCardCost(document, card);

		new TextCompositor(this, template).setCardText(document,
				card.getCardText(), template.getMaxTextWidth());

		setImage(document, cardImage);
	}

	private void setCardCost(SVGDocument document, CardParameters card) {
		SVGTSpanElement costTextLine = (SVGTSpanElement) document.getElementById("cardCost").getFirstChild();
		costTextLine.setTextContent(
				new Integer(card.getCost()).toString());
		if(card.isCostSpecial()) {
			addAsteriskToNumber(costTextLine);
		}
	}

	private void addAsteriskToNumber(SVGTSpanElement costTextLine) {
		// get the bounding box of the digit which represents the cost
		SVGRect rect = costTextLine.getExtentOfChar(0);
		// x,y is the upper-left corner. This y is not the same as the Attribute y, because the Attribute y is the position of the baseline of a text
		Float x = rect.getX();
		Float y = rect.getY();	
		Float width = rect.getWidth();
		Float height = rect.getHeight();

		Element asteriskLine = costTextLine.getOwnerDocument().createElement("tspan");
		asteriskLine.setTextContent("*");
		
		// use a smaller font for the asterisk
		Double newFontSize = TextCompositor.getFontsize(costTextLine.getParentNode()) * 2 / 3;
		asteriskLine.setAttribute("style", "font-size:" + newFontSize.toString() + TextCompositor.getFontSizeUnit(costTextLine.getParentNode()));
		
		// move the digit a little to the left to make room for the asterisk
		Float newX = x - width * 0.2F;
		((Element)costTextLine.getParentNode()).setAttribute("x", newX.toString());
		costTextLine.setAttribute("x", newX.toString());
		
		
		// this y is the baseline of the text. We set the baseline to the middle of the digit
		// An * is positioned above the baseline, so it will be somewhere between the middle and the upper part of the digit
		asteriskLine.setAttribute("y", Float.toString(y + height / 2));
		// to get the digit and the asterisk very close together we let their bounding boxes overlap
		asteriskLine.setAttribute("x", Float.toString(newX + width * 0.8F));
		
		costTextLine.appendChild(asteriskLine);
	}

	private void setImage(Document document, CardImage cardImage)
			throws IOException {
		Element imagePlaceholder = document.getElementById("cardImage");
		if(cardImage == null) {
			if(imagePlaceholder != null) {
				imagePlaceholder.getParentNode().removeChild(imagePlaceholder);
			}
			return;
		}

		double x = Double.parseDouble(imagePlaceholder.getAttribute("x"));
		double y = Double.parseDouble(imagePlaceholder.getAttribute("y"));
		double width = Double.parseDouble(imagePlaceholder
				.getAttribute("width"));
		double height = Double.parseDouble(imagePlaceholder
				.getAttribute("height"));

		if (cardImage.getScale() != 0) {
			// width = width * cardImage.getScale();
			// only scale y for now to really center the image
			height = height * cardImage.getScale();
		} else { // old option
			if (cardImage.getWidth() > 0) {
				x += (width - cardImage.getWidth()) / 2;
				width = cardImage.getWidth();
			}

			if (cardImage.getHeight() > 0) {
				height = cardImage.getHeight();
			}
		}
		Node elementAboveImage = imagePlaceholder;
		if (cardImage.isBehindTextBackground()) {
			elementAboveImage = document.getElementById("textBackground");
		}
		Element image = embedImage(cardImage.getFileName(), elementAboveImage,
				x, y, width, height, true, true);
		imagePlaceholder.getParentNode().removeChild(imagePlaceholder);
		if (cardImage.getShadow() > 0) {
			addShadow(image, cardImage.getShadow());
		}
	}

	private void addShadow(Element image, Double shadowSize) {
		Document document = image.getOwnerDocument();
		Element filter = document.createElement("filter");
		filter.setAttribute("id", "dropShadow" + new Random().nextInt());
		filter.setAttribute("color-interpolation-filters", "sRGB");
		Element gaussianBlurFilter = document.createElement("feGaussianBlur");
		gaussianBlurFilter.setAttribute("in", "SourceAlpha");
		gaussianBlurFilter.setAttribute("stdDeviation", shadowSize.toString());
		gaussianBlurFilter.setAttribute("result", "blur");
		Element colorMatrixFilter = document.createElement("feColorMatrix");
		colorMatrixFilter.setAttribute("result", "bluralpha");
		colorMatrixFilter.setAttribute("type", "matrix");
		colorMatrixFilter.setAttribute("values",
				"1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 1 0");
		Element offsetFilter = document.createElement("feOffset");
		offsetFilter.setAttribute("in", "bluralpha");
		offsetFilter.setAttribute("dx", "2");
		offsetFilter.setAttribute("dy", "0");
		offsetFilter.setAttribute("result", "offsetBlur");

		Element merge = document.createElement("feMerge");
		Element mergeNode1 = document.createElement("feMergeNode");
		mergeNode1.setAttribute("in", "offsetBlur");
		Element mergeNode2 = document.createElement("feMergeNode");
		mergeNode2.setAttribute("in", "SourceGraphic");
		merge.appendChild(mergeNode1);
		merge.appendChild(mergeNode2);

		filter.appendChild(gaussianBlurFilter);
		filter.appendChild(colorMatrixFilter);
		filter.appendChild(offsetFilter);
		filter.appendChild(merge);

		document.getDocumentElement().getElementsByTagName("defs").item(0)
				.appendChild(filter);

		image.setAttribute("style", "filter:url(#" + filter.getAttribute("id")
				+ ")");
	}

	public Element embedImage(String fileName, Node referenceNode, Double x,
			Double y, Double width, Double height, Boolean preserveAspectRatio,
			Boolean center) throws IOException {
		if (fileName.endsWith(".svg")) {
			return embedSVGImage(fileName, referenceNode, x, y, width, height,
					preserveAspectRatio, center);
		} else {
			return embedBitmapImage(fileName, referenceNode, x, y, width,
					height, preserveAspectRatio, center);
		}
	}

	public Element embedSVGImage(String fileName, Node referenceNode, Double x,
			Double y, Double width, Double height, Boolean preserveAspectRatio,
			Boolean center) throws IOException {
		Element image = new SAXSVGDocumentFactory(
				XMLResourceDescriptor.getXMLParserClassName())
				.createSVGDocument(new File(fileName).toURI().toASCIIString())
				.getDocumentElement();

		Double imageWidth = parseSVGLength(image.getAttribute("width"));
		Double imageHeight = parseSVGLength(image.getAttribute("height"));
		Double xScale = width / imageWidth;
		Double yScale = height / imageHeight;

		if (preserveAspectRatio) {
			xScale = Math.min(xScale, yScale);
			yScale = xScale;
			if (center) {
				Double xDiff = width - imageWidth * xScale;
				Double yDiff = height - imageHeight * yScale;
				if (xDiff > 1) {
					x += xDiff / 2;
				}
				if (yDiff > 1) {
					y += yDiff / 2;
				}
			}
		}

		image = (Element) referenceNode.getOwnerDocument().importNode(image,
				true);

		Element imageGroup = referenceNode.getOwnerDocument()
				.createElement("g");
		imageGroup.setAttribute("transform",
				"matrix(" + xScale.toString() + ",0,0," + yScale.toString()
						+ "," + x.toString() + "," + y.toString() + ")");
		imageGroup.appendChild(image);

		referenceNode.getParentNode().insertBefore(imageGroup, referenceNode);
		return imageGroup;
	}

	public Element embedBitmapImage(String fileName, Node referenceNode,
			Double x, Double y, Double width, Double height,
			Boolean preserveAspectRatio, Boolean center) throws IOException {
		Element image = referenceNode.getOwnerDocument().createElement("image");

		image.setAttribute("x", x.toString());
		image.setAttribute("y", y.toString());
		image.setAttribute("width", width.toString());
		image.setAttribute("height", height.toString());
		image.setAttribute("preserveAspectRatio", "xMidYMid meet");

		String filetype;
		if (fileName.endsWith(".png")) {
			filetype = "image/png";
		} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			filetype = "image/jpeg";
		} else {
			throw new RuntimeException("Filetype of the file " + fileName
					+ " is not supported!");
		}
		image.setAttributeNS("http://www.w3.org/1999/xlink", "xlink:href",
				"data:" + filetype + ";base64," + base64Encode(fileName));
		referenceNode.getParentNode().insertBefore(image, referenceNode);
		return image;
	}

	private String base64Encode(String fileName) throws FileNotFoundException,
			IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		InputStream input = new FileInputStream(fileName);
		Base64EncoderStream output = new Base64EncoderStream(
				byteArrayOutputStream);

		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}

		output.close();
		String base64FileContent = byteArrayOutputStream.toString();
		return base64FileContent;
	}

	private Double parseSVGLength(String length) {
		if (length.endsWith("px")) {
			length = length.substring(0, length.length() - 2);
		}
		if (length.endsWith("pt")) {
			length = length.substring(0, length.length() - 2);
		}
		return Double.parseDouble(length);
	}

	private void embedImages(SVGDocument document, CardTemplate template)
			throws IOException {
		NodeList imageNodes = document.getElementsByTagName("image");
		for (int i = 0; i < imageNodes.getLength(); i++) {
			if (!isAlreadyEmbeded(imageNodes.item(i))) {
				SVGImageElement image = (SVGImageElement) imageNodes.item(i);
				double width = image.getWidth().getBaseVal().getValue();
				double height = image.getHeight().getBaseVal().getValue();
				double x = image.getX().getBaseVal().getValue();
				double y = image.getY().getBaseVal().getValue();
				String filename = image.getHref().getBaseVal();
				filename = template.getBaseDir() + File.separator + filename;

				Boolean preserveAspectRatio = true;

				if (image.getPreserveAspectRatio().getBaseVal().getAlign() == SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE) {
					preserveAspectRatio = false;
				}
				embedImage(filename, image, x, y, width, height,
						preserveAspectRatio, false);
				image.getParentNode().removeChild(image);
				// the nodelist is updated on the fly, therefore the next image
				// will occupy the position from the image we just removed
				i--;
			}
		}
	}

	private boolean isAlreadyEmbeded(Node imageNode) {
		Element imageElement = (Element) imageNode;
		String hrefAttribute = imageElement.getAttributeNS(
				"http://www.w3.org/1999/xlink", "href");
		return hrefAttribute.contains("base64");
	}

}
