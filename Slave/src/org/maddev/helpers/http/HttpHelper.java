package org.maddev.helpers.http;

import com.google.gson.JsonObject;
import org.maddev.Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpHelper {

    public static String getIpAddress() {
        try {
            URLConnection connection = new URL("https://api.ipify.org?format=json").openConnection();
            InputStream input = connection.getInputStream();
            JsonObject element = Main.gson.fromJson(new InputStreamReader(input), JsonObject.class);
            input.close();
            return element.get("ip").getAsString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
