import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public class CreateSVGTest {

	public void paint(Graphics2D g) {
		g.setPaint(Color.red);
		g.fill(new Rectangle(10, 10, 100, 100));
		//g.drawS
		JTextArea n = new JTextArea( "test string\n next line" );
		n.setFont(new Font("Verdana", Font.BOLD, 20));
		n.setWrapStyleWord( true );
		n.setLineWrap( true );
		n.setForeground( Color.blue );
		n.setBounds( 90, 90 , 50, 50 );
		n.setOpaque( false );
		g.setPaint(Color.blue);
		n.paint(g);
	}

	public static void main(String[] args) throws IOException {

		// Get a DOMImplementation.
		DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();

		// Create an instance of org.w3c.dom.Document.
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		SVGDocument document = (SVGDocument) domImpl.createDocument(svgNS, "svg", null);

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

		// Ask the test to render into the SVG Graphics2D implementation.
		CreateSVGTest test = new CreateSVGTest();
		test.paint(svgGenerator);
		
		//svgGenerator.drawI

		 // Populate the document root with the generated SVG content.
//        Element root = (Element) document.getDocumentElement();
//        svgGenerator.getRoot(root);
		
		// Finally, stream out SVG to the standard output using
		// UTF-8 encoding.
		boolean useCSS = true; // we want to use CSS style attributes
		Writer out = new FileWriter("drawTest.svg");
		svgGenerator.stream(out, useCSS);
		out = new OutputStreamWriter(System.out, "UTF-8");
		svgGenerator.stream(out, useCSS);
		
        // Display the document.
        JSVGCanvas canvas = new JSVGCanvas();
        JFrame f = new JFrame();
        f.getContentPane().add(canvas);
        canvas.setSVGDocument(document);
        f.pack();
        f.setVisible(true);
	}
}
