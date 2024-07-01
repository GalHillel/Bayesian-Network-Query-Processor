import java.util.*;

public class CPT {

    /**
     * Generates a Conditional Probability Table (CPT) from given values, outcomes,
     * and variable names.
     * 
     * @param values   Array of probabilities for each combination of outcomes.
     * @param outcomes List of outcome combinations for each variable.
     * @param names    List of variable names.
     * @return LinkedHashMap representing the generated CPT.
     */
    public static LinkedHashMap<String, Double> generateCPT(double[] values, List<List<String>> outcomes,
            List<String> names) {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        // Initialize an array to store the generated outcome combinations
        String[] outputs = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            outputs[i] = "";
        }

        // Calculate all possible outcome combinations based on the given outcomes lists
        int exp = values.length;
        for (int i = 0; i < outcomes.size(); i++) {
            List<String> o = outcomes.get(i);
            exp = exp / o.size();
            int k = 0, sum = 0;
            for (int j = 0; j < values.length; j++) {
                sum++;
                outputs[j] += o.get(k);
                if (i != outcomes.size() - 1)
                    outputs[j] += ",";
                if (sum >= exp) {
                    k++;
                    sum = 0;
                    if (k >= o.size()) {
                        k = 0;
                    }
                }
            }
        }

        // Generate keys for the LinkedHashMap using variable names and outcome
        // combinations
        List<String> keys = new ArrayList<>();
        for (String output : outputs) {
            String[] split_key = output.split(",");
            StringBuilder key_line = new StringBuilder();
            for (int j = 0; j < split_key.length; j++) {
                String key = names.get(j) + "=" + split_key[j];
                key_line.append(key);
                if (j != split_key.length - 1)
                    key_line.append(",");
            }
            keys.add(key_line.toString());
        }

        // Populate the result LinkedHashMap with generated keys and corresponding
        // values
        for (int i = 0; i < values.length; i++) {
            result.put(keys.get(i), values[i]);
        }

        return result;
    }

    /**
     * Joins multiple factors (conditional probability tables) into a single factor.
     * 
     * @param factors List of factors to be joined.
     * @param counter Counter to track the operations performed.
     * @return Joined factor as a LinkedHashMap.
     */
    public static LinkedHashMap<String, Double> joinFactors(List<LinkedHashMap<String, Double>> factors,
            Counter counter) {
        if (factors.isEmpty()) {
            throw new IllegalArgumentException("List of factors to join cannot be empty.");
        }

        LinkedHashMap<String, Double> result = factors.get(0);
        List<LinkedHashMap<String, Double>> remainingFactors = new ArrayList<>(factors.subList(1, factors.size()));
        return joinFactorsRecursive(remainingFactors, result, counter);
    }

    /**
     * Recursively joins factors (conditional probability tables) into a single
     * factor.
     * 
     * @param factors List of factors to be joined.
     * @param result  Resulting factor after each recursive join.
     * @param counter Counter to track the operations performed.
     * @return Joined factor as a LinkedHashMap.
     */
    private static LinkedHashMap<String, Double> joinFactorsRecursive(List<LinkedHashMap<String, Double>> factors,
            LinkedHashMap<String, Double> result, Counter counter) {
        if (factors.isEmpty()) {
            return result;
        }

        // Add the current result to the list of factors to be joined
        factors.add(result);
        // Sort factors based on a custom comparison method
        factors.sort((f1, f2) -> CPTCompare(f1, f2) ? 1 : -1);

        // Take the first factor after sorting and join it with the second factor
        result = factors.get(0);
        result = joinTwoFactors(result, factors.get(1), counter);
        result = UtilFunctions.fixingDuplicatesValuesInKeys(result);

        // Remove the first two factors (already joined) and continue recursively
        List<LinkedHashMap<String, Double>> remainingFactors = new ArrayList<>(factors.subList(2, factors.size()));
        return joinFactorsRecursive(remainingFactors, result, counter);
    }

    /**
     * Joins two factors (conditional probability tables) into a single factor.
     * 
     * @param factor1 First factor to be joined.
     * @param factor2 Second factor to be joined.
     * @param counter Counter to track the operations performed.
     * @return Joined factor as a LinkedHashMap.
     */
    public static LinkedHashMap<String, Double> joinTwoFactors(LinkedHashMap<String, Double> factor1,
            LinkedHashMap<String, Double> factor2, Counter counter) {
        // Extract outcomes and names from both factors
        HashMap<String, List<String>> factor1Outcomes = getNamesAndOutcomes(factor1);
        HashMap<String, List<String>> factor2Outcomes = getNamesAndOutcomes(factor2);

        // Get sets of names from both factors
        Set<String> factor1Names = factor1Outcomes.keySet();
        Set<String> factor2Names = factor2Outcomes.keySet();

        // Find common names between the two factors
        List<String> commonNames = UtilFunctions.intersection(new ArrayList<>(factor1Names),
                new ArrayList<>(factor2Names));

        // Initialize the result factor
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        // Iterate through entries of factor2
        for (Map.Entry<String, Double> entry2 : factor2.entrySet()) {
            // Split the key of factor2 into variable names and outcomes
            LinkedHashMap<String, String> valuesLine = UtilFunctions.splitKeysToVariablesAndOutcomes(entry2.getKey());
            List<String> intersectionValues = new ArrayList<>();

            // Collect intersection values based on common names
            for (String name : commonNames) {
                intersectionValues.add(name + "=" + valuesLine.get(name));
            }

            // Iterate through entries of factor1
            for (Map.Entry<String, Double> entry1 : factor1.entrySet()) {
                boolean isCompatible = true;

                // Check compatibility based on intersection values
                for (String value : intersectionValues) {
                    if (!entry1.getKey().contains(value)) {
                        isCompatible = false;
                        break;
                    }
                }

                // If compatible, calculate the product and add to the result factor
                if (isCompatible) {
                    double product = entry2.getValue() * entry1.getValue();
                    String[] factor2Split = entry2.getKey().split(",");
                    List<String> factor2List = new ArrayList<>(Arrays.asList(factor2Split));

                    String[] factor1Split = entry1.getKey().split(",");
                    List<String> factor1List = new ArrayList<>(Arrays.asList(factor1Split));

                    List<String> newKeySplit = UtilFunctions.union(factor1List, factor2List);
                    Collections.sort(newKeySplit);
                    String newKey = UtilFunctions.combineWithCommas(newKeySplit);
                    result.put(newKey, product);
                }
            }
        }

        // Update the counter with the number of entries in the result
        counter.addToProduct(result.size());

        return result;
    }

    /**
     * Retrieves variable names and their respective outcomes from a factor.
     * 
     * @param factor Factor (conditional probability table) as a LinkedHashMap.
     * @return LinkedHashMap mapping variable names to lists of outcomes.
     */
    public static LinkedHashMap<String, List<String>> getNamesAndOutcomes(LinkedHashMap<String, Double> factor) {
        LinkedHashMap<String, List<String>> outcomes = new LinkedHashMap<>();

        // Initialize a list to store variable names
        List<String> names = new ArrayList<>();
        // Iterate through the first entry of the factor to determine variable names
        for (Map.Entry<String, Double> line : factor.entrySet()) {
            LinkedHashMap<String, String> lineSplit = UtilFunctions.splitKeysToVariablesAndOutcomes(line.getKey());
            for (Map.Entry<String, String> inner : lineSplit.entrySet()) {
                names.add(inner.getKey());
            }
            break;
        }

        // Initialize empty lists for outcomes based on variable names
        for (String name : names) {
            outcomes.put(name, new ArrayList<>());
        }

        // Populate outcomes lists with actual outcomes from the factor
        for (Map.Entry<String, Double> line : factor.entrySet()) {
            LinkedHashMap<String, String> lineSplit = UtilFunctions.splitKeysToVariablesAndOutcomes(line.getKey());
            for (Map.Entry<String, String> inner : lineSplit.entrySet()) {
                if (!outcomes.get(inner.getKey()).contains(inner.getValue())) {
                    outcomes.get(inner.getKey()).add(inner.getValue());
                }
            }
        }
        return outcomes;
    }

    /**
     * Retrieves variable names from a factor.
     * 
     * @param factor Factor (conditional probability table) as a LinkedHashMap.
     * @return List of variable names present in the factor.
     */
    public static List<String> getFactorNames(LinkedHashMap<String, Double> factor) {
        List<String> names = new ArrayList<>();
        LinkedHashMap<String, List<String>> namesAndOutcomes = getNamesAndOutcomes(factor);
        for (Map.Entry<String, List<String>> entry : namesAndOutcomes.entrySet()) {
            names.add(entry.getKey());
        }
        return names;
    }

    /**
     * Eliminates a variable from a factor (conditional probability table).
     * 
     * @param factor  Factor (conditional probability table) as a LinkedHashMap.
     * @param hidden  Variable to be eliminated.
     * @param counter Counter to track the operations performed.
     * @return Eliminated factor as a LinkedHashMap.
     */
    public static LinkedHashMap<String, Double> eliminate(LinkedHashMap<String, Double> factor, Variable hidden,
            Counter counter) {
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        // Get variable names from the factor
        List<String> names = getFactorNames(factor);

        // If there is only one variable, return an empty result (no elimination needed)
        if (names.size() <= 1) {
            return result;
        }

        // Get outcomes for the hidden variable
        List<String> outcomes = hidden.getOutcomes();
        List<String> values = new ArrayList<>();
        for (String outcome : outcomes) {
            values.add(hidden.getName() + "=" + outcome);
        }

        // Iterate through entries of the factor
        for (Map.Entry<String, Double> entry : factor.entrySet()) {
            // Check if the entry contains any of the values related to the hidden variable
            for (String value : values) {
                if (entry.getKey().contains(value)) {
                    // Remove the value from the key to eliminate the hidden variable
                    List<String> newKeySplit = new ArrayList<>(Arrays.asList(entry.getKey().split(",")));
                    newKeySplit.remove(value);
                    String newKey = UtilFunctions.combineWithCommas(newKeySplit);

                    // Iterate again through entries of the factor to combine compatible entries
                    for (Map.Entry<String, Double> innerEntry : factor.entrySet()) {
                        boolean isCompatible = true;
                        List<String> newKeyValues = UtilFunctions.separateByCommas(newKey);
                        for (String newValue : newKeyValues) {
                            if (!innerEntry.getKey().contains(newValue)) {
                                isCompatible = false;
                                break;
                            }
                        }

                        if (innerEntry.getKey().equals(entry.getKey())) {
                            isCompatible = false;
                        }

                        // If compatible, add the values and update the result factor
                        if (isCompatible) {
                            double sum = entry.getValue() + innerEntry.getValue();
                            if (!result.containsKey(newKey)) {
                                counter.addSum(1);
                                result.put(newKey, sum);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Sorts a list of factors (conditional probability tables).
     * 
     * @param factors List of factors to be sorted.
     * @return Sorted list of factors.
     */
    public static List<LinkedHashMap<String, Double>> sortFactors(List<LinkedHashMap<String, Double>> factors) {
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Double>[] sortedFactors = factors.toArray(new LinkedHashMap[0]);
        Arrays.sort(sortedFactors, (f1, f2) -> CPTCompare(f1, f2) ? 1 : -1);
        return new ArrayList<>(Arrays.asList(sortedFactors));
    }

    /**
     * Custom comparison method for factors (conditional probability tables).
     * 
     * @param factor1 First factor to be compared.
     * @param factor2 Second factor to be compared.
     * @return True if factor1 should be placed after factor2 in sorted order, false
     *         otherwise.
     */
    private static boolean CPTCompare(LinkedHashMap<String, Double> factor1, LinkedHashMap<String, Double> factor2) {
        if (factor1.size() < factor2.size()) {
            return false;
        } else if (factor1.size() > factor2.size()) {
            return true;
        } else {
            List<String> factor1Names = getFactorNames(factor1);
            List<String> factor2Names = getFactorNames(factor2);

            int factor1AsciiSum = 0;
            for (String name : factor1Names) {
                for (int i = 0; i < name.length(); i++) {
                    factor1AsciiSum += name.charAt(i);
                }
            }

            int factor2AsciiSum = 0;
            for (String name : factor2Names) {
                for (int i = 0; i < name.length(); i++) {
                    factor2AsciiSum += name.charAt(i);
                }
            }

            return factor1AsciiSum >= factor2AsciiSum;
        }
    }
}
