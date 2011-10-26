public class CardCompositorFactory {

	public static CardCompositor create(CardParameters card,
			CardTemplate template) {
		if (card.getTypeTemplate().equals("action-victory") || card.getTypeTemplate().equals("basic-victory") || card.getTypeTemplate().equals("victory-reaction")) {
			return new CardCompositorVictory(template);
		} else if (card.getTypeTemplate().equals("treasure") || card.getTypeTemplate().equals("basic-treasure") || card.getTypeTemplate().equals("treasure-reaction")) {
			return new CardCompositorTreasure(template);
		} else if (card.getTypeTemplate().equals("treasure-victory")) {
			return new CardCompositorTreasureVictory(template);
		} else {
			return new CardCompositor(template);
		}
	}

}
