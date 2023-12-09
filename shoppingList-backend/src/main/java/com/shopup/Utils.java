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

    public static String mergeNames(String str1, String str2) {
        // If both strings are the same, return the first string
        if (str1.equals(str2)) {
            return str1;
        }

        // Identify the shorter and longer string
        String shorter = str1.length() < str2.length() ? str1 : str2;
        String longer = str1.length() < str2.length() ? str2 : str1;

        // Start checking for substrings
        for (int length = shorter.length(); length >= 4; length--) {
            for (int start = 0; start <= shorter.length() - length; start++) {
                String subStr = shorter.substring(start, start + length);
                if (longer.contains(subStr)) {
                    // Remove the matched substring from the shorter string
                    String remaining = shorter.replace(subStr, "");
                    // Concatenate the remaining part with the longer string
                    return longer + remaining;
                }
            }
        }

        // If the substring length is less than 4, return the concatenation of both strings
        return str1 + str2;
    }
}
