import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QueryReader {

    /**
     * Determines the type of query based on its initial character.
     * Assumes 'P' indicates Variable Elimination (VE) and others indicate Bayes.
     *
     * @param query The query string to determine type for.
     * @return QueryType.VE if starts with 'P', otherwise QueryType.BAYES.
     */
    public static QueryType determineQueryType(String query) {
        return query.charAt(0) == 'P' ? QueryType.VE : QueryType.BAYES;
    }

    /**
     * Parses a Bayes Ball query to extract relevant variables and evidence.
     *
     * @param query The Bayes Ball query string.
     * @return A list containing variables and evidence extracted from the query.
     */
    public static List<String> parseBayesBallQuery(String query) {
        String[] halves = query.split("\\|");
        String[] firstSecond = halves[0].split("-");

        List<String> output = new ArrayList<>(Arrays.asList(firstSecond));

        if (halves.length > 1) {
            String[] evidence = halves[1].split(",");
            for (String s : evidence) {
                String[] keys = s.split("=");
                output.add(keys[0]);
            }
        }

        return output;
    }

    /**
     * Parses a Variable Elimination (VE) query to extract hypothesis and evidence.
     *
     * @param query The VE query string.
     * @return A list containing hypothesis and evidence extracted from the query.
     */
    public static List<String> parseVariableEliminationQuery(String query) {
        List<String> output = new ArrayList<>();
        String[] inQuery = query.split(" ");
        String inParentheses = inQuery[0].substring(2, inQuery[0].length() - 1);
        String[] halves = inParentheses.split("\\|");
        output.add(halves[0]);

        StringBuilder evidence = new StringBuilder();
        if (halves.length > 1) {
            String[] evidences = halves[1].split(",");
            for (int i = 0; i < evidences.length; i++) {
                evidence.append(evidences[i]);
                if (i != evidences.length - 1)
                    evidence.append(",");
            }
        }
        if (evidence.length() > 0) {
            output.add(evidence.toString());
        }
        return output;
    }

    /**
     * Parses the hidden variables part of a Variable Elimination (VE) query.
     *
     * @param query The VE query string.
     * @return A list containing hidden variables extracted from the query.
     */
    public static List<String> parseVariableEliminationQueryHidden(String query) {
        List<String> output = new ArrayList<>();
        String[] halves = query.split("\\|");
        String[] splitHidden = halves[1].split(" ");
        String[] hidden = splitHidden[1].split("-");
        Collections.addAll(output, hidden);
        return output;
    }
}
