package org.kkdev.v2raygo.ProxyFile;

import java.util.ArrayList;
import java.util.List;

import libv2ray.V2RayContext;
/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class ProxyConfigureFile {

    public static List<ProxyConfItem> CreateFromStringArray(libv2ray.StringArrayList input,V2RayContext ctx){
        List<ProxyConfItem> drt = new ArrayList<>() ;
        for (int i = 0; i< input.getLen(); i++) {
            drt.add(new ProxyConfItem(input.getElementById(i),ctx));
        }
    return drt;
    }

    /**
     * A ProxyFile item representing a piece of content.
     */
    public static class ProxyConfItem {
        public final String path;

        public final V2RayContext ctx;

        //public String Filename;


        public ProxyConfItem(String path,V2RayContext ctx) {
            this.path = path;
            this.ctx = ctx;
        }

        @Override
        public String toString() {
            return path;
        }
    }
}
