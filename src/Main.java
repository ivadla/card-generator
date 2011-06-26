import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main {

	String templateLocation;
	String imageLocation;
	String outputFolder;

	public Main(String templateFolder, String imageFolder, String outputFolder) {
		this.templateLocation = templateFolder;
		this.imageLocation = imageFolder;
		this.outputFolder = outputFolder;
	}

	public static void main(String[] args) {
		if(args.length > 3) {
			//Main main = new Main("data/templates/openclipart/config.xml", "data/images/openclipart", "output");
			Main main = new Main(args[0], args[1], args[2]);
			for(int i = 3; i < args.length; i++) { 
				main.generateCards(args[i]);
			}
		} else {
			System.out.println("Usage: java -jar card-generator.jar <template directory> <images directory> <output directory> <card files or directories>");
		}

	}
	
	public void generateCards(String path) {
		File pathFile = new File(path); 
		if(!pathFile.exists()) {
			System.err.println("Error: \"" + path + "\" does not exist!");
			System.exit(1);
		}
		if(pathFile.isDirectory()) {
			System.out.println("Looking for card files in directory " + path + " ...");
			String[] fileList = pathFile.list();
			Arrays.sort(fileList);
			for (String filename : fileList) {
				generateCards(path + File.separator + filename);
			}
		} else if(pathFile.isFile()) {
			if (path.endsWith(".xml")) {
				System.out.print("Processing card \"" + path + "\"...");
				try {
					generateCard(path);
					System.out.println("done");
				} catch (Exception e) {
					System.err.println("Aborted processing, because of Error: ");
					e.printStackTrace();
				}
			} else {
				System.out.println("Ignoring \"" +  path + "\", because it is not a XML File.");
			}
		} else {
			System.out.println("Ignoring file of unkown type " + path);
		}
	}

	public boolean imageConfigExists(String set, String cardName) {
		return new File(this.imageLocation + File.separator + set + File.separator + cardName + ".xml").exists();
	}
	
	public void generateCard(String cardXMLFile) throws SAXException, IOException, ParserConfigurationException {
		CardParameters card = new CardParameters(cardXMLFile);
		CardCompositor comp =  CardCompositorFactory.create(card, new CardTemplate(
				getTemplateConfigFileName()));
		CardImage cardImage = getCardImage(card) ;
		comp.generateCard(getOutputFileName(card.getSet(), card.getCardTitle(), "svg"),
				card, cardImage);
	}

	private String getTemplateConfigFileName() {
		if(! new File(templateLocation).exists()) {
			System.err.println(templateLocation + " does not exist! Aborting");
			System.exit(1);
		}
		if(templateLocation.endsWith(".xml")) {
			return templateLocation;
		}
		if(new File(templateLocation + File.separator + "config.xml").exists()) {
			return templateLocation + File.separator + "config.xml";
		}
		if(new File(templateLocation + File.separator + "template" + File.separator + "config.xml").exists()) {
			return templateLocation + File.separator + "template" + File.separator + "config.xml";
		}
		if(new File(templateLocation + File.separator + "templates" + File.separator + "config.xml").exists()) {
			return templateLocation + File.separator + "templates" + File.separator + "config.xml";
		}
		System.err.println("No template found at \"" + templateLocation + "\" Aborting");
		System.exit(1);
		return null;
	}
	private CardImage getCardImage(CardParameters card) throws SAXException, IOException, ParserConfigurationException {
		String cardImageXMLFilename = getCardImageXMLFilename(card);
		if(cardImageXMLFilename == null) {
			return null;
		}
		System.out.print(" using cardimage configuration \"" + cardImageXMLFilename + "\" ...");
		return new CardImage(cardImageXMLFilename);
	}
	
	private String getCardImageXMLFilename(CardParameters card) throws SAXException, IOException, ParserConfigurationException {
		String cardName = card.getCardTitle().toLowerCase();
		if(! new File(imageLocation).exists()) {
			System.err.println(imageLocation + " does not exist! No image will be used.");
			return null;
		}
		if(imageLocation.endsWith(".xml")) {
			return imageLocation;
		} 
		if(new File(imageLocation).isDirectory()) {
			if(new File(imageLocation + File.separator + cardName + ".xml").exists()) {
				return imageLocation + File.separator + cardName + ".xml";
			}
			if(new File(imageLocation + File.separator + card.getSet()).exists() && new File(imageLocation + File.separator + card.getSet()).isDirectory()) {
				if(new File(imageLocation + File.separator + card.getSet() + File.separator + cardName + ".xml").exists()) {
					return imageLocation + File.separator + card.getSet() + File.separator + cardName + ".xml";
				}
			}
		}
		System.err.println("No image found for the card " + cardName + " from set " + card.getSet() + " at \"" + imageLocation + "\"! No image will be used.");
		return null;
	}

	private String getOutputFileName(String cardSet, String cardName,
			String fileExtension) {
		String realOutputFolder = outputFolder + File.separator + fileExtension
				+ File.separator + cardSet;
		new File(realOutputFolder).mkdirs();
		String outputFileName = realOutputFolder + File.separator + cardName.toLowerCase()
				+ "." + fileExtension;
		return outputFileName;
	}
}
