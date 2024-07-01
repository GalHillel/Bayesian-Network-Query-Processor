import java.util.*;

public class BayesianNetwork {

    // Enum to track visited nodes in algorithms
    public enum Visited {
        YES,
        NO
    }

    private final List<Variable> variables; // List of all variables in the Bayesian network
    private final LinkedHashMap<String, List<Variable>> parents; // Mapping of each variable to its parents
    private final LinkedHashMap<String, List<Variable>> children; // Mapping of each variable to its children

    private static final List<Variable> emptyList = new ArrayList<>(); // Empty list used for initialization

    // Constructor for BayesianNetwork class
    public BayesianNetwork(List<Variable> variables) {
        this.variables = new ArrayList<>(variables); // Initialize variables list with provided variables

        this.parents = new LinkedHashMap<>(); // Initialize parents map
        this.children = new LinkedHashMap<>(); // Initialize children map

        initialize(); // Initialize parents and children mappings
    }

    // Method to initialize parent and child relationships for variables
    private void initialize() {
        // Iterate through each variable in the network
        for (Variable variable : this.variables) {
            List<Variable> variableParents = variable.getParents(); // Get parents of current variable

            // If variable has parents, update parents and children mappings
            if (variableParents != null) {
                this.parents.put(variable.getName(), variableParents); // Add parents to parents map

                // Update children map with current variable as child of its parents
                for (Variable parent : variableParents) {
                    if (this.children.containsKey(parent.getName())) {
                        this.children.get(parent.getName()).add(variable);
                    } else {
                        List<Variable> newList = new ArrayList<>();
                        newList.add(variable);
                        this.children.put(parent.getName(), newList);
                    }
                }
            }
        }

        // Ensure all variables have entries in parents and children maps
        for (Variable variable : this.variables) {
            if (!this.parents.containsKey(variable.getName())) {
                this.parents.put(variable.getName(), emptyList);
            }
            if (!this.children.containsKey(variable.getName())) {
                this.children.put(variable.getName(), emptyList);
            }
        }
    }

    // Get the total number of variables in the network
    public int getSize() {
        return this.variables.size();
    }

    // Get a variable by its name from the network
    public Variable getVariableByName(String name) {
        for (Variable variable : this.variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null; // Return null if variable with given name is not found
    }

    // Perform Bayesian network inference using the Bayes Ball algorithm
    public boolean bayesBall(String startNode, String destinationNode, List<String> evidenceNodesNames) {
        List<Variable> evidenceNodes = new ArrayList<>();

        // Convert evidence node names to Variable objects
        if (evidenceNodesNames != null) {
            for (String name : evidenceNodesNames) {
                evidenceNodes.add(this.getVariableByName(name));
            }
        }

        // Call the overloaded bayesBall method with Variable objects
        return bayesBall(getVariableByName(startNode), getVariableByName(destinationNode), evidenceNodes);
    }

    // Overloaded Bayesian network inference method using the Bayes Ball algorithm
    private boolean bayesBall(Variable startNode, Variable destinationNode, List<Variable> evidenceNodes) {

        // Return true immediately if start or destination nodes are null
        if (startNode == null || destinationNode == null)
            return true;

        // Return false if start node is the same as destination node
        if (startNode.equals(destinationNode))
            return false;

        LinkedHashMap<Variable, Visited> visited = new LinkedHashMap<>();

        // Initialize visited map and set shaded and fromChild flags for evidence nodes
        for (Variable variable : this.variables) {
            variable.setShaded(evidenceNodes.contains(variable));
            variable.setFromChild(false);
            visited.put(variable, Visited.NO);
        }

        visited.put(startNode, Visited.YES); // Mark start node as visited
        Queue<Variable> queue = new LinkedList<>();
        queue.add(startNode); // Add start node to queue

        // Perform BFS using the queue to explore the network
        while (!queue.isEmpty()) {
            Variable v = queue.poll();
            for (Variable u : getNeighbors(v)) {
                // Return false if destination node is reached
                if (u.equals(destinationNode)) {
                    return false;
                }

                // Add u to queue and set its fromChild flag if it is a parent of v
                if (this.parents.get(v.getName()).contains(u)) {
                    queue.add(u);
                    u.setFromChild(true);
                } else if (visited.get(u) == Visited.NO) {
                    // Add u to queue and mark it as visited
                    queue.add(u);
                    visited.put(u, Visited.YES);
                    u.setFromChild(false);

                    // Adjust fromChild flag based on shading conditions
                    if (u.isShaded()) {
                        u.setFromChild(true);
                        if (v.isShaded()) {
                            u.setFromChild(false);
                        }
                    }
                }
            }
        }
        return true; // Return true if destination node is not reachable
    }

    // Get neighbors of a variable based on its child status
    private List<Variable> getNeighbors(Variable variable) {
        List<Variable> neighbors = new ArrayList<>(this.children.get(variable.getName()));
        if (variable.isFromChild()) {
            neighbors.addAll(this.parents.get(variable.getName()));
        }
        return neighbors;
    }

    // Perform variable elimination algorithm for probabilistic inference
    public List<Double> variableElimination(String hypothesis, List<String> evidence, List<String> hidden) {

        // Split hypothesis query into variable name and value
        String[] hypothesisQuery = hypothesis.split("=");
        Variable hypothesisVariable = getVariableByName(hypothesisQuery[0]);
        String hypothesisValue = hypothesisQuery[1];

        List<String> evidenceValues = new ArrayList<>();
        List<Variable> evidenceVariables = new ArrayList<>();

        // Convert evidence strings into Variable objects and their corresponding values
        if (evidence != null && !evidence.isEmpty()) {
            for (String evs : evidence) {
                String[] evidenceQueries = evs.split(",");
                for (String ev : evidenceQueries) {
                    String[] evidenceQuery = ev.split("=");
                    if (evidenceQuery.length > 0) {
                        evidenceVariables.add(getVariableByName(evidenceQuery[0]));
                        evidenceValues.add(evidenceQuery[1]);
                    }
                }
            }
        }

        List<Variable> hiddenVariables = new ArrayList<>();

        // Convert hidden node names into Variable objects
        if (hidden != null) {
            for (String s : hidden) {
                hiddenVariables.add(getVariableByName(s));
            }
        }

        // Call the overloaded variableElimination method with Variable objects
        return variableElimination(hypothesisVariable, hypothesisValue, evidenceVariables, evidenceValues,
                hiddenVariables);
    }

    // Overloaded variable elimination method using Variable objects for
    // probabilistic inference
    private List<Double> variableElimination(Variable hypothesis, String hypothesisValue,
            List<Variable> evidenceVariables, List<String> evidenceValues, List<Variable> hidden) {

        Counter counter = new Counter(); // Counter to keep track of operations
        LinkedHashMap<String, LinkedHashMap<String, Double>> factors = new LinkedHashMap<>(); // Factors map
        List<String> evidenceVariablesNames = new ArrayList<>(); // Names of evidence variables

        // Populate evidenceVariablesNames with names of evidence variables
        for (Variable variable : evidenceVariables) {
            evidenceVariablesNames.add(variable.getName());
        }

        // Update factors based on evidence variables and values
        for (Variable variable : this.variables) {
            factors.put(variable.getName(), updateCPT(evidenceVariablesNames, evidenceValues, variable.getCPT()));
        }

        List<Variable> checking = new ArrayList<>();
        checking.add(hypothesis);
        checking.addAll(evidenceVariables);

        // Determine hidden variables that are not descendants of checked variables
        LinkedHashMap<String, Boolean> notGrandparents = new LinkedHashMap<>();
        for (Variable h : hidden) {
            notGrandparents.put(h.getName(), false);
        }

        // Check each variable in checking list against hidden variables
        for (Variable v : checking) {
            for (Variable h : hidden) {
                notGrandparents.compute(h.getName(), (k, b) -> Boolean.TRUE.equals(b) | v.isGrandParent(h));
            }
        }

        // Remove factors not associated with any hidden variables
        for (Map.Entry<String, Boolean> entry : notGrandparents.entrySet()) {
            if (!entry.getValue()) {
                factors.remove(entry.getKey());
            }
        }

        // Handle special case when no evidence is provided
        if (evidenceVariables.isEmpty()) {
            for (Map.Entry<String, LinkedHashMap<String, Double>> f : factors.entrySet()) {
                for (Map.Entry<String, Double> line : f.getValue().entrySet()) {
                    if (line.getKey().equals(hypothesisValue)) {
                        List<Double> surpriseResult = new ArrayList<>();
                        surpriseResult.add(line.getValue());
                        surpriseResult.add(0.0);
                        surpriseResult.add(0.0);
                        return surpriseResult;
                    }
                }
            }
        }

        // Handle elimination of hidden variables from factors
        if (!hidden.isEmpty()) {
            for (Variable h : hidden) {
                List<LinkedHashMap<String, Double>> cptToJoin = new ArrayList<>();
                List<String> variablesNamesToJoin = new ArrayList<>();

                for (Map.Entry<String, LinkedHashMap<String, Double>> entry : factors.entrySet()) {
                    if (CPT.getFactorNames(entry.getValue()).contains(h.getName())) {
                        cptToJoin.add(entry.getValue());
                        variablesNamesToJoin.add(entry.getKey());
                    }
                }

                String lastName = "empty";
                if (variablesNamesToJoin.size() > 1) {
                    for (String name : variablesNamesToJoin) {
                        factors.remove(name);
                        lastName = name;
                    }
                } else if (variablesNamesToJoin.size() == 1) {
                    lastName = variablesNamesToJoin.get(0);
                }

                if (!cptToJoin.isEmpty()) {
                    if (cptToJoin.size() > 1) {
                        LinkedHashMap<String, Double> newFactor = CPT.joinFactors(cptToJoin, counter);
                        boolean factorToAdd = true;

                        if (CPT.getFactorNames(newFactor).size() > 1) {
                            newFactor = CPT.eliminate(newFactor, h, counter);
                        } else if (CPT.getFactorNames(newFactor).size() == 1) {
                            factorToAdd = false;
                        }

                        if (factorToAdd)
                            factors.put(lastName, newFactor);
                    }
                }
            }
        }

        // Remove factors with size 1 or less from factors map
        LinkedHashMap<String, Integer> sizes = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, Double>> factor : factors.entrySet()) {
            sizes.put(factor.getKey(), factor.getValue().size());
        }

        for (Map.Entry<String, Integer> factor : sizes.entrySet()) {
            if (factor.getValue() <= 1) {
                factors.remove(factor.getKey());
            }
        }

        // Join remaining factors to compute final factor
        LinkedHashMap<String, Double> lastFactor = new LinkedHashMap<>();
        if (factors.size() > 1) {
            List<LinkedHashMap<String, Double>> factorsLeft = new ArrayList<>();

            for (Map.Entry<String, LinkedHashMap<String, Double>> f : factors.entrySet()) {
                factorsLeft.add(f.getValue());
            }
            lastFactor = CPT.joinFactors(factorsLeft, counter);
        } else {

            for (Map.Entry<String, LinkedHashMap<String, Double>> f : factors.entrySet()) {
                lastFactor = new LinkedHashMap<>(f.getValue());
                break;
            }
        }

        // Eliminate non-hypothesis variables from final factor
        List<String> namesInLastFactor = CPT.getFactorNames(lastFactor);
        if (namesInLastFactor.size() > 1) {
            namesInLastFactor.remove(hypothesis.getName());
            for (String name : namesInLastFactor) {
                lastFactor = CPT.eliminate(lastFactor, getVariableByName(name), counter);
            }
        }

        lastFactor = normalize(lastFactor, counter); // Normalize final factor values

        double value = 0.0;
        for (Map.Entry<String, Double> entry : lastFactor.entrySet()) {
            if (entry.getKey().contains(hypothesisValue)) {
                value = entry.getValue();
                break;
            }
        }

        List<Double> result = new ArrayList<>();
        result.add(value);
        result.add((double) counter.getSum());
        result.add((double) counter.getProduct());

        return result; // Return final result of variable elimination
    }

    // Update Conditional Probability Table (CPT) based on evidence variables and
    // values
    public static LinkedHashMap<String, Double> updateCPT(List<String> evidence, List<String> values,
            LinkedHashMap<String, Double> factor) {

        List<String> variablesInFactor = CPT.getFactorNames(factor); // Get variables in the factor
        List<String> evidenceInFactor = UtilFunctions.intersection(variablesInFactor, evidence); // Get evidence in the
                                                                                                 // factor

        List<String> relevantEvidence = new ArrayList<>();
        List<String> relevantValues = new ArrayList<>();

        // Extract relevant evidence and values from input evidence and values
        for (int i = 0; i < evidence.size(); i++) {
            String name = evidence.get(i);
            if (evidenceInFactor.contains(name)) {
                relevantEvidence.add(evidence.get(i));
                relevantValues.add(values.get(i));
            }
        }

        LinkedHashMap<String, Double> result = new LinkedHashMap<>();

        // Filter factor based on relevant evidence and values
        for (Map.Entry<String, Double> entry : factor.entrySet()) {
            boolean match = true;
            for (int i = 0; i < relevantEvidence.size(); i++) {
                StringBuilder evidenceValue = new StringBuilder();
                evidenceValue.append(relevantEvidence.get(i)).append("=").append(relevantValues.get(i));
                match &= entry.getKey().contains(evidenceValue);
            }
            if (match) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result; // Return updated CPT based on evidence
    }

    // Normalize a factor to ensure probabilities sum up to 1
    public LinkedHashMap<String, Double> normalize(LinkedHashMap<String, Double> factor, Counter counter) {

        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        factor = UtilFunctions.fixingDuplicatesValuesInKeys(factor); // Handle duplicate values in keys

        // Get outcomes for normalization
        LinkedHashMap<String, List<String>> outcomes = CPT.getNamesAndOutcomes(factor);
        String variableName = "";
        for (Map.Entry<String, List<String>> entry : outcomes.entrySet()) {
            variableName = entry.getKey();
        }
        List<String> variableOutcomes = outcomes.get(variableName);
        List<String> variableOutcomesKeys = new ArrayList<>();
        for (String outcome : variableOutcomes) {
            String value = variableName + "=" + outcome;
            variableOutcomesKeys.add(value);
        }
        List<Double> values = new ArrayList<>();

        // Populate values with factor values
        for (String outcome : variableOutcomesKeys) {
            values.add(factor.get(outcome));
        }

        counter.addSum(values.size() - 1); // Increment counter

        double exp = 0.0;
        for (Double value : values) {
            exp += value;
        }
        exp = 1 / exp; // Calculate normalization factor
        for (Map.Entry<String, Double> entry : factor.entrySet()) {
            result.put(entry.getKey(), entry.getValue() * exp); // Normalize factor values
        }
        return result; // Return normalized factor
    }
}
