package globalquake.core.intensity;

import org.tinylog.Logger;

public class IntensityTable {

    public static double getIntensity(double mag, double dist) {
        mag = 1.2 * mag - 0.022 * mag * mag - 1;
        if (dist > 1200) {
            dist = 1200 + Math.pow(dist - 1200, 0.4) * 22.0;
        }
        return (Math.pow(15, mag * 0.92 + 4.0)) / (5 * Math.pow(dist, 2.1 + 0.07 * mag) + 1000 + 1 * Math.pow(5, mag));

    }

    public static double getMagnitude(double dist, double intensity) {
        double epsilon = 1e-6; // Tolerance for floating-point comparison
        double low = -2.0;
        double high = 10.0;

        // Perform binary search
        while (low <= high) {
            double mid = low + (high - low) / 2;
            double currentIntensity = getIntensity(mid, dist);

            if (Math.abs(currentIntensity - intensity) < epsilon) {
                // Found a close enough match
                return mid;
            } else if (currentIntensity < intensity) {
                // Adjust the search range
                low = mid + epsilon;
            } else {
                high = mid - epsilon;
            }
        }

        // If no exact match is found, return an approximation
        return low;
    }

}
