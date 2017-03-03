package tk.ardentbot.Utils;

import tk.ardentbot.Utils.Tuples.Pair;

import java.util.*;

public class StringUtils {

    public static Comparator<Pair<String, Double>> SIMILARITY = (o1, o2) -> {
        if (o1.getV() < o2.getV()) return 1;
        else if (Objects.equals(o1.getV(), o2.getV())) return 0;
        else return -1;
    };

    public static void replaceAll(final StringBuilder builder, final String from, final String to) {
        int index;
        while ((index = builder.indexOf(from)) != -1) {
            builder.replace(index, index + from.length(), to);
        }
    }

    public static String replaceFirst(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
    }

    /**
     * Thanks StackOverflow
     */
    public static String replaceLast(final String text, final String regex, final String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public static String[] split(String string, final int lenth, final String split) {
        Objects.requireNonNull(string);
        if (string.length() == 0) {
            return new String[]{};
        }
        else if (string.length() == 1) {
            return new String[]{string};
        }
        else if (string.length() <= lenth) {
            return new String[]{string};
        }
        final List<String> strings = new ArrayList<>();

        while (string.length() > lenth) {
            final String current = string.substring(0, lenth + split.length());

            final int index = current.lastIndexOf(split);

            if (index == -1) {
                throw new UnsupportedOperationException("One or more substrings were too long!");
            }

            final String substring = current.substring(0, index);

            strings.add(substring);
            string = StringUtils.replaceFirst(string, substring + split, "");

        }

        return strings.toArray(new String[strings.size()]);
    }

    public static String toPrettyString(final Iterable<?> collection) {
        String string = "";

        for (final Object object : collection) {
            string += Objects.toString(object) + ", ";
        }
        return StringUtils.replaceLast(string, ", ", "");
    }

    public static ArrayList<String> mostSimilar(String s, ArrayList<String> tags) {
        ArrayList<String> mostSimilar = new ArrayList<>();
        ArrayList<Pair<String, Double>> pairs = new ArrayList<>();
        for (String tag : tags) {
            pairs.add(new Pair<String, Double>(tag, calculateSimilarity(s, tag)));
        }
        Collections.sort(pairs, SIMILARITY);
        for (int i = 0; i < 4; i++) {
            if (i < tags.size()) {
                mostSimilar.add(pairs.get(i).getK());
            }
        }
        return mostSimilar;
    }

    public static double calculateSimilarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
        /* // If you have StringUtils, you can use it to calculate the edit distance:
        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                                                             (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://r...content-available-to-author-only...e.org/wiki/Levenshtein_distance#Java
    public static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    /**
     * Return the number of millisecond found in commandTime.
     * @param commandTime Formatted String (ex 2d - 3w - 5h)
     * @return Number of millisecond
     */
    public static long commandeTime(String commandTime){
        int time = Integer.parseInt(commandTime.replace("w", "").replace("d", "").replace("h", "").replace("m", ""));
        long now = 0;
        if (commandTime.endsWith("m")) {
            now = time * 1000 * 60;
        }
        else if (commandTime.endsWith("h")) {
            now = time * 1000 * 60 * 60;
        }
        else if (commandTime.endsWith("d")) {
            now = time * 1000 * 60 * 60 * 24;
        }
        else if (commandTime.endsWith("w")) {
            now = time * 1000 * 60 * 60 * 24 * 7;
        }
        return now;
    }

}