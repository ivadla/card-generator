import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ParseDiehrstraitsCom {

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public static void main(String[] args) throws IOException {
		String filename = "data/dominion.diehrstraits.com_withUnixNewlines.htm";
		new ParseDiehrstraitsCom().parseFile(filename, "output");
	}

	private void parseFile(String filename, String outputFolder)
			throws IOException {
		Scanner s = new Scanner(new FileInputStream(filename));
		while (s.hasNextLine()) {
			s.nextLine();
			if (s.findInLine("<tr class='\\w+'><td>\\d+</td><td><b><a href='[^']*'>([^<]*)</a></b></td><td><a href='[^']*'>([^<]*)</a></td><td>([^<]*)</td><td>\\$([^<]*)</td><td>(.*)</td></tr>") != null) {
				MatchResult result = s.match();
				String name = result.group(1);
				String set = result.group(2).toLowerCase();
				if (!set.equals("common")) {
					String type = result.group(3);
					String typeTemplate = getTypeTemplate(type);
					Integer cost = parseCoinCost(result.group(4));
					Integer potionCost = parsePotionCost(result.group(4));
					String text = result.group(5);
					Integer coinValue = parseCoinValue(text);
					Integer victoryPointValue = parseVictoryPointValue(text);
					List<TextLine> textlines = parseText(text);

					CardParameters card = new CardParameters(name, set, type,
							typeTemplate, cost, potionCost, coinValue,
							victoryPointValue, textlines);

					writeToFile(card, outputFolder);

				}
			}
		}
		s.close();
	}

	private void writeToFile(CardParameters card, String outputFolder)
			throws IOException {
		Document xmlDocument = card.toXML("output/test.xml");

		// include set name in the output folder
		String realOutputFolder = outputFolder + File.separator + "xml"
				+ File.separator + card.getSet();
		// create parent directories if necessary
		new File(realOutputFolder).mkdirs();

		String outputFileName = realOutputFolder + File.separator
				+ card.getCardTitle() + ".xml";

		FileOutputStream outputStream = new FileOutputStream(outputFileName
				.toLowerCase().replace("'", ""));
		OutputFormat outputFormat = new OutputFormat("XML", "UTF-8", true);
		outputFormat.setLineWidth(0);
		XMLSerializer serializer = new XMLSerializer(outputStream, outputFormat);
		serializer.asDOMSerializer().serialize(xmlDocument);
		outputStream.close();
	}

	private Integer parsePotionCost(String costString) {
		if (costString.contains("P")) {
			return 1;
		}
		return 0;
	}

	private Integer parseCoinCost(String costString) {
		return Integer.parseInt(costString.substring(0, 1));
	}

	private String getTypeTemplate(String type) {
		if (type.equals("Action - Victory")) {
			return "action-victory";
		} else if (type.equals("Action - Duration")) {
			return "duration";
		} else if (type.equals("Action - Reaction") || type.equals("Reaction")) {
			return "reaction";
		} else if (type.equals("Treasure - Victory")) {
			return "treasure-victory";
		} else if (type.equals("Treasure") || type.equals("Treasure - Prize")) {
			return "treasure";
		} else if (type.equals("Victory")) {
			return "victory";
		} else if (type.equals("Action") || type.equals("Action - Attack")
				|| type.equals("Action - Attack - Prize")
				|| type.equals("Action - Prize")) {
			return "action";
		}
		throw new RuntimeException("Unknown type: " + type);
	}

	private Integer parseVictoryPointValue(String text) {
		Matcher m = Pattern.compile("(.*, )?(\\d) Victory(,|\\.).*").matcher(
				text);
		if (m.matches()) {
			return Integer.parseInt(m.group(2));
		}
		m = Pattern.compile(".*<br />(\\d) VP").matcher(text);
		if (m.matches()) {
			return Integer.parseInt(m.group(1));
		}
		return null;
	}

	private List<TextLine> parseText(String text) {
		List<TextLine> lines = new ArrayList<TextLine>();
		for (String lineString : text.split("<br( /)?>")) {
			String realText = lineString.replaceAll("\\$([0-9]+)", "\\$$1\\$");
			TextLine line = new TextLine(realText);
			if (lineString.matches("[-_]+")) {
				line.setSeparator(true);
			}
			lines.add(line);
		}
		return lines;
	}

	private Integer parseCoinValue(String text) {

		Matcher m = Pattern.compile(".*Worth (\\d) Coins?\\..*").matcher(text);
		if (m.matches()) {
			return Integer.parseInt(m.group(1));
		}

		m = Pattern.compile(".*Worth \\$(\\d)\\..*").matcher(text);
		if (m.matches()) {
			return Integer.parseInt(m.group(1));
		}

		m = Pattern.compile("(\\d) Coins(<br|,).*").matcher(text);
		if (m.matches()) {
			return Integer.parseInt(m.group(1));
		}

		return null;
	}

}
