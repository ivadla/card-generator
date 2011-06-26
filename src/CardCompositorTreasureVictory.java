import java.io.IOException;

import org.w3c.dom.svg.SVGDocument;

public class CardCompositorTreasureVictory extends CardCompositor {

	public CardCompositorTreasureVictory(CardTemplate template) {
		super(template);
	}

	@Override
	public void modify(SVGDocument document, CardParameters card,
			CardImage cardImage) throws IOException {
		if (document.getElementById("cardValue") != null) {
			document.getElementById("cardValue")
					.getFirstChild()
					.setTextContent(new Integer(card.getCoinValue()).toString());
		}
		if (document.getElementById("cardVictoryPointValue") != null) {
			document.getElementById("cardVictoryPointValue")
					.getFirstChild()
					.setTextContent(
							new Integer(card.getVictoryPointValue()).toString());
		}
		super.modify(document, card, cardImage);
	}
}
