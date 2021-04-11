package com.icom.symbiote.utils;

import org.json.JSONObject;

public class ResultUtils {
    public static String getResult(String code, Object res){
        try {
            JSONObject jo = new JSONObject();
            jo.put("code",code);
            jo.put("result",res);
            return jo.toString();
        }catch(Exception e){
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
