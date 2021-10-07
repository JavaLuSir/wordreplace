package org.javalu.docgen.util;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyUtil {

    private static String TMPDIR = System.getProperty("user.dir") + "/template/data.properties";


    public static boolean savemap(Map<String, String> mapdata) {
        File f = new File(TMPDIR);
        InputStream fins = null;
        OutputStream fons = null;

        Properties p = new Properties();
        try {
            fins = new BufferedInputStream(new FileInputStream(f));
            p.load(fins);
            mapdata.forEach((k,v)->{ p.setProperty(k,v);});

            fons = new FileOutputStream(TMPDIR);
            p.store(fons,null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fons.flush();
                fons.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean clear() {
        File f = new File(TMPDIR);
        OutputStream fons = null;

        Properties p = new Properties();
        try {
            fons = new FileOutputStream(TMPDIR);
            p.store(fons,null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                fons.flush();
                fons.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static Map<String, String> readmap() {
        Map<String, String> resultMap = new HashMap<>();
        File f = new File(TMPDIR);
        InputStream fins = null;

        Properties p = new Properties();
        try {
            fins = new BufferedInputStream(new FileInputStream(f));
            p.load(fins);
            Enumeration<?> e = p.propertyNames();
            for (String key :p.stringPropertyNames()) {
                resultMap.put(key, p.getProperty(key));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }


}
