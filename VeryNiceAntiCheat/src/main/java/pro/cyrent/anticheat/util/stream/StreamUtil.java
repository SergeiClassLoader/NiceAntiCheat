package pro.cyrent.anticheat.util.stream;

import com.google.common.collect.Lists;
import pro.cyrent.anticheat.util.math.Pair;
import pro.cyrent.anticheat.util.math.Tuple;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamUtil {

    public static <T> Collection<T> filter(Collection<T> data, Predicate<T> filter) {

        List<T> list = new LinkedList<>();

        if (filter == null || data.isEmpty()) return list;

        for (T object : data) {

            if (filter.test(object)) list.add(object);
        }

        return list;
    }

    public static double getAverageNumberDifference(List<Double> doubles) {
        if (doubles == null || doubles.size() < 2) {
            // Handle the case where there are not enough elements in the list
            return Double.NaN; // Not-a-Number to represent an undefined average
        }

        double sumDifference = 0.0;

        for (int i = 0; i < doubles.size() - 1; i++) {
            double diff = Math.abs(doubles.get(i + 1) - doubles.get(i));
            sumDifference += diff;
        }

        // Calculate the average difference
        return sumDifference / (doubles.size() - 1);
    }


    public static double mean(Collection<? extends Number> samples) {
        double sum = 0D;

        for (Number val : samples) sum += val.doubleValue();

        return sum / samples.size();
    }


    public static double calculateSerialCorrelation(Collection<? extends Number> data) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data series must contain at least two elements.");
        }

        double mean = mean(data);
        double numerator = 0.0;
        double denominator = 0.0;

        Number[] dataArray = data.toArray(new Number[0]);

        for (int i = 0; i < dataArray.length - 1; i++) {
            numerator += (dataArray[i].doubleValue() - mean) * (dataArray[i + 1].doubleValue() - mean);
        }

        for (Number number : dataArray) {
            denominator += Math.pow(number.doubleValue() - mean, 2);
        }

        return numerator / denominator;
    }

    public static double giniCoefficient(Collection<? extends Number> data) {
        if (data == null || data.size() < 2) {
            throw new IllegalArgumentException("Data series must contain at least two elements.");
        }

        Number[] dataArray = data.toArray(new Number[0]);
        int n = dataArray.length;
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = dataArray[i].doubleValue();
        }

        // Sort the values
        java.util.Arrays.sort(values);

        double cumulativeSum = 0;
        double cumulativeValuesSum = 0;
        for (int i = 0; i < n; i++) {
            cumulativeValuesSum += values[i];
            cumulativeSum += cumulativeValuesSum;
        }

        double mean = cumulativeValuesSum / n;
        return (n + 1.0 - 2.0 * cumulativeSum / cumulativeValuesSum) / n;
    }

    public static double getEntropy(Collection<? extends Number> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data series must not be empty.");
        }

        Map<Double, Integer> frequencyMap = new HashMap<>();
        for (Number number : data) {
            double value = number.doubleValue();
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }

        double entropy = 0.0;
        double total = data.size();

        for (Map.Entry<Double, Integer> entry : frequencyMap.entrySet()) {
            double probability = entry.getValue() / total;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        return entropy;
    }

    public static double getGrid(final List<Float> entry) {
        /*
         * We're creating the variables average min and max to start calculating the possibility of cinematic camera.
         * Why does this work? Cinematic camera is essentially a slowly increasing slowdown (which is why cinematic camera
         * becomes slower the more you use it) which in turn makes it so the min max and average are extremely close together.
         */
        double average = 0.0;
        double min = 0.0, max = 0.0;

        /*
         * These are simple min max calculations done manually for the sake of simplicity. We're using the numbers 0.0
         * since we also want to account for the possibility of a negative number. If there are no negative numbers then
         * there is absolutely no need for us to care about that number other than getting the max.
         */
        for (final double number : entry) {
            if (number < min) min = number;
            if (number > max) max = number;

            /*
             * Instead of having a sum variable we can use an average variable which we divide
             * right after the loop is over. Smart programming trick if you want to use it.
             */
            average += number;
        }

        /*
         * We're dividing the average by the length since this is the formula to getting the average.
         * Specifically its (sum(n) / length(n)) = average(n) -- with n being the entry set we're analyzing.
         */
        average /= entry.size();

        /*
         * This is going to estimate how close the average and the max were together with the possibility of a min
         * variable which is going to represent a negative variable since the preset variable on min is 0.0.
         */
        return (max - average) - min;
    }


    public static double getGridDouble(final Collection<Double> entry) {
        double average = 0.0;
        double min = 0.0, max = 0.0;

        for (final double number : entry) {
            if (number < min) min = number;
            if (number > max) max = number;
            average += number;
        }

        average /= entry.size();

        return (max - average) - min;
    }

    public static <T extends Number> T getModeV2(Collection<T> collect) {
        Map<T, Integer> repeated = new HashMap<>();

        //Sorting each value by how to repeat into a map.
        collect.forEach(val -> {
            int number = repeated.getOrDefault(val, 0);

            repeated.put(val, number + 1);
        });

        //Calculating the largest value to the key, which would be the mode.
        return repeated.keySet().stream()
                .map(key -> new Tuple<>(key, repeated.get(key))) //We map it into a Tuple for easier sorting.
                .max(Comparator.comparing(tup -> tup.two, Comparator.naturalOrder()))
                .orElseThrow(NullPointerException::new).one;
    }


    public static Number getMode(Collection<? extends Number> samples) {
        Map<Number, Integer> frequencies = new HashMap<>();

        samples.forEach(i -> frequencies.put(i, frequencies.getOrDefault(i, 0) + 1));

        Number mode = null;
        int highest = 0;

        for (var entry : frequencies.entrySet()) {
            if (entry.getValue() > highest) {
                mode = entry.getKey();
                highest = entry.getValue();
            }
        }

        return mode;
    }

    public static double getCPS(Collection<? extends Number> values) {
        return 20 / getAverage(values);
    }


    /**
     * @param - collection The collection of the numbers you want to get the duplicates from
     * @return - The duplicate amount
     */
    public static int getDuplicates(final Collection<? extends Number> collection) {
        return collection.size() - getDistinct(collection);
    }


    public static double getKurtosis(Collection<? extends Number> values) {
        double n = values.size();

        if (n < 3)
            return Double.NaN;

        double average = getAverage(values);
        double stDev = getStandardDeviation(values);

        AtomicDouble accum = new AtomicDouble(0D);

        values.forEach(delay -> accum.getAndAdd(Math.pow(delay.doubleValue() - average, 4D)));

        return n * (n + 1) / ((n - 1) * (n - 2) * (n - 3)) *
                (accum.get() / Math.pow(stDev, 4D)) - 3 *
                Math.pow(n - 1, 2D) / ((n - 2) * (n - 3));
    }

    public synchronized static double getAverage(Collection<? extends Number> values) {
        return values.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0D);
    }

    public static double getAverageV(final Collection<? extends Number> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (final Number number : data) {
            sum += number.doubleValue();
        }
        return sum / data.size();
    }


    public static double getSkewness(Collection<? extends Number> data) {
        double sum = 0;
        int count = 0;

        final List<Double> numbers = Lists.newArrayList();

        // Get the sum of all the data and the amount via looping
        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;

            numbers.add(number.doubleValue());
        }

        // Sort the numbers to run the calculations in the next part
        Collections.sort(numbers);

        // Run the formula to get skewness
        final double mean =  sum / count;
        final double median = (count % 2 != 0) ? numbers.get(count / 2) : (numbers.get((count - 1) / 2) + numbers.get(count / 2)) / 2;
        final double variance = getVariance(data);

        return 3 * (mean - median) / variance;
    }

    public static double deviationSquared(Iterable<? extends Number> iterable) {
        double n = 0.0;
        int n2 = 0;

        for (Number anIterable : iterable) {
            n += (anIterable).doubleValue();
            ++n2;
        }

        double n3 = n / n2;
        double n4 = 0.0;

        for (Number anIterable : iterable) {
            n4 += Math.pow(anIterable.doubleValue() - n3, 2.0);
        }

        return (n4 == 0.0) ? 0.0 : (n4 / (n2 - 1));
    }

    public static double[] getMagnitudes(Collection<? extends Number> data) {
        int n = data.size();

        // Convert Collection<? extends Number> to double[]
        double[] dataArray = new double[n];

        int i = 0;
        for (Number number : data) {
            dataArray[i++] = number.doubleValue();
        }

        // Pad the data to the next power of 2
        int paddedSize = 1;
        while (paddedSize < n) {
            paddedSize <<= 1;
        }

        // Create a new array with the padded size
        double[] paddedArray = new double[paddedSize];
        System.arraycopy(dataArray, 0, paddedArray, 0, n);

        // Perform FFT
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftResultComplex = transformer.transform(paddedArray, TransformType.FORWARD);

        // Calculate magnitudes
        double[] magnitudes = new double[paddedSize / 2];
        for (i = 0; i < paddedSize / 2; i++) {
            magnitudes[i] = fftResultComplex[i].abs(); // Get the magnitude of the complex number
        }

        return magnitudes;
    }

    public static double getAverageMagnitude(double[] magnitudes) {
        double sum = 0.0;
        for (double magnitude : magnitudes) {
            sum += magnitude;
        }
        return sum / magnitudes.length;
    }

    public static double getMedian(Iterable<? extends Number> iterable) {
        List<Double> data = new ArrayList<>();

        for (Number number : iterable) {
            data.add(number.doubleValue());
        }

        return getMedian(data);
    }

    public static double getDeviation(final Collection<? extends Number> nums) {
        if (nums.isEmpty()) return 0D;

        return Math.sqrt((getVariance(nums) / (nums.size() - 1)));
    }

    public static double getVariance(final Collection<? extends Number> data) {
        if (data.isEmpty()) return 0D;

        int count = 0;

        double sum = 0.0;
        double variance = 0.0;

        double average;

        // Increase the sum and the count to find the average and the standard deviation
        for (final Number number : data) {
            sum += number.doubleValue();
            ++count;
        }

        average = sum / count;

        // Run the standard deviation formula
        for (final Number number : data) {
            variance += Math.pow(number.doubleValue() - average, 2.0);
        }

        return variance;
    }

    public static int getDistinct(final Collection<? extends Number> collection) {
        if (collection.isEmpty()) return 0;

        return new HashSet<>(collection).size();
    }

    public static double getMaximumDouble(final Collection<Double> nums) {
        if (nums.isEmpty()) return 0.0d;

        double max = Double.MIN_VALUE;

        for (final double val : nums) {
            if (val > max) max = val;
        }

        return max;
    }

    public static double getStandardDeviation(Collection<? extends Number> values) {
        double average = getAverage(values);
        AtomicDouble variance = new AtomicDouble(0D);

        values.forEach(delay -> variance.getAndAdd(Math.pow(delay.doubleValue() - average, 2D)));

        return Math.sqrt(variance.get() / values.size());
    }

    public static double getMedian(List<Double> data) {
        if (data.size() > 1) {
            if (data.size() % 2 == 0)
                return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
            else
                return data.get(Math.round(data.size() / 2f));
        }
        return 0;
    }

    public static Pair<List<Double>, List<Double>> getOutliersV2(final Collection<? extends Number> collection) {
        final List<Double> values = new ArrayList<>();

        for (final Number number : collection) {
            values.add(number.doubleValue());
        }

        final double q1 = getMedian(values.subList(0, values.size() / 2));
        final double q3 = getMedian(values.subList(values.size() / 2, values.size()));

        final double iqr = Math.abs(q1 - q3);
        final double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        final Pair<List<Double>, List<Double>> tuple = new Pair<>(new ArrayList<>(), new ArrayList<>());

        for (final Double value : values) {
            if (value < lowThreshold) {
                tuple.getK().add(value);
            } else if (value > highThreshold) {
                tuple.getV().add(value);
            }
        }

        return tuple;
    }

    public static Tuple<List<Double>, List<Double>> getOutliers(Collection<? extends Number> collection) {
        List<Double> values = new ArrayList<>();

        for (Number number : collection) {
            values.add(number.doubleValue());
        }

        if (values.size() < 4) return new Tuple<>(new ArrayList<>(), new ArrayList<>());

        double q1 = getMedian(values.subList(0, values.size() / 2)),
                q3 = getMedian(values.subList(values.size() / 2, values.size()));
        double iqr = Math.abs(q1 - q3);

        double lowThreshold = q1 - 1.5 * iqr, highThreshold = q3 + 1.5 * iqr;

        Tuple<List<Double>, List<Double>> tuple = new Tuple<>(new ArrayList<>(), new ArrayList<>());

        for (Double value : values) {
            if (value < lowThreshold) tuple.one.add(value);
            else if (value > highThreshold) tuple.two.add(value);
        }

        return tuple;
    }

    public static int getMaximumInt(final Collection<Integer> nums) {
        if (nums.isEmpty()) return 0;

        int max = Integer.MIN_VALUE;

        for (final int val : nums) {
            if (val > max) max = val;
        }

        return max;
    }

    public static long getMaximumLong(final Collection<Long> nums) {
        if (nums.isEmpty()) return 0L;

        long max = Long.MIN_VALUE;

        for (final long val : nums) {

            if (val > max) max = val;
        }

        return max;
    }

    public static float getMaximumFloat(final Collection<Float> nums) {
        if (nums.isEmpty()) return 0.0f;

        float max = Float.MIN_VALUE;

        for (final float val : nums) {

            if (val > max) max = val;
        }

        return max;
    }

    public static double getMinimumDouble(final Collection<Double> nums) {
        if (nums.isEmpty()) return 0.0d;

        double min = Double.MAX_VALUE;

        for (final double val : nums) {

            if (val < min) min = val;
        }

        return min;
    }

    public static int getMinimumInt(final Collection<Integer> nums) {
        if (nums.isEmpty()) return 0;

        int min = Integer.MAX_VALUE;

        for (final int val : nums) {

            if (val < min) min = val;
        }

        return min;
    }

    public static long getMinimumLong(final Collection<Long> nums) {
        if (nums.isEmpty()) return 0L;

        long min = Long.MAX_VALUE;

        for (final long val : nums) {

            if (val < min) min = val;
        }

        return min;
    }

    public static float getMinimumFloat(final Collection<Float> nums) {
        if (nums.isEmpty()) return 0.0f;

        float min = Float.MAX_VALUE;

        for (final float val : nums) {

            if (val < min) min = val;
        }

        return min;
    }

    public synchronized static <T> boolean anyMatch(final List<T> objects, final Predicate<T> condition) {
        if (condition == null) return false;

        for (final T object : objects) {

            if (condition.test(object)) return true;
        }

        return false;
    }

    public static double calculateSum(List<Integer> numbers) {
        return numbers.stream().mapToDouble(Integer::doubleValue).sum();
    }

    public static double calculateProduct(List<Integer> numbers) {
        return numbers.stream().mapToDouble(Integer::doubleValue).reduce(1, (a, b) -> a * b);
    }

    public static double calculateSumOfSquares(List<Integer> numbers) {
        return numbers.stream().mapToDouble(num -> Math.pow(num, 2)).sum();
    }

    // Method to calculate the harmonic mean of the numbers in the list
    public static double calculateHarmonicMean(List<Integer> numbers) {
        if (numbers.isEmpty()) {
            return -1;
        }

        double reciprocalSum = numbers.stream().mapToDouble(num -> 1.0 / num).sum();
        return numbers.size() / reciprocalSum;
    }

    public static double calculateGeometricMean(List<Integer> numbers) {
        if (numbers.isEmpty()) {
        //    throw new IllegalArgumentException("List cannot be empty");
            return -1;
        }

        double product = numbers.stream().mapToDouble(Integer::doubleValue).reduce(1, (a, b) -> a * b);
        return Math.pow(product, 1.0 / numbers.size());
    }

    // Method to count the occurrences of each number in the list
    public static Map<Integer, Long> countOccurrences(List<Integer> numbers) {
        return numbers.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    public static List<Double> calculateMovingAverageInt(List<Integer> numbers, int windowSize) {
        if (windowSize <= 0 || windowSize > numbers.size()) {
            return new ArrayList<>();
        }

        return IntStream.rangeClosed(0, numbers.size() - windowSize)
                .mapToDouble(i -> numbers.subList(i, i + windowSize).stream().mapToDouble(Integer::doubleValue).average().orElse(0))
                .boxed()
                .collect(Collectors.toList());
    }

    // Method to check if the list is a palindrome
    public static boolean isPalindrome(List<Integer> numbers) {
        int size = numbers.size();
        for (int i = 0; i < size / 2; i++) {
            if (!numbers.get(i).equals(numbers.get(size - i - 1))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isPalindromeF(List<Float> numbers) {
        int size = numbers.size();
        for (int i = 0; i < size / 2; i++) {
            if (!numbers.get(i).equals(numbers.get(size - i - 1))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isConsecutivePermutation(List<Integer> numbers) {
        int n = numbers.size();
        List<Integer> sortedList = numbers.stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < n; i++) {
            if (sortedList.get(i) != i + sortedList.get(0)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isConsecutivePermutationF(List<Float> numbers) {
        int n = numbers.size();
        List<Float> sortedList = numbers.stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < n; i++) {
            if (sortedList.get(i) != i + sortedList.get(0)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isArithmeticProgression(List<Integer> numbers) {
        if (numbers.size() < 2) {
            return true; // A single element is always an arithmetic progression
        }

        int commonDifference = numbers.get(1) - numbers.get(0);

        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) - numbers.get(i - 1) != commonDifference) {
                return false;
            }
        }

        return true;
    }

    public static boolean isArithmeticProgressionF(List<Float> numbers) {
        if (numbers.size() < 2) {
            return true; // A single element is always an arithmetic progression
        }

        float commonDifference = numbers.get(1) - numbers.get(0);

        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) - numbers.get(i - 1) != commonDifference) {
                return false;
            }
        }

        return true;
    }

    // Method to check if the list is a geometric progression
    public static boolean isGeometricProgression(List<Integer> numbers) {
        if (numbers.size() < 2) {
            return true; // A single element is always a geometric progression
        }

        int commonRatio = numbers.get(1) / numbers.get(0);

        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) / numbers.get(i - 1) != commonRatio) {
                return false;
            }
        }

        return true;
    }

    public static boolean isGeometricProgressionF(List<Float> numbers) {
        if (numbers.size() < 2) {
            return true; // A single element is always a geometric progression
        }

        float commonRatio = numbers.get(1) / numbers.get(0);

        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) / numbers.get(i - 1) != commonRatio) {
                return false;
            }
        }

        return true;
    }


    // Method to calculate the difference between consecutive elements
    public static List<Integer> calculateDifferences(List<Integer> numbers) {
        if (numbers.size() < 2) {
            throw new IllegalArgumentException("List should have at least two elements for differences");
        }

        return IntStream.range(1, numbers.size())
                .mapToObj(i -> numbers.get(i) - numbers.get(i - 1))
                .collect(Collectors.toList());
    }

    public static int identifyTrend(List<Integer> numbers) {
        List<Integer> differences = calculateDifferences(numbers);

        long increasingCount = differences.stream().filter(diff -> diff > 0).count();
        long decreasingCount = differences.stream().filter(diff -> diff < 0).count();

        if (increasingCount == differences.size()) {
            return 1;
        } else if (decreasingCount == differences.size()) {
            return -1;
        } else {
            return 0;
        }
    }



    public static <T> boolean allMatch(final Collection<T> collection, final Predicate<T> condition) {
        if (condition == null) return false;

        for (final T object : collection) {

            if (!condition.test(object)) return false;
        }

        return true;
    }

    public static <T> List<T> getFiltered(final Collection<T> data, final Predicate<T> filter) {

        final List<T> list = new LinkedList<>();

        if (filter == null || data.isEmpty()) return list;

        for (final T object : data) {

            if (filter.test(object)) list.add(object);
        }

        return list;
    }

    public static int filteredCount(final Collection<? extends Number> data, final Predicate<Number> filter) {
        if (filter == null || data.isEmpty()) return 0;

        int count = 0;

        for (final Number num : data) {

            if (filter.test(num)) count++;
        }

        return count;
    }
}
