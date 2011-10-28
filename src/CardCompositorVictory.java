import java.io.IOException;

import org.w3c.dom.svg.SVGDocument;

public class CardCompositorVictory extends CardCompositor {

	public CardCompositorVictory(CardTemplate template) {
		super(template);
	}

	@Override
	public void modify(SVGDocument document, CardParameters card, CardImage cardImage) throws IOException {
		if (document.getElementById("cardVictoryPointValue") != null) {
			if (card.getVictoryPointValue() != null) {
				document.getElementById("cardVictoryPointValue").getFirstChild().setTextContent(new Integer(card.getVictoryPointValue()).toString());
			} else {
				// if the victory card has no fixed value, we remove the VP image and move the cardText
				new TextCompositor(this, getTemplate()).increaseHeightOfTextElement(document.getElementById("cardText"),
						Double.parseDouble(document.getElementById("victoryPoints").getAttribute("y")) * 1.1);
				document.getElementById("cardVictoryPointValue").getParentNode().removeChild(document.getElementById("cardVictoryPointValue"));
				document.getElementById("victoryPoints").getParentNode().removeChild(document.getElementById("victoryPoints"));
				document.getElementById("separatorLine").getParentNode().removeChild(document.getElementById("separatorLine"));

			}
		}
		super.modify(document, card, cardImage);
	}

}
