package org.kkdev.v2raygo.ProxyFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ProxyConfigureFile {

    /**
     * An array of sample (ProxyFile) items.
     */
    public static final List<ProxyConfItem> ITEMS = new ArrayList<ProxyConfItem>();

    /**
     * A map of sample (ProxyFile) items, by ID.
     */
    public static final Map<String, ProxyConfItem> ITEM_MAP = new HashMap<String, ProxyConfItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(ProxyConfItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static ProxyConfItem createDummyItem(int position) {
        return new ProxyConfItem(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A ProxyFile item representing a piece of content.
     */
    public static class ProxyConfItem {
        public final String id;
        public final String content;
        public final String details;

        public String Filename;


        public ProxyConfItem(String id, String content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
