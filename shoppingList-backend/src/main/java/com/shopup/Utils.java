package com.shopup;

import java.util.TreeMap;

class Utils {
    public TreeMap<Integer, String> stringToTreeMap(String mapString) {
        TreeMap<Integer, String> map = new TreeMap<>();

        // Removing the curly braces
        mapString = mapString.substring(1, mapString.length() - 1);

        // Splitting the string into entries
        String[] entries = mapString.split(", ");

        for (String entry : entries) {
            // Splitting each entry into key and value
            String[] keyValue = entry.split("=");
            Integer key = Integer.parseInt(keyValue[0]);
            String value = keyValue[1];

            // Adding to the TreeMap
            map.put(key, value);
        }

        return map;
    }

    public static String mergeNames(String s1, String s2) {
        if (s1.equals(s2)) {
            return s1;
        }

        // Find the longest significant overlap
        String overlap = findLongestOverlap(s1, s2);
        if (overlap.length() >= 4) {
            return s1 + s2.substring(overlap.length());
        }

        return s1 + s2;
    }

    private static String findLongestOverlap(String s1, String s2) {
        for (int i = Math.min(s1.length(), s2.length()); i >= 4; i--) {
            if (s1.endsWith(s2.substring(0, i))) {
                return s2.substring(0, i);
            }
        }
        return "";
    }
}
