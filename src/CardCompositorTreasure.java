import java.io.IOException;

import org.w3c.dom.svg.SVGDocument;

public class CardCompositorTreasure extends CardCompositor {

	public CardCompositorTreasure(CardTemplate template) {
		super(template);
	}

	@Override
	public void modify(SVGDocument document, CardParameters card,
			CardImage cardImage) throws IOException {
		if (document.getElementById("cardValue") != null) {
			// if the treasure card has no fixed value, we remove the coin image and move the cardText 
			if (card.getCoinValue() == null) {
				new TextCompositor(this, getTemplate())
						.increaseHeightOfTextElement(
								document.getElementById("cardText"),
								Double.parseDouble(document.getElementById(
										"valueBackground").getAttribute("y")) * 1.2);
				document.getElementById("cardValue").getParentNode()
						.removeChild(document.getElementById("cardValue"));
				document.getElementById("valueBackground")
						.getParentNode()
						.removeChild(document.getElementById("valueBackground"));

			} else {
				document.getElementById("cardValue")
						.getFirstChild()
						.setTextContent(
								new Integer(card.getCoinValue()).toString());
			}
		}
		super.modify(document, card, cardImage);
	}

}
