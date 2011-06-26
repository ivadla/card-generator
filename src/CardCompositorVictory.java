import java.io.IOException;

import org.w3c.dom.svg.SVGDocument;


public class CardCompositorVictory extends CardCompositor {

	public CardCompositorVictory(CardTemplate template) {
		super(template);
	}
	
	@Override
	public void modify(SVGDocument document, CardParameters card,
			CardImage cardImage) throws IOException {
		if (document.getElementById("cardVictoryPointValue") != null && card.getVictoryPointValue() != null) {
			document.getElementById("cardVictoryPointValue").getFirstChild()
					.setTextContent(new Integer(card.getVictoryPointValue()).toString());
		}
		super.modify(document, card, cardImage);
	}

}
