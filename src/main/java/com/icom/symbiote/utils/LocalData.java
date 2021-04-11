package com.icom.symbiote.utils;

import org.apache.jena.tdb.store.Hash;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.*;


public class LocalData {

    private static int aamSizeMax = 10;
    private static int aamSizeCur = 0;
    private static HashMap<String,String> sessionInfo = new HashMap<String,String>();
    private static HashMap<String, Object> facInfo = new HashMap<String,Object>();
    private static HashMap<String,String> timeInfo = new HashMap<String, String>();

    private static int aamSize = 0;

    private static String getHashNum(String str){
        String stringHash = str;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes());
            stringHash = new String(messageDigest.digest());
        }catch (Exception e){
            e.printStackTrace();
        }
        return stringHash;
    }
    //                  0123456789
    private static String alphaChar = "870wabcdlkpert12mq";
    private static String alphaNum  = "0123456789";
    private static String getAlphaStr(String num){
        StringBuilder ret = new StringBuilder();
        for (int i = 0;i< num.length(); i++){
            char chr = num.charAt(i);
            int chrInt = chr - '0';
            ret.append(alphaChar.charAt(chrInt));
        }
        return ret.toString();
    }
    private static String getAlphaNum(String str){
        StringBuilder ret = new StringBuilder();
        for (int i = 0;i< str.length(); i++){
            int p = alphaChar.indexOf(str.charAt(i));
            char chr = alphaNum.charAt(p);
            ret.append(chr);
        }
        return ret.toString();
    }
    public static synchronized void cleanInfo(){
        System.out.println("clean start");
        Date dt = new Date();
        long tmnow = dt.getTime();
        Set kset = timeInfo.keySet();
        Iterator it = kset.iterator();
        while(it.hasNext()){
            String key = (String)it.next();
            String tmStr = timeInfo.get(key);
            try{
                long ln = Long.parseLong(tmStr);
                if (tmnow - ln > 20*60*1000){
                    sessionInfo.remove(key);
                    facInfo.remove(key);
                    timeInfo.remove(key);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println("clean end");
    }
    public static JSONObject addEntry(String usr, String psw, Object obj)
    {
        String ret = "";
        {
            JSONObject jobj = new JSONObject();
            Date dt = new Date();
            long tm = dt.getTime();
            String tmStr = Long.toString(tm);
            ret = tmStr;
            //ret = getHashNum(tmStr);

            try {
                jobj.put("usr", usr);
                jobj.put("psw", psw);
                //jobj.put("key", ret);
                jobj.put("tm", tmStr);
                //jobj.put("plat",pl);
            }catch(Exception e){
                e.printStackTrace();
            }
            sessionInfo.put(ret, jobj.toString());
            facInfo.put(ret,obj);
            timeInfo.put(ret,tmStr);
        }
        ret = getAlphaStr(ret);
        JSONObject jobj = new JSONObject();
        jobj.put("token",ret);
        return jobj;
    }

    public static String getInfo(String key){
        key = getAlphaNum(key);
        String ret = (String)sessionInfo.get(key);
        return ret;
    }
    public static Object getFac(String key){
        key = getAlphaNum(key);
        Object ret = facInfo.get(key);
        return ret;
    }
}
