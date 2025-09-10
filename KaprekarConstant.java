//lines 145 and 146 change the number of digits and the base
package pack;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

// --- Helper Classes for Result Management ---

enum ResultType {
    FINAL, CYCLE
}

class KaprekarResult {
    private int[] initialNumber;
    private ResultType type;
    private int[] finalNumber;
    private List<int[]> cycleElements;
    private int iterations;
    private int base; // Store the base for correct display in toString()

    public KaprekarResult(int[] initialNumber, int[] finalNumber, int iterations, int base) {
        this.initialNumber = initialNumber;
        this.type = ResultType.FINAL;
        this.finalNumber = finalNumber;
        this.iterations = iterations;
        this.base = base;
    }

    public KaprekarResult(int[] initialNumber, List<int[]> cycleElements, int iterations, int base) {
        this.initialNumber = initialNumber;
        this.type = ResultType.CYCLE;
        this.cycleElements = cycleElements;
        this.iterations = iterations;
        this.base = base;
    }

    public int[] getInitialNumber() { return initialNumber; }
    public ResultType getType() { return type; }
    public int[] getFinalNumber() { return finalNumber; }
    public List<int[]> getCycleElements() { return cycleElements; }
    public int getIterations() { return iterations; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Initial: ").append(o7_16_2025.arrayToStringWithBaseDigits(initialNumber, base))
          .append(", Type: ").append(type)
          .append(", Iterations: ").append(iterations);
        if (type == ResultType.FINAL) {
            sb.append(", FinalNum: ").append(o7_16_2025.arrayToStringWithBaseDigits(finalNumber, base));
        } else { // CYCLE
            sb.append(", Cycle: [");
            for (int i = 0; i < cycleElements.size(); i++) {
                sb.append(o7_16_2025.arrayToStringWithBaseDigits(cycleElements.get(i), base));
                if (i < cycleElements.size() - 1) {
                    sb.append(" -> ");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
}

// Represents a unique cycle for aggregation, comparing cycles by their set of elements.
class UniqueCycleRepresenter {
    // Canonical representation: a set of string representations of the cycle's elements.
    // This makes comparison order-independent.
    private Set<String> canonicalCycleStrings;
    
    // An example of the actual cycle elements, needed for displaying.
    private List<int[]> exampleCycleElements; 
    
    // How many distinct initial numbers lead to this unique cycle.
    private int count; 
    private int base; // Store the base for correct display in toString()

    public UniqueCycleRepresenter(List<int[]> cycleElements, int base) { // Base added to constructor
        this.base = base; // Initialize base
        this.canonicalCycleStrings = new HashSet<>();
        for (int[] digits : cycleElements) {
            // Use the base-aware string conversion for canonical representation
            this.canonicalCycleStrings.add(o7_16_2025.arrayToStringWithBaseDigits(digits, base)); 
        }
        this.exampleCycleElements = new ArrayList<>();
        for (int[] element : cycleElements) {
            this.exampleCycleElements.add(element.clone());
        }
        this.count = 0; 
    }

    public void incrementCount() {
        this.count++;
    }

    public int getCount() {
        return count;
    }

    public List<int[]> getExampleCycleElements() {
        return exampleCycleElements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueCycleRepresenter that = (UniqueCycleRepresenter) o;
        return Objects.equals(canonicalCycleStrings, that.canonicalCycleStrings); 
    }

    @Override
    public int hashCode() {
        return Objects.hash(canonicalCycleStrings); 
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Cycle: [");
        for (int i = 0; i < exampleCycleElements.size(); i++) {
            sb.append(o7_16_2025.arrayToStringWithBaseDigits(exampleCycleElements.get(i), base)); // Use custom display
            if (i < exampleCycleElements.size() - 1) {
                sb.append(" -> ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}


// --- Main Class ---
public class o7_16_2025 {

    // Global variable for the base
    private static int CURRENT_BASE = 10; // Default to base 10

    public static void main(String[] args) {
        int n = 6; // Number of digits
        CURRENT_BASE = 10; // Set the base here (e.g., 2 for binary, 10 for decimal, 16 for hexadecimal, 36 for max)

        // Validate base range (2 to 36 inclusive)
        if (CURRENT_BASE < 2 || CURRENT_BASE > 36) { // Reverted to allow bases > 10
            System.out.println("Error: Base must be between 2 and 36 (inclusive).");
            return;
        }
        if (n < 1) {
            System.out.println("Error: Number of digits (n) must be at least 1.");
            return;
        }

        List<int[]> allNumbers = generateAllNumbers(n, CURRENT_BASE);

        System.out.println("Starting Kaprekar's Routine Analysis for " + n + "-digit numbers in Base " + CURRENT_BASE + ".");
        System.out.println("Total valid numbers to analyze: " + allNumbers.size());
        System.out.println("--------------------------------------------------");

        List<KaprekarResult> allKaprekarResults = new ArrayList<>();

        for (int[] initialNumber : allNumbers) {
            KaprekarResult result = analyzeKaprekarSequence(initialNumber, n, CURRENT_BASE);
            allKaprekarResults.add(result);
        }

        System.out.println("\n--- Aggregated Analysis Results for N=" + n + " in Base " + CURRENT_BASE + " ---");

        if (allKaprekarResults.isEmpty()) {
            System.out.println("No valid numbers generated for N=" + n + " in Base " + CURRENT_BASE + ".");
            return;
        }

        // Keys for unique final numbers will now be the custom formatted strings
        Map<String, Integer> uniqueFinalNumbers = new HashMap<>(); 
        int maxIterationsToFinal = 0;

        // Keys for unique cycles will use the base-aware UniqueCycleRepresenter
        Map<UniqueCycleRepresenter, UniqueCycleRepresenter> uniqueCycles = new HashMap<>(); 
        int totalNumbersConvergedToCycles = 0;

        for (KaprekarResult result : allKaprekarResults) {
            if (result.getType() == ResultType.FINAL) {
                // Use the custom display method for the map key
                String finalNumStr = arrayToStringWithBaseDigits(result.getFinalNumber(), CURRENT_BASE); 
                uniqueFinalNumbers.put(finalNumStr, uniqueFinalNumbers.getOrDefault(finalNumStr, 0) + 1);

                if (result.getIterations() > maxIterationsToFinal) {
                    maxIterationsToFinal = result.getIterations();
                }
            } else { // ResultType.CYCLE
                // Pass base to UniqueCycleRepresenter constructor
                UniqueCycleRepresenter currentCycleRepr = new UniqueCycleRepresenter(result.getCycleElements(), CURRENT_BASE);
                
                if (uniqueCycles.containsKey(currentCycleRepr)) {
                    uniqueCycles.get(currentCycleRepr).incrementCount();
                } else {
                    currentCycleRepr.incrementCount(); 
                    uniqueCycles.put(currentCycleRepr, currentCycleRepr); 
                }
                totalNumbersConvergedToCycles++;
            }
        }

        // --- Reporting ---
        int totalCheckedNumbers = allNumbers.size();

        // Determine if all results lead to a single final number AND no cycles
        boolean allConvergeToSingleFinal = (uniqueFinalNumbers.size() == 1 && uniqueCycles.isEmpty());

        if (allConvergeToSingleFinal) {
            System.out.println("ALL numbers of " + n + " digits in Base " + CURRENT_BASE + " converge to the single final number: " + 
                               uniqueFinalNumbers.keySet().iterator().next()); 
            System.out.println("The most number of iterations it took to arrive at that number was: " + maxIterationsToFinal);
        } else {
            System.out.println("NOT all numbers of " + n + " digits in Base " + CURRENT_BASE + " converge to the same final number, or some entered cycles.");
            
            System.out.println("\n--- Unique Final Numbers Found ---");
            if (uniqueFinalNumbers.isEmpty()) {
                System.out.println("No numbers converged to a final fixed point for N=" + n + " in Base " + CURRENT_BASE + ".");
            } else {
                for (Map.Entry<String, Integer> entry : uniqueFinalNumbers.entrySet()) {
                    if (totalCheckedNumbers > 0) {
                        double percent = (double) entry.getValue() / totalCheckedNumbers * 100;
                        System.out.printf("Final Number: %s (Reached by %.2f%% of checked numbers)%n", entry.getKey(), percent);
                    } else {
                        System.out.println("Final Number: " + entry.getKey() + " (Reached by " + entry.getValue() + " numbers)");
                    }
                }
                System.out.println("Maximum iterations to reach any unique final number: " + maxIterationsToFinal);
            }

            System.out.println("\n--- Unique Cycles Found ---");
            if (uniqueCycles.isEmpty()) {
                System.out.println("No numbers converged to a cycle for N=" + n + " in Base " + CURRENT_BASE + ".");
            } else {
                int cycleCounter = 1;
                for (UniqueCycleRepresenter cycleRepr : uniqueCycles.values()) {
                    if (totalCheckedNumbers > 0) {
                        double percent = (double) cycleRepr.getCount() / totalCheckedNumbers * 100;
                        System.out.printf("Unique Cycle %d: %s (Reached by %.2f%% of checked numbers)%n", 
                                          cycleCounter, cycleRepr.toString(), percent);
                    } else {
                        System.out.println("Unique Cycle " + cycleCounter + ": " + cycleRepr.toString() + 
                                           " (Reached by " + cycleRepr.getCount() + " numbers)");
                    }
                    cycleCounter++;
                }
            }

            if (totalCheckedNumbers > 0) {
                double percentToCycles = (double) totalNumbersConvergedToCycles / totalCheckedNumbers * 100;
                System.out.printf("\nPercentage of ALL checked numbers that converged to ANY cycle: %.2f%%%n", percentToCycles);
            } else {
                System.out.println("\nCannot calculate percentage to cycles, as no valid numbers were analyzed.");
            }
        }
        System.out.println("\n--- Analysis Complete ---");
    }

    /**
     * Converts an integer digit value (0-35) to its character representation ('0'-'9', 'A'-'Z').
     * @param digit The integer value of the digit.
     * @return The character representation of the digit.
     * @throws IllegalArgumentException if the digit is out of the valid range (0-35).
     */
    public static char digitToChar(int digit) {
        if (digit >= 0 && digit <= 9) {
            return (char) ('0' + digit);
        } else if (digit >= 10 && digit <= 35) {
            return (char) ('A' + (digit - 10));
        } else {
            throw new IllegalArgumentException("Digit " + digit + " is out of valid range (0-35)");
        }
    }

    /**
     * Converts an array of integer digits into a string representation using the specified base,
     * converting digit values > 9 to letters (A-Z).
     * @param digits The array of integer digits.
     * @param base The base of the number system for display.
     * @return A string representation of the number (e.g., "[1, 0, A]" for [1, 0, 10] in base 16).
     */
    public static String arrayToStringWithBaseDigits(int[] digits, int base) {
        if (digits == null) {
            return "null";
        }
        if (digits.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < digits.length; i++) {
            sb.append(digitToChar(digits[i]));
            if (i < digits.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }


    /**
     * Analyzes the Kaprekar-like sequence for a given initial number in a specific base.
     * @param initialDigits The starting number's digits as an array.
     * @param n The number of digits (used for padding results).
     * @param base The base of the number system.
     * @return A KaprekarResult object detailing the outcome of the analysis.
     */
    private static KaprekarResult analyzeKaprekarSequence(int[] initialDigits, int n, int base) {
        List<int[]> sequenceHistory = new ArrayList<>();
        int[] currentNum = initialDigits;
        int iteration = 0; 

        while (true) {
            sequenceHistory.add(currentNum); 
            iteration++; 

            int[] decreasingOrder = getDecreasingOrder(currentNum);
            int[] increasingOrder = getIncreasingOrder(currentNum);

            int[] nextNum = subtractSortedNumbers(decreasingOrder, increasingOrder, n, base);
            
            if (Arrays.equals(nextNum, currentNum)) {
                // Pass base to KaprekarResult constructor
                return new KaprekarResult(initialDigits, nextNum, iteration - 1, base);
            }

            int cycleStartIndex = -1;
            for (int i = 0; i < sequenceHistory.size(); i++) {
                if (Arrays.equals(nextNum, sequenceHistory.get(i))) {
                    cycleStartIndex = i; 
                    break;
                }
            }

            if (cycleStartIndex != -1) {
                List<int[]> trueCycleElements = new ArrayList<>();
                for (int i = cycleStartIndex; i < sequenceHistory.size(); i++) {
                    trueCycleElements.add(sequenceHistory.get(i).clone()); 
                }
                // Pass base to KaprekarResult constructor
                return new KaprekarResult(initialDigits, trueCycleElements, cycleStartIndex, base);
            }

            currentNum = nextNum;
        }
    }


    /**
     * Helper method to convert an array of digits in a given base into a single base-10 integer.
     * @param digits The array of digits.
     * @param base The base of the number system the digits are in.
     * @return The base-10 integer representation of the digits.
     */
    public static int convertDigitsToInt(int[] digits, int base) {
        int number = 0;
        for (int digit : digits) {
            number = number * base + digit;
        }
        return number;
    }

    /**
     * Converts a base-10 integer into an array of its digits in a specified base,
     * padded with leading zeros to ensure the array has a specific length `n`.
     * @param number The base-10 integer to convert.
     * @param n The desired length of the digit array.
     * @param base The target base for the digit representation.
     * @return An array of digits representing the number in the specified base, padded with leading zeros.
     */
    private static int[] convertIntToPaddedDigits(int number, int n, int base) {
        // Handle 0 explicitly
        if (number == 0) {
            int[] zeroDigits = new int[n]; 
            Arrays.fill(zeroDigits, 0);
            return zeroDigits;
        }

        List<Integer> tempDigits = new ArrayList<>();
        int currentNumber = Math.abs(number); // Work with absolute value

        while (currentNumber > 0) {
            tempDigits.add(currentNumber % base);
            currentNumber /= base;
        }

        // Pad with leading zeros if necessary
        while (tempDigits.size() < n) {
            tempDigits.add(0);
        }

        // Convert List to array and reverse (since we built it from right to left)
        int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            // Fill from the end of tempDigits to the beginning of result
            result[i] = tempDigits.get(tempDigits.size() - 1 - i);
        }
        return result;
    }

    /**
     * Subtracts the integer formed by increasing order digits from the integer
     * formed by decreasing order digits, and returns the result as an array of digits
     * padded to the original length `n` in the specified base.
     * @param decreasingDigits An array of digits sorted in decreasing order (in the current base).
     * @param increasingDigits An array of digits sorted in increasing order (in the current base).
     * @param n The original number of digits for padding the result.
     * @param base The base of the number system.
     * @return An array of digits representing the result of the subtraction in the specified base, padded with leading zeros.
     */
    private static int[] subtractSortedNumbers(int[] decreasingDigits, int[] increasingDigits, int n, int base) {
        int numDecreasing = convertDigitsToInt(decreasingDigits, base); // Convert to base 10
        int numIncreasing = convertDigitsToInt(increasingDigits, base);   // Convert to base 10
        int subtractionResult = numDecreasing - numIncreasing;           // Perform subtraction in base 10
        return convertIntToPaddedDigits(subtractionResult, n, base);     // Convert result back to digits in the specified base
    }
    
    /**
     * Helper method to sort digits in decreasing order using Quicksort.
     * @param digits The array of digits to sort.
     * @return A new array with digits sorted in decreasing order.
     */
    private static int[] getDecreasingOrder(int[] digits) {
        int[] result = digits.clone(); 
        quickSortDescending(result, 0, result.length - 1);
        return result;
    }

    /**
     * Recursive Quicksort implementation for descending order.
     * @param arr The array to be sorted.
     * @param low The starting index of the subarray.
     * @param high The ending index of the subarray.
     */
    private static void quickSortDescending(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partitionDescending(arr, low, high);
            quickSortDescending(arr, low, pi - 1);
            quickSortDescending(arr, pi + 1, high);
        }
    }

    /**
     * This function takes last element as pivot, places the pivot element at its correct
     * position in sorted array, and places all greater (for descending) elements to left
     * of pivot and all smaller elements to right of pivot.
     * @param arr The array to be partitioned.
     * @param low The starting index of the subarray.
     * @param high The ending index of the subarray (pivot chosen here).
     * @return The partitioning index.
     */
    private static int partitionDescending(int[] arr, int low, int high) {
        int pivot = arr[high]; 
        int i = (low - 1); 

        for (int j = low; j < high; j++) {
            if (arr[j] >= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    /**
     * Helper method to sort digits in increasing order using Quicksort.
     * @param digits The array of digits to sort.
     * @return A new array with digits sorted in increasing order.
     */
    private static int[] getIncreasingOrder(int[] digits) {
        int[] result = digits.clone(); 
        quickSortIncreasing(result, 0, result.length - 1);
        return result;
    }

    /**
     * Recursive Quicksort implementation for increasing order.
     * @param arr The array to be sorted.
     * @param low The starting index of the subarray.
     * @param high The ending index of the subarray.
     */
    private static void quickSortIncreasing(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partitionIncreasing(arr, low, high);
            quickSortIncreasing(arr, low, pi - 1);
            quickSortIncreasing(arr, pi + 1, high);
        }
    }

    /**
     * This function takes last element as pivot, places the pivot element at its correct
     * position in sorted array, and places all smaller (for increasing) elements to left
     * of pivot and all greater elements to right of pivot.
     * @param arr The array to be partitioned.
     * @param low The starting index of the subarray.
     * @param high The ending index of the subarray (pivot chosen here).
     * @return The partitioning index.
     */
    private static int partitionIncreasing(int[] arr, int low, int high) {
        int pivot = arr[high]; 
        int i = (low - 1); 

        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    /**
     * Checks if a number (represented by its digits) is a valid candidate for Kaprekar's Routine.
     * A valid candidate must:
     * 1. Have at least two distinct digits (to ensure descending and ascending numbers are different).
     * 2. If it's a multi-digit number (n > 1), its first digit must not be 0.
     * @param digits The array of digits representing the number.
     * @param base The base of the number system.
     * @return true if the number is a valid Kaprekar candidate, false otherwise.
     */
    private static boolean checkedNumber(int[] digits, int base) {
        int n = digits.length;

        // Condition 2: If multi-digit, must not start with zero.
        if (n > 1 && digits[0] == 0) {
            return false;
        }

        // Condition 1: Must have at least two distinct digits.
        boolean allSame = true;
        for (int i = 1; i < n; i++) {
            if (digits[i] != digits[0]) {
                allSame = false;
                break;
            }
        }
        return !allSame; // If all digits are the same, it's not a valid Kaprekar number.
    }

    /**
     * This method generates ALL valid n-digit numbers as per the `checkedNumber` logic
     * for a given base.
     * @param n The number of digits.
     * @param base The base of the number system.
     * @return A list of valid n-digit numbers as arrays of digits.
     */
    public static List<int[]> generateAllNumbers(int n, int base) {
        List<int[]> validNumbers = new ArrayList<>();
        int[] digits = new int[n];
        generateAllDigitsRecursive(digits, 0, n, validNumbers, base);
        return validNumbers;
    }

    /**
     * Recursive helper for `generateAllNumbers` to build all digit combinations
     * for a given base.
     * @param digits The current digit array being built.
     * @param position The current digit position being set (0-indexed).
     * @param n The total number of digits for the number.
     * @param validNumbers The list to add valid numbers to.
     * @param base The base of the number system.
     */
    private static void generateAllDigitsRecursive(int[] digits, int position, int n, List<int[]> validNumbers, int base) {
        if (position == n) {
            // Once all positions are filled, check if the number is valid
            if (checkedNumber(digits, base)) {
                validNumbers.add(digits.clone()); // Add a clone to avoid modification issues
            }
            return;
        }

        // Determine the starting digit for the current position:
        // If it's the first digit (position == 0) and it's a multi-digit number (n > 1),
        // the first digit cannot be 0. Otherwise, it can be 0.
        int startDigit = (position == 0 && n > 1) ? 1 : 0; 

        // Digits range from 0 to (base - 1)
        for (int digit = startDigit; digit < base; digit++) {
            digits[position] = digit;
            generateAllDigitsRecursive(digits, position + 1, n, validNumbers, base);
        }
    }
} // End of class o7_16_2025
