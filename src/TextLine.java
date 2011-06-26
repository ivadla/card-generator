
public class TextLine {

	private String text;
	private boolean italic;
	private boolean bold;
	private boolean separator;
	
	public String getText() {
		return text;
	}
	public boolean isItalic() {
		return italic;
	}
	public void setItalic(boolean italic) {
		this.italic = italic;
	}
	public boolean isBold() {
		return bold;
	}
	public void setBold(boolean bold) {
		this.bold = bold;
	}
	public boolean isSeparator() {
		return separator;
	}
	public void setSeparator(boolean separator) {
		this.separator = separator;
	}
	public TextLine(String text) {
		this(text, false, false);
	}
	public TextLine(String text, boolean italic, boolean bold) {
		this.text = text;
		this.italic = italic;
		this.bold = bold;
		this.separator = false;
	}
}
