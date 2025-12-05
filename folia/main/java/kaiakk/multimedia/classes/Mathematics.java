package kaiakk.multimedia.classes;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A grab-bag of small, useful math utilities for plugins.
 * All methods are static and intended to be easy to use from other plugins.
 */
public final class Mathematics {

	private Mathematics() {}

	public static int clamp(int val, int min, int max) {
		return Math.max(min, Math.min(max, val));
	}

	public static double clamp(double val, double min, double max) {
		return Math.max(min, Math.min(max, val));
	}

	public static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	public static double map(double value, double inMin, double inMax, double outMin, double outMax) {
		if (inMax - inMin == 0) return outMin; // avoid divide by zero
		return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
	}

	public static boolean approxEquals(double a, double b, double eps) {
		return Math.abs(a - b) <= Math.abs(eps);
	}

	public static long gcd(long a, long b) {
		a = Math.abs(a);
		b = Math.abs(b);
		while (b != 0) {
			long t = b;
			b = a % b;
			a = t;
		}
		return a;
	}

	public static long lcm(long a, long b) {
		if (a == 0 || b == 0) return 0;
		return Math.abs(a / gcd(a, b) * b);
	}

	public static boolean isPrime(int n) {
		if (n <= 1) return false;
		if (n <= 3) return true;
		if (n % 2 == 0 || n % 3 == 0) return false;
		int i = 5;
		while ((long) i * i <= n) {
			if (n % i == 0 || n % (i + 2) == 0) return false;
			i += 6;
		}
		return true;
	}

	public static BigInteger factorial(int n) {
		if (n < 0) throw new IllegalArgumentException("n must be >= 0");
		BigInteger r = BigInteger.ONE;
		for (int i = 2; i <= n; i++) r = r.multiply(BigInteger.valueOf(i));
		return r;
	}

	public static BigInteger permutation(int n, int r) {
		if (n < 0 || r < 0 || r > n) throw new IllegalArgumentException("invalid n or r");
		BigInteger res = BigInteger.ONE;
		for (int i = 0; i < r; i++) res = res.multiply(BigInteger.valueOf(n - i));
		return res;
	}

	public static BigInteger combination(int n, int r) {
		if (n < 0 || r < 0 || r > n) throw new IllegalArgumentException("invalid n or r");
		if (r == 0 || r == n) return BigInteger.ONE;
		r = Math.min(r, n - r);
		BigInteger num = BigInteger.ONE;
		BigInteger den = BigInteger.ONE;
		for (int i = 1; i <= r; i++) {
			num = num.multiply(BigInteger.valueOf(n - r + i));
			den = den.multiply(BigInteger.valueOf(i));
		}
		return num.divide(den);
	}

	public static int randomInt(int minInclusive, int maxInclusive) {
		if (minInclusive > maxInclusive) throw new IllegalArgumentException("min > max");
		return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
	}

	public static double randomDouble(double minInclusive, double maxExclusive) {
		if (minInclusive >= maxExclusive) throw new IllegalArgumentException("min >= max");
		return ThreadLocalRandom.current().nextDouble(minInclusive, maxExclusive);
	}

	public static boolean chance(double percent) {
		if (percent <= 0) return false;
		if (percent >= 100) return true;
		return ThreadLocalRandom.current().nextDouble(0.0, 100.0) < percent;
	}

	public static double roundTo(double value, int decimals) {
		if (decimals < 0) throw new IllegalArgumentException("decimals must be >= 0");
		double mul = Math.pow(10.0, decimals);
		return Math.round(value * mul) / mul;
	}

	public static int nearestPowerOfTwo(int v) {
		if (v <= 1) return 1;
		int p = 1;
		while (p < v) p <<= 1;
		return p;
	}

	public static String formatWithSuffix(long value) {
		long abs = Math.abs(value);
		if (abs < 1_000) return Long.toString(value);
		if (abs < 1_000_000) return String.format("%.1fK", value / 1_000.0);
		if (abs < 1_000_000_000) return String.format("%.1fM", value / 1_000_000.0);
		return String.format("%.1fB", value / 1_000_000_000.0);
	}

	public static double degToRad(double deg) { return deg * Math.PI / 180.0; }
	public static double radToDeg(double rad) { return rad * 180.0 / Math.PI; }

	public static double normalizeAngleDeg(double deg) {
		double r = deg % 360.0;
		if (r < -180) r += 360.0;
		if (r > 180) r -= 360.0;
		return r;
	}

	public static double distance2D(double x1, double y1, double x2, double y2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		return Math.hypot(dx, dy);
	}

	public static double distance3D(double x1, double y1, double z1, double x2, double y2, double z2) {
		double dx = x1 - x2;
		double dy = y1 - y2;
		double dz = z1 - z2;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public static double safeDivide(double a, double b, double fallback) {
		if (b == 0.0) return fallback;
		return a / b;
	}

	public static <T extends Number> double sum(List<T> list) {
		if (list == null) return 0.0;
		double s = 0.0;
		for (Number n : list) if (n != null) s += n.doubleValue();
		return s;
	}

}
