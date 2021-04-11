package com.icom.symbiote.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResourcesData {
    public static String callService(String securityRequest, String resourceURL, String payload) throws IOException {
        URL url = new URL(resourceURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("PUT");
        setSecurityHeaders(securityRequest, con);
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        con.connect();

        OutputStream outputStream = con.getOutputStream();
        outputStream.write(payload.getBytes());
        outputStream.flush();

        return readAndLogString(con.getInputStream());
    }
    public static String readAndLogString(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String temp = null;
        StringBuilder sb = new StringBuilder();
        while((temp = in.readLine()) != null){
            sb.append(temp).append(" ");
        }
        String result = sb.toString();
        System.out.println("Body: " + result);
        return result;
    }
    public static void setSecurityHeaders(String securityRequest, HttpURLConnection con) {
        con.setRequestProperty("x-auth-timestamp", Long.toString(System.currentTimeMillis()));
        con.setRequestProperty("x-auth-1", securityRequest);
        con.setRequestProperty("x-auth-size", "1");
    }
    public static String getReadCurrentValue(String securityRequest, String resourceURL, int top) throws IOException {
        URL url = new URL(resourceURL + "/Observations?$top=" + top);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        setSecurityHeaders(securityRequest, con);
        con.setRequestProperty("Accept", "application/json");

        con.connect();

        Reader reader = readAndLog(con.getInputStream());
        return reader.toString();
    }
    public static Reader readAndLog(InputStream inputStream) throws IOException {
        String result = readAndLogString(inputStream);

        return new StringReader(result);
    }
}
