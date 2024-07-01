import java.util.*;

public class Variable {
    private final String name;
    private List<Variable> parents;
    private final List<String> outcomes;
    private LinkedHashMap<String, Double> cpt;
    private boolean shaded;
    private boolean fromChild;
    @SuppressWarnings("unused")
    private boolean uninitialized;

    /**
     * Constructor to initialize a Variable with its name and possible outcomes.
     *
     * @param name     The name of the variable.
     * @param outcomes List of possible outcomes for the variable.
     */
    public Variable(String name, List<String> outcomes) {
        this.name = name;
        this.outcomes = outcomes;
        this.cpt = new LinkedHashMap<>(); // Initialize CPT as an empty LinkedHashMap
        this.shaded = false; // By default, the variable is not shaded
        this.fromChild = false; // By default, the variable does not originate from a child node
        this.uninitialized = false; // By default, the variable is considered initialized
    }

    /**
     * Initializes the parents of the variable along with its CPT.
     *
     * @param values  Array of values representing probabilities for each outcome.
     * @param parents Array of parent variables.
     */
    public void initializeParents(double[] values, Variable[] parents) {
        this.parents = new ArrayList<>(Arrays.asList(parents));

        if (this.parents.isEmpty()) {
            // No parents case: directly set CPT based on provided values
            for (int i = 0; i < this.outcomes.size(); i++) {
                this.cpt.put(this.name + '=' + this.outcomes.get(i), values[i]);
            }
        } else {
            // Has parents case: generate CPT using parent outcomes and names
            List<List<String>> allOutcomes = new ArrayList<>();
            List<String> allNames = new ArrayList<>();

            for (Variable parent : this.parents) {
                allOutcomes.add(parent.outcomes);
                allNames.add(parent.name);
            }
            allOutcomes.add(this.outcomes);
            allNames.add(this.name);

            this.cpt = CPT.generateCPT(values, allOutcomes, allNames); // Generate CPT using utility function
        }
        this.uninitialized = true; // Mark variable as initialized
    }

    /**
     * Retrieves the list of parent variables.
     *
     * @return List of parent variables.
     */
    public List<Variable> getParents() {
        return this.parents;
    }

    /**
     * Checks if the variable has any parent variables.
     *
     * @return True if the variable has parents, false otherwise.
     */
    public boolean hasParents() {
        return !this.parents.isEmpty();
    }

    /**
     * Retrieves the name of the variable.
     *
     * @return The name of the variable.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets whether the variable is shaded (used in algorithms).
     *
     * @param shaded True if the variable is shaded, false otherwise.
     */
    public void setShaded(boolean shaded) {
        this.shaded = shaded;
    }

    /**
     * Checks if the variable is shaded (used in algorithms).
     *
     * @return True if the variable is shaded, false otherwise.
     */
    public boolean isShaded() {
        return this.shaded;
    }

    /**
     * Retrieves the Conditional Probability Table (CPT) of the variable.
     *
     * @return The CPT of the variable.
     */
    public LinkedHashMap<String, Double> getCPT() {
        return this.cpt;
    }

    /**
     * Retrieves the list of possible outcomes for the variable.
     *
     * @return List of possible outcomes.
     */
    public List<String> getOutcomes() {
        return this.outcomes;
    }

    /**
     * Checks if the current variable is a grandparent of a given parent variable.
     *
     * @param parentToCheck The parent variable to check against.
     * @return True if the current variable is a grandparent, false otherwise.
     */
    public boolean isGrandParent(Variable parentToCheck) {
        return isGrandParent(parentToCheck, this);
    }

    /**
     * Helper method to recursively check if the current variable is a grandparent
     * of a given parent variable.
     *
     * @param parentToCheck The parent variable to check against.
     * @param current       The current variable being checked recursively.
     * @return True if the current variable is a grandparent, false otherwise.
     */
    private boolean isGrandParent(Variable parentToCheck, Variable current) {
        if (current.getName().equals(parentToCheck.getName())) {
            return true;
        }
        if (!current.hasParents()) {
            return false;
        }
        for (Variable parent : current.parents) {
            if (isGrandParent(parentToCheck, parent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the variable originates from a child node.
     *
     * @return True if the variable originates from a child node, false otherwise.
     */
    public boolean isFromChild() {
        return this.fromChild;
    }

    /**
     * Sets whether the variable originates from a child node.
     *
     * @param fromChild True if the variable originates from a child node, false
     *                  otherwise.
     */
    public void setFromChild(boolean fromChild) {
        this.fromChild = fromChild;
    }

    /**
     * Custom implementation of toString() to return the name of the variable.
     *
     * @return The name of the variable.
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Custom implementation of equals() to compare variables based on their name.
     *
     * @param o The object to compare against.
     * @return True if the variables are equal (based on name), false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Variable variable = (Variable) o;
        return Objects.equals(this.name, variable.name);
    }

    /**
     * Custom implementation of hashCode() to generate hash based on the name of the
     * variable.
     *
     * @return Hash code of the variable.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
