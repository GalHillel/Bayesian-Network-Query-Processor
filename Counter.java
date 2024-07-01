public class Counter {

    private int sum; // Counter for sum
    private int product; // Counter for product

    // Constructor to initialize the counters
    public Counter() {
        this.sum = 0;
        this.product = 0;
    }

    // Method to add to the sum counter
    public void addSum(int add) {
        this.sum += add;
    }

    // Method to add to the product counter
    public void addToProduct(int mul) {
        this.product += mul;
    }

    // Getter method to retrieve the current sum counter value
    public int getSum() {
        return sum;
    }

    // Getter method to retrieve the current product counter value
    public int getProduct() {
        return product;
    }
}
