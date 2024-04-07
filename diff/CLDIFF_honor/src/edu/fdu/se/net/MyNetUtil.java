package edu.fdu.se.net;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by huangkaifeng on 2018/10/21.
 *
 */
public class MyNetUtil {

    public static Map<String, String> parsePostedKeys(InputStream is, String contentType) throws IOException {
        byte[] cache = new byte[100];
        int res;
        StringBuilder postString = new StringBuilder();
        while ((res = is.read(cache)) != -1) {
            String a = new String(cache).substring(0, res);
            postString.append(a);
        }
        Global.logger.info(postString.toString());
        Map<String, String> mMap = new HashMap<>();
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            String[] entries = postString.toString().split("&");

            for (String entry : entries) {
                String[] kvs = entry.split("=");
                mMap.put(kvs[0], kvs[1]);
            }
        } else if ("application/json".equals(contentType)) {
            JSONObject jo = new JSONObject(postString.toString());
            Iterator iter = jo.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                String value = jo.getString(key);
                mMap.put(key, value);
            }
            return mMap;
        }
        return mMap;
    }


    public static String getPostString(InputStream is) throws IOException {
        byte[] cache = new byte[100];
        int res;
        StringBuilder postString = new StringBuilder();
        while ((res = is.read(cache)) != -1) {
            String a = new String(cache).substring(0, res);
            postString.append(a);
        }
        return postString.toString();
    }

    public static void writeResponseInBytes(OutputStream os, byte[] bytes) throws IOException{

        try (BufferedOutputStream out = new BufferedOutputStream(os)) {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
                byte[] buffer = new byte[1000];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
                out.close();
            }
        }
    }
}