import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class UtilFunctions {

    /**
     * Converts a LinkedHashMap to a string representation.
     *
     * @param hashmap The LinkedHashMap to convert.
     * @param <K>     Type of keys in the map.
     * @param <V>     Type of values in the map.
     * @return String representation of the map.
     */
    public static <K, V> String hashMapToString(LinkedHashMap<K, V> hashmap) {
        if (hashmap.isEmpty())
            return "";
        StringBuilder output = new StringBuilder();
        hashmap.forEach((key, value) -> {
            output.append(key);
            output.append(" : ");
            output.append(value);
            output.append("\n");
        });
        return output.toString();
    }

    /**
     * Splits keys in the format "variable=outcome" into a map.
     *
     * @param keys The keys string to split.
     * @return A LinkedHashMap where keys are variables and values are outcomes.
     */
    public static LinkedHashMap<String, String> splitKeysToVariablesAndOutcomes(String keys) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        String[] keys_split = keys.split(",");
        for (String key : keys_split) {
            String[] key_split = key.split("=");
            result.put(key_split[0], key_split[1]);
        }
        return result;
    }

    /**
     * Performs union of two lists, removing duplicates.
     *
     * @param X   First list.
     * @param Y   Second list.
     * @param <T> Type of elements in the lists.
     * @return A list containing elements from both lists, without duplicates.
     */
    public static <T> List<T> union(List<T> X, List<T> Y) {
        Set<T> result = new HashSet<>();
        result.addAll(X);
        result.addAll(Y);
        return new ArrayList<>(result);
    }

    /**
     * Performs intersection of two lists.
     *
     * @param X   First list.
     * @param Y   Second list.
     * @param <T> Type of elements in the lists.
     * @return A list containing common elements from both lists.
     */
    public static <T> List<T> intersection(List<T> X, List<T> Y) {
        List<T> result = new ArrayList<>();
        if (X.isEmpty() || Y.isEmpty())
            return result;
        for (T x : X) {
            if (Y.contains(x)) {
                result.add(x);
            }
        }
        return result;
    }

    /**
     * Combines a list of strings into a single string separated by commas.
     *
     * @param list The list of strings to combine.
     * @return A string with elements from the list separated by commas.
     */
    public static String combineWithCommas(List<String> list) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            result.append(list.get(i));
            if (i != list.size() - 1)
                result.append(",");
        }
        return result.toString();
    }

    /**
     * Splits a string by commas into a list of strings.
     *
     * @param string The string to split.
     * @return A list of strings split by commas.
     */
    public static List<String> separateByCommas(String string) {
        return new ArrayList<>(Arrays.asList(string.split(",")));
    }

    /**
     * Fixes duplicate values in keys of a factor by removing unnecessary
     * duplicates.
     *
     * @param factor The factor to fix.
     * @return A LinkedHashMap with fixed keys.
     */
    public static LinkedHashMap<String, Double> fixingDuplicatesValuesInKeys(LinkedHashMap<String, Double> factor) {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        if (factor.size() == 1 && CPT.getFactorNames(factor).size() == 1) {
            return factor;
        }

        LinkedHashMap<String, List<String>> outcomes = CPT.getNamesAndOutcomes(factor);

        if (outcomes.isEmpty()) {
            return result;
        }

        List<String> unWelcomeValues = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : outcomes.entrySet()) {
            if (entry.getValue().size() == 1) {
                String value = entry.getKey() + "=" + entry.getValue().get(0);
                unWelcomeValues.add(value);
            }
        }

        for (Map.Entry<String, Double> entry : factor.entrySet()) {
            StringBuilder new_key = new StringBuilder();
            List<String> new_key_split = separateByCommas(entry.getKey());

            for (String key : new_key_split) {
                if (!unWelcomeValues.contains(key)) {
                    new_key.append(key).append(",");
                }
            }
            result.put(new_key.substring(0, new_key.length() - 1), entry.getValue());
        }

        return result;
    }

    /**
     * Rounds a double value to five decimal places.
     *
     * @param d The value to round.
     * @return The rounded value.
     */
    public static double roundFiveDecimalPlaces(double d) {
        BigDecimal bigDecimal = new BigDecimal(Double.toString(d));
        bigDecimal = bigDecimal.setScale(5, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
