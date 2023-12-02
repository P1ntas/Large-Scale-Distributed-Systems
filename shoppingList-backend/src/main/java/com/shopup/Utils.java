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
}
