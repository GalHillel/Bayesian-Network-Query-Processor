import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Ex1 {

    // Constants for input/output file names and split mark
    private static final String INPUT_FILE_NAME = "input.txt";
    private static final String OUTPUT_FILE_NAME = "output.txt";
    private static final String SPLIT_MARK = "split_text";

    public static void main(String[] args) {
        List<Variable> variables = new ArrayList<>();
        StringBuilder queries = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE_NAME))) {
            String line;
            int lineCounter = 0;

            // Read lines from input file
            while ((line = br.readLine()) != null) {
                if (lineCounter == 0) {
                    // First line contains XML for variables
                    variables = loadVariablesFromXML(line);
                } else {
                    // Remaining lines are queries separated by SPLIT_MARK
                    queries.append(line).append(SPLIT_MARK);
                }
                lineCounter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Create BayesianNetwork object with loaded variables
        BayesianNetwork network = new BayesianNetwork(variables);

        // Process queries and get output
        StringBuilder output = processQueries(queries.toString(), network);

        // Save output to file and print to console
        saveOutputToFile(output.toString(), OUTPUT_FILE_NAME);
        System.out.println("Output:\n" + output);
    }

    // Method to load variables from XML string
    private static List<Variable> loadVariablesFromXML(String xmlLine) {
        Document document = XMLParser.parseXML(xmlLine);
        return XMLParser.buildVariables(document);
    }

    // Method to process all queries
    private static StringBuilder processQueries(String queries, BayesianNetwork network) {
        StringBuilder output = new StringBuilder();
        String[] splitQueries = queries.split(SPLIT_MARK);

        for (int i = 0; i < splitQueries.length; i++) {
            String q = splitQueries[i];
            // Determine type of query (BAYES or VE)
            if (QueryReader.determineQueryType(q).equals(QueryType.BAYES)) {
                // Process Bayes Ball query
                processBayesQuery(q, network, output);
            } else if (QueryReader.determineQueryType(q).equals(QueryType.VE)) {
                // Process Variable Elimination query
                processVariableEliminationQuery(q, network, output);
            }
            // Append newline if not the last query
            if (i != splitQueries.length - 1) {
                output.append("\n");
            }
        }
        return output;
    }

    // Method to process Bayes Ball query
    private static void processBayesQuery(String query, BayesianNetwork network, StringBuilder output) {
        List<String> ballVariables = QueryReader.parseBayesBallQuery(query);
        String firstVariable = ballVariables.get(0);
        String secondVariable = ballVariables.get(1);
        List<String> evidenceVariables = ballVariables.subList(2, ballVariables.size());
        // Determine if variables are independent using Bayesian Network
        boolean independents = network.bayesBall(firstVariable, secondVariable, evidenceVariables);
        // Append "yes" or "no" based on independence
        output.append(independents ? "yes" : "no");
    }

    // Method to process Variable Elimination query
    private static void processVariableEliminationQuery(String query, BayesianNetwork network, StringBuilder output) {
        List<String> eliminationVariables = QueryReader.parseVariableEliminationQuery(query);
        List<String> hidden = QueryReader.parseVariableEliminationQueryHidden(query);
        String hypothesis = eliminationVariables.get(0);
        List<String> evidence = eliminationVariables.subList(1, eliminationVariables.size());
        // Perform Variable Elimination and get results
        List<Double> veResult = network.variableElimination(hypothesis, evidence, hidden);
        // Format and append results to output
        output.append(UtilFunctions.roundFiveDecimalPlaces(veResult.get(0))).append(",");
        output.append((long) Math.floor(veResult.get(1))).append(",");
        output.append((long) Math.floor(veResult.get(2)));
    }

    // Method to save output to file
    private static void saveOutputToFile(String output, String fileName) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
