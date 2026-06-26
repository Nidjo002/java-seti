package com.seti.utils;

import com.seti.engine.action.AnalyzeAction;
import com.seti.engine.action.EndTurnAction;
import com.seti.engine.action.GameAction;
import com.seti.engine.action.LandAction;
import com.seti.engine.action.LaunchAction;
import com.seti.engine.action.MoveAction;
import com.seti.engine.action.OrbitAction;
import com.seti.engine.action.ScanAction;
import com.seti.engine.action.TradeAction;
import com.seti.engine.action.TradeDirection;
import com.seti.exception.XmlReplayException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class XmlUtils {

    private XmlUtils() {
    }

    private static final String REPLAY_FILE = "dat/replay.xml";

    private static final String ROOT = "Replay";
    private static final String ACTION = "Action";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_PLAYER = "player";
    private static final String ATTR_RING = "ring";
    private static final String ATTR_SECTOR = "sector";
    private static final String ATTR_DIRECTION = "direction";

    public static void saveReplay(List<ReplayEntryUtil> entries) {
        try {
            Document doc = newDocumentBuilder().newDocument();
            Element root = doc.createElement(ROOT);
            doc.appendChild(root);

            for (ReplayEntryUtil entry : entries) {
                root.appendChild(toElement(doc, entry));
            }

            writeDocument(doc);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new XmlReplayException("Failed to write replay XML file.", e);
        }
    }

    public static List<ReplayEntryUtil> readReplay() {
        List<ReplayEntryUtil> entries = new ArrayList<>();
        try {
            Document doc = newDocumentBuilder().parse(new File(REPLAY_FILE));
            NodeList nodes = doc.getDocumentElement().getElementsByTagName(ACTION);

            for (int i = 0; i < nodes.getLength(); i++) {
                entries.add(toEntry((Element) nodes.item(i)));
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XmlReplayException("Failed to read replay XML file.", e);
        }
        return entries;
    }

    private static Element toElement(Document doc, ReplayEntryUtil entry) {
        Element el = doc.createElement(ACTION);
        el.setAttribute(ATTR_PLAYER, String.valueOf(entry.playerIndex()));
        switch (entry.action()) {
            case LaunchAction _ -> el.setAttribute(ATTR_TYPE, "LAUNCH");
            case OrbitAction _ -> el.setAttribute(ATTR_TYPE, "ORBIT");
            case LandAction _ -> el.setAttribute(ATTR_TYPE, "LAND");
            case ScanAction _ -> el.setAttribute(ATTR_TYPE, "SCAN");
            case AnalyzeAction _ -> el.setAttribute(ATTR_TYPE, "ANALYZE");
            case EndTurnAction _ -> el.setAttribute(ATTR_TYPE, "END_TURN");
            case MoveAction move -> {
                el.setAttribute(ATTR_TYPE, "MOVE");
                el.setAttribute(ATTR_RING, String.valueOf(move.getTargetRing()));
                el.setAttribute(ATTR_SECTOR, String.valueOf(move.getTargetSector()));
            }
            case TradeAction trade -> {
                el.setAttribute(ATTR_TYPE, "TRADE");
                el.setAttribute(ATTR_DIRECTION, trade.getDirection().name());
            }
        }
        return el;
    }

    private static ReplayEntryUtil toEntry(Element el) {
        int player = Integer.parseInt(el.getAttribute(ATTR_PLAYER));
        String type = el.getAttribute(ATTR_TYPE);
        GameAction action = switch (type) {
            case "LAUNCH" -> new LaunchAction();
            case "ORBIT" -> new OrbitAction();
            case "LAND" -> new LandAction();
            case "SCAN" -> new ScanAction();
            case "ANALYZE" -> new AnalyzeAction();
            case "END_TURN" -> new EndTurnAction();
            case "MOVE" -> new MoveAction(
                    Integer.parseInt(el.getAttribute(ATTR_RING)),
                    Integer.parseInt(el.getAttribute(ATTR_SECTOR)));
            case "TRADE" -> new TradeAction(
                    TradeDirection.valueOf(el.getAttribute(ATTR_DIRECTION)));
            default -> throw new XmlReplayException("Unknown action type: " + type);
        };
        return new ReplayEntryUtil(player, action);
    }

    private static void writeDocument(Document doc) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newDefaultInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        File file = new File(REPLAY_FILE);
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        transformer.transform(new DOMSource(doc), new StreamResult(file));
    }

    private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return factory.newDocumentBuilder();
    }
}