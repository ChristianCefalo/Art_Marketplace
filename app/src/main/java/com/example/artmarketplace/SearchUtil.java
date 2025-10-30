package com.example.artmarketplace;

import java.util.*;

class SearchUtil {
    static List<String> buildTokens(String title, List<String> tags) {
        Set<String> out = new LinkedHashSet<>();
        if (title != null) {
            for (String w : title.toLowerCase(Locale.US).split("[^a-z0-9]+")) {
                if (w.length() >= 2) out.add(w);
            }
        }
        if (tags != null) {
            for (String t : tags) {
                if (t == null) continue;
                String z = t.toLowerCase(Locale.US).trim();
                if (z.length() >= 2) out.add(z);
            }
        }
        return new ArrayList<>(out);
    }
}
