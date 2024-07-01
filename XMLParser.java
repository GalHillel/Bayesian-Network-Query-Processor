import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMLParser {

    /**
     * Parses an XML file and returns the Document object representing it.
     *
     * @param fileName Name of the XML file to parse.
     * @return Document object representing the parsed XML.
     */
    public static Document parseXML(String fileName) {
        File inputFile = new File(fileName);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = null;
        try {
            if (builder == null) {
                throw new IOException("DocumentBuilder not initialized.");
            }
            doc = builder.parse(inputFile);
        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }

        return doc;
    }

    /**
     * Builds a list of Variable objects from the parsed XML Document.
     *
     * @param doc Parsed Document object representing the XML.
     * @return List of Variable objects parsed from the XML.
     */
    public static List<Variable> buildVariables(Document doc) {
        List<String> names = new ArrayList<>();
        List<List<String>> outcomes = new ArrayList<>();
        HashMap<String, List<Variable>> parents = new HashMap<>();
        HashMap<String, List<Double>> values = new HashMap<>();

        // Extract VARIABLE elements from XML
        NodeList variableList = doc.getDocumentElement().getElementsByTagName("VARIABLE");

        // Process each VARIABLE element
        for (int i = 0; i < variableList.getLength(); i++) {
            Node variableNode = variableList.item(i);
            String name = "";
            List<String> outcome = new ArrayList<>();

            if (variableNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList inner = variableNode.getChildNodes();
                for (int j = 0; j < inner.getLength(); j++) {
                    Node innerNode = inner.item(j);

                    // Extract NAME and OUTCOME elements
                    if (innerNode.getNodeName().equals("NAME")) {
                        name = innerNode.getTextContent();
                    } else if (innerNode.getNodeName().equals("OUTCOME")) {
                        NodeList node_outcomes = innerNode.getChildNodes();
                        for (int k = 0; k < node_outcomes.getLength(); k++) {
                            outcome.add(node_outcomes.item(k).getTextContent());
                        }
                    }
                }
            }
            names.add(name);
            outcomes.add(outcome);
        }

        // Create Variable objects and store them in a HashMap for lookup
        HashMap<String, Variable> variableHashMap = new HashMap<>();
        for (int i = 0; i < variableList.getLength(); i++) {
            variableHashMap.put(names.get(i), new Variable(names.get(i), outcomes.get(i)));
        }

        // Extract DEFINITION elements from XML
        NodeList definitionList = doc.getDocumentElement().getElementsByTagName("DEFINITION");

        // Process each DEFINITION element
        for (int i = 0; i < definitionList.getLength(); i++) {
            Node definitionNode = definitionList.item(i);
            String name = "";
            List<Variable> variableParents = new ArrayList<>();
            String table = "";

            if (definitionNode.getNodeType() == Node.ELEMENT_NODE) {
                NodeList inner = definitionNode.getChildNodes();
                for (int j = 0; j < inner.getLength(); j++) {
                    Node innerNode = inner.item(j);

                    // Extract FOR, GIVEN, and TABLE elements
                    switch (innerNode.getNodeName()) {
                        case "FOR":
                            name = innerNode.getTextContent();
                            break;
                        case "GIVEN":
                            NodeList nodeParents = innerNode.getChildNodes();
                            for (int k = 0; k < nodeParents.getLength(); k++) {
                                variableParents.add(variableHashMap.get(nodeParents.item(k).getTextContent()));
                            }
                            break;
                        case "TABLE":
                            table = innerNode.getTextContent();
                            break;
                    }
                }
            }
            parents.put(name, variableParents);
            values.put(name, parseTableValues(table));
        }

        // Create Variable objects with initialized parents and CPTs
        List<Variable> variables = new ArrayList<>();
        variableHashMap.forEach((key, value) -> {
            List<Double> tableValues = values.get(key);
            List<Variable> parentVariables = parents.get(key);

            // Convert list of Double to array of double
            double[] valuesArray = new double[tableValues.size()];
            for (int idx = 0; idx < valuesArray.length; idx++) {
                valuesArray[idx] = tableValues.get(idx);
            }

            // Convert list of Variable to array of Variable
            Variable[] parentArray = new Variable[parentVariables.size()];
            for (int idx = 0; idx < parentArray.length; idx++) {
                parentArray[idx] = parentVariables.get(idx);
            }

            // Initialize the Variable object with its parents and CPT
            value.initializeParents(valuesArray, parentArray);
            variables.add(value);
        });

        return variables;
    }

    /**
     * Parses a string representing table values into a list of Doubles.
     *
     * @param tableLine String representing table values.
     * @return List of Doubles parsed from the table string.
     */
    private static List<Double> parseTableValues(String tableLine) {
        String[] splitLine = tableLine.split(" ");
        List<Double> result = new ArrayList<>();
        for (String value : splitLine) {
            result.add(Double.parseDouble(value));
        }
        return result;
    }
}
