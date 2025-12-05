package kaiakk.multimedia.classes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ColorConverter {

    public static final char SECTION_SIGN = '\u00A7';

        private static final Pattern HEX_REGEX = Pattern.compile("&#([a-fA-F0-9]{6})");

        private static final Pattern STRIP_PATTERN = Pattern.compile(
            "(?i)" + SECTION_SIGN + "(?:[0-9A-FK-OR]|x(?:" + SECTION_SIGN + "[0-9A-F]){6})"
        );

    public static String translateAmpersandToSection(String textToTranslate) {
        if (textToTranslate == null) {
            return null;
        }
        return textToTranslate.replace('&', SECTION_SIGN);
    }

    public static String translateSectionToAmpersand(String coloredText) {
        if (coloredText == null) {
            return null;
        }
        return coloredText.replace(SECTION_SIGN, '&');
    }

    public static String colorize(String text) {
        if (text == null) {
            return null;
        }

        String coloredText = text;
        Matcher matcher = HEX_REGEX.matcher(coloredText);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder hexBuilder = new StringBuilder();
            hexBuilder.append(SECTION_SIGN).append('x');
            for (char character : hexColor.toCharArray()) {
                hexBuilder.append(SECTION_SIGN).append(character);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(hexBuilder.toString()));
        }
        matcher.appendTail(buffer);
        coloredText = buffer.toString();

        return coloredText.replace('&', SECTION_SIGN);
    }

    public static String stripColor(String text) {
        if (text == null) {
            return null;
        }
        return STRIP_PATTERN.matcher(text).replaceAll("");
    }
}
