package com.icom.symbiote.controller;

import com.icom.symbiote.login.SymbioteLogin;
import com.icom.symbiote.login.SymbioteResources;
import com.icom.symbiote.model.Observation;
import com.icom.symbiote.utils.CleanThread;
import com.icom.symbiote.utils.LocalData;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TestController {
    String coreAddress = "https://intracom-core.symbiote-h2020.eu";//"https://symbiote-open.man.poznan.pl";
    String keystorePath = "testKeystore";
    String keystorePassword = "testKeystore";
    String exampleHomePlatformIdentifier ="SymbIoTe_Core_AAM";
    //====================================================================
    @PostMapping("/symbiote/login")
    public String postloginToSymbiote(@RequestBody String postInfo){
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteLogin.Login(coreAddress,
                jobj.getString("usr"),
                jobj.getString("psw"),
                "cl1",
                exampleHomePlatformIdentifier
        );
        CleanThread clnTrd = new CleanThread();
        Thread trdLocal = new Thread(clnTrd);
        trdLocal.start();;

        return ans;
    }
    @PostMapping("/symbiote/getListOfL1")
    public String postListOfResL1(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteLogin.getListL1Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("platid"));
        return ans;
    }
    @PostMapping("/symbiote/getListOfL2")
    public String postListOfResL2(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteResources.getListL2Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("fedid"),
                jobj.getString("platid"));
        return ans;
    }
/*    @PostMapping("/symbiote/getListOfL2new")
    public String postListOfResL2new(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteResources.getListL2Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("fedid"));
        return ans;
    }*/
}
