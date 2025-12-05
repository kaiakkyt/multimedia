package kaiakk.multimedia.classes;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64.Encoder;
import java.util.Base64.Decoder;

/**
 * Extremely easy Base64 helper for plugins.
 * Provides encode/decode helpers for strings/bytes, file read/write, and resource handling.
 */
public final class Base64 {

	private Base64() {}

	private static final Encoder ENCODER = java.util.Base64.getEncoder();
	private static final Decoder DECODER = java.util.Base64.getDecoder();

	public static String encode(String input) {
		if (input == null) return null;
		return ENCODER.encodeToString(input.getBytes(StandardCharsets.UTF_8));
	}

	public static String encodeBytes(byte[] bytes) {
		if (bytes == null) return null;
		return ENCODER.encodeToString(bytes);
	}

	public static String decodeToString(String base64) {
		if (base64 == null) return null;
		byte[] decoded = DECODER.decode(base64);
		return new String(decoded, StandardCharsets.UTF_8);
	}

	public static byte[] decodeToBytes(String base64) {
		if (base64 == null) return null;
		return DECODER.decode(base64);
	}

	public static String encodeFileToBase64(Path file) throws IOException {
		byte[] data = Files.readAllBytes(file);
		return encodeBytes(data);
	}

	public static void decodeBase64ToFile(String base64, Path outFile) throws IOException {
		byte[] data = decodeToBytes(base64);
		if (outFile.getParent() != null) Files.createDirectories(outFile.getParent());
		Files.write(outFile, data);
	}

	public static String readBase64TextFile(Path textFile) throws IOException {
		return new String(Files.readAllBytes(textFile), StandardCharsets.UTF_8);
	}

	public static void writeBase64TextFile(String base64, Path textFile) throws IOException {
		if (textFile.getParent() != null) Files.createDirectories(textFile.getParent());
		Files.write(textFile, base64.getBytes(StandardCharsets.UTF_8));
	}

	public static String encodeFileToBase64Safe(Path file) {
		try { return encodeFileToBase64(file); } catch (IOException e) { return null; }
	}

	public static boolean decodeBase64ToFileSafe(String base64, Path outFile) {
		try { decodeBase64ToFile(base64, outFile); return true; } catch (IOException e) { return false; }
	}

	public static String readBase64TextFileSafe(Path textFile) {
		try { return readBase64TextFile(textFile); } catch (IOException e) { return null; }
	}

	public static boolean writeBase64TextFileSafe(String base64, Path textFile) {
		try { writeBase64TextFile(base64, textFile); return true; } catch (IOException e) { return false; }
	}

	public static String encodeResourceToBase64(JavaPlugin plugin, String resourcePath) throws IOException {
		if (plugin == null) throw new IllegalArgumentException("plugin is null");
		try (InputStream is = plugin.getResource(resourcePath)) {
			if (is == null) throw new FileNotFoundException(resourcePath);
			java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			return encodeBytes(buffer.toByteArray());
		}
	}

	public static String encodeResourceToBase64Safe(JavaPlugin plugin, String resourcePath) {
		try { return encodeResourceToBase64(plugin, resourcePath); } catch (IOException e) { return null; }
	}

	public static boolean decodeResourceBase64ToFileSafe(JavaPlugin plugin, String resourcePath, Path outFile) {
		String base64 = encodeResourceToBase64Safe(plugin, resourcePath);
		if (base64 == null) return false;
		return decodeBase64ToFileSafe(base64, outFile);
	}

	public static String encodeFileInDataFolder(JavaPlugin plugin, String relativePath) throws IOException {
		Path p = plugin.getDataFolder().toPath().resolve(relativePath);
		return encodeFileToBase64(p);
	}

	public static boolean decodeBase64ToDataFolderFileSafe(JavaPlugin plugin, String base64, String relativeOutPath) {
		Path p = plugin.getDataFolder().toPath().resolve(relativeOutPath);
		return decodeBase64ToFileSafe(base64, p);
	}

	/**
	 * Build a data URI from raw bytes and mime type. If mimeType is null/empty,
	 * `application/octet-stream` is used.
	 */
	public static String toDataUriFromBytes(byte[] data, String mimeType) {
		if (data == null) return null;
		String mt = (mimeType == null || mimeType.trim().isEmpty()) ? "application/octet-stream" : mimeType;
		return "data:" + mt + ";base64," + encodeBytes(data);
	}

	/** Encode a string (UTF-8) to a data URI with given mime type. */
	public static String encodeToDataUri(String input, String mimeType) {
		if (input == null) return null;
		return toDataUriFromBytes(input.getBytes(StandardCharsets.UTF_8), mimeType);
	}

	/** Encode a file to a data URI (throws IOException). */
	public static String encodeFileToDataUri(Path file, String mimeType) throws IOException {
		byte[] data = Files.readAllBytes(file);
		return toDataUriFromBytes(data, mimeType);
	}

	/** Encode a bundled resource to a data URI (throws IOException). */
	public static String encodeResourceToDataUri(JavaPlugin plugin, String resourcePath, String mimeType) throws IOException {
		if (plugin == null) throw new IllegalArgumentException("plugin is null");
		try (InputStream is = plugin.getResource(resourcePath)) {
			if (is == null) throw new FileNotFoundException(resourcePath);
			java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			return toDataUriFromBytes(buffer.toByteArray(), mimeType);
		}
	}

	/** Safe wrapper for encodeFileToDataUri */
	public static String encodeFileToDataUriSafe(Path file, String mimeType) {
		try { return encodeFileToDataUri(file, mimeType); } catch (IOException e) { return null; }
	}

	/**
	 * Simple holder for parsed data URI values.
	 */
	public static final class DataUri {
		public final String mimeType;
		public final String base64; // base64 payload (without the data:...;base64, prefix)

		public DataUri(String mimeType, String base64) {
			this.mimeType = mimeType == null || mimeType.trim().isEmpty() ? "application/octet-stream" : mimeType;
			this.base64 = base64;
		}

		public byte[] toBytes() { return DECODER.decode(base64); }

		public String toDecodedString() { return new String(toBytes(), StandardCharsets.UTF_8); }
	}

	/**
	 * Parse a data URI (base64). Returns null if the input is not a valid base64 data URI.
	 */
	public static DataUri parseDataUri(String dataUri) {
		if (dataUri == null) return null;
		if (!dataUri.startsWith("data:")) return null;
		int comma = dataUri.indexOf(',');
		if (comma < 0) return null;
		String meta = dataUri.substring(5, comma); // after "data:"
		String payload = dataUri.substring(comma + 1);
		boolean isBase64 = meta.contains(";base64");
		String mime = meta.replace(";base64", "");
		if (!isBase64) {
			// not base64 encoded; try URL-decoding and then base64-encode the result
			try {
				String decoded = java.net.URLDecoder.decode(payload, StandardCharsets.UTF_8.name());
				String b64 = ENCODER.encodeToString(decoded.getBytes(StandardCharsets.UTF_8));
				return new DataUri(mime, b64);
			} catch (Exception e) {
				return null;
			}
		}
		// payload is base64; return as-is
		return new DataUri(mime, payload);
	}

	/** Safe parse that returns null on error (same as parseDataUri but kept for symmetry). */
	public static DataUri parseDataUriSafe(String dataUri) { return parseDataUri(dataUri); }

	/** Quick check whether a string looks like a data URI. */
	public static boolean isDataUri(String s) { return s != null && s.startsWith("data:"); }

}
