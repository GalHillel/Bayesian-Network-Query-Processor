
# Bayesian Network Query Processor

This Java application processes queries on a Bayesian network defined in an XML format, computing probabilities and answering specific queries based on the network's structure and conditional probabilities.

## Project Structure

```
├── alarm_net.xml
├── BayesianNetwork.java
├── Counter.java
├── CPT.java
├── Ex1.java
├── input.txt
├── output.txt
├── QueryReader.java
├── QueryType.java
├── UtilFunctions.java
├── Variable.java
└── XMLParser.java
```

### File Descriptions

- **alarm_net.xml**: Example XML file defining a Bayesian network.
- **BayesianNetwork.java**: Represents the Bayesian network structure using `Variable` objects and computes probabilities using variable elimination (VE) and Bayes' ball algorithms.
- **Counter.java**: Utility class for counting purposes within the Bayesian network computations.
- **CPT.java**: Manages the conditional probability tables (CPTs) for variables.
- **Ex1.java**: The main class where the application execution starts. It reads input from a file, initializes the Bayesian network, processes queries, and outputs results.
- **input.txt**: Input file containing the XML definition of the Bayesian network followed by queries.
- **output.txt**: Output file where the results of the processed queries are saved.
- **QueryReader.java**: Handles different types of queries (Bayes' ball and variable elimination), parsing them into usable data structures for processing by the `BayesianNetwork`.
- **QueryType.java**: Enum class defining the types of queries supported (Bayes' ball and variable elimination).
- **UtilFunctions.java**: Provides utility functions like union, intersection, string manipulation, and rounding for various operations within the application.
- **Variable.java**: Represents a variable in the Bayesian network with its name, outcomes, parents, conditional probability table (CPT), and other related properties.
- **XMLParser.java**: Parses XML files describing Bayesian networks, extracts variables, outcomes, parent-child relationships, and CPTs, and initializes `Variable` objects accordingly.

## Installation

1. **Clone the repository:**

   ```bash
   git clone <repository_url>
   cd BayesianNetworkQueryProcessor
   ```

2. **Compile the Java files:**

   Ensure you have JDK (Java Development Kit) installed.

   ```bash
   javac *.java
   ```

## Usage

1. **Prepare input files:**

   - Create an `input.txt` file containing the XML definition of the Bayesian network followed by queries.
   
     Example:
     ```
     <VARIABLE>
         <NAME>Variable1</NAME>
         <OUTCOME>Outcome1</OUTCOME>
         <OUTCOME>Outcome2</OUTCOME>
         ...
     </VARIABLE>
     <DEFINITION>
         <FOR>Variable1</FOR>
         <GIVEN>ParentVariable1</GIVEN>
         <GIVEN>ParentVariable2</GIVEN>
         <TABLE>0.2 0.8</TABLE>
     </DEFINITION>
     P(Variable1 | ParentVariable1, ParentVariable2)
     B(Variable1-Variable2|ParentVariable1=Value1,ParentVariable2=Value2)
     ```

2. **Run the application:**

   Execute the compiled `Ex1` class with Java, providing `input.txt` as input.

   ```bash
   java Ex1
   ```

3. **Output:**

   The application will process the queries, compute the results, and save them to an `output.txt` file.

## Example

Given the following `input.txt`:

```
alarm_net.xml
P(Burglary | JohnCalls=true, MaryCalls=true)
B(JohnCalls-MaryCalls|Alarm=true)
```

- **alarm_net.xml**: Contains the Bayesian network definition.
- **Queries**:
  - `P(Burglary | JohnCalls=true, MaryCalls=true)`: Query for the probability of Burglary given that both John and Mary have called.
  - `B(JohnCalls-MaryCalls|Alarm=true)`: Query for the Bayes' ball algorithm between JohnCalls and MaryCalls given that Alarm is true.

## Contributing

Contributions are welcome! If you'd like to enhance this project or add new features, feel free to fork the repository and submit your changes via pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for details.
