/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.xml;

import hr.algebra.model.GameState;
import hr.algebra.model.Player;
import java.awt.Point;
import java.io.File;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import javafx.scene.control.Alert;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author zakesekresa
 */
public class DOM {

    public static void Write(List<GameState> moves) throws ParserConfigurationException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("moves");
        doc.appendChild(rootElement);
        for (GameState gamestate : moves) {
            Element gs = doc.createElement("gamestate");
            rootElement.appendChild(gs);

            Element player = doc.createElement("player");
            player.setTextContent(gamestate.getPlayerOnTurn().getName());
            gs.appendChild(player);

            Element burned = doc.createElement("burned");
            gs.appendChild(burned);

            for (Point match : gamestate.getBurnedMatches()) {
                Element matc = doc.createElement("match");
                burned.appendChild(matc);

                Element x = doc.createElement("x");
                x.setTextContent(String.valueOf(match.x));
                matc.appendChild(x);

                Element y = doc.createElement("y");
                y.setTextContent(String.valueOf(match.y));
                matc.appendChild(y);
            }

        }
        Transformer transformer
                = TransformerFactory.newInstance().newTransformer();

        Source xmlSource = new DOMSource(doc);
        String name = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss"));
        Result xmlResult = new StreamResult(new File(name + ".xml"));

        transformer.transform(xmlSource, xmlResult);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("XML created");
        alert.setContentText("XML replay created successfuly!");

        alert.showAndWait();
    }

    public static List<GameState> Read(File xmlFile) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList list = doc.getElementsByTagName("gamestate");
        List<GameState> gamestates = new ArrayList<>();

        for (int temp = 0; temp < list.getLength(); temp++) {

            Node node = list.item(temp);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                Element element = (Element) node;

                String player = element.getElementsByTagName("player").item(0).getTextContent();
                Element name = (Element) element.getElementsByTagName("burned").item(0);

                NodeList matches = name.getElementsByTagName("match");
                List<Point> matchesList = new ArrayList<>();

                for (int temp2 = 0; temp2 < matches.getLength(); temp2++) {

                    Node node2 = matches.item(temp2);
                    if (node2.getNodeType() == Node.ELEMENT_NODE) {

                        Element element2 = (Element) node2;

                        String x = element2.getElementsByTagName("x").item(0).getTextContent();
                        String y = element2.getElementsByTagName("y").item(0).getTextContent();

                        matchesList.add(new Point(Integer.valueOf(x), Integer.valueOf(y)));
                    }
                }
                gamestates.add(new GameState(matchesList, new Player(player)));

            }
        }
        return gamestates;

    }
}
