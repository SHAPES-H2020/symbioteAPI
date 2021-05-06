package com.icom.symbiote.controller;

import com.icom.symbiote.login.SymbioteAdmin;
import com.icom.symbiote.login.SymbioteLogin;
import com.icom.symbiote.login.SymbioteResources;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResController {
    String exampleHomePlatformIdentifier ="SymbIoTe_Core_AAM";

    @PostMapping("/symbiote/getL2Res")
    public String postGetL2Res(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteResources.getL2Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("fedid"),
                jobj.getString("resname"),
                jobj.getString("platid"));
        return ans;
    }
    @PostMapping("/symbiote/getL1Res")
    public String postGetL1Res(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteLogin.getL1Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("resid"),
                jobj.getString("platid"));
        return ans;
    }
    @PostMapping("/symbiote/accessL2Res")
    public String postAccessSymbioteL2Res(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteResources.accessL2Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("fedid"),
                jobj.getString("resid"));
        return ans;
    }
    @PostMapping("/symbiote/accessL1Res")
    public String postAccessSymbioteL1Res(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteResources.accessL1Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("resid"));
        return ans;
    }
    @PostMapping("/symbiote/registerL1Res")
    public String postRegisterSymbioteL1Res(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteAdmin.registerL1ResRes(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("resname"),
                jobj.getInt("restype"),
                jobj.getString("platid")
                );
        return ans;
    }
    @PostMapping("/symbiote/shareL1Res")
    public String postShareL1Res(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteResources.shareL1Res(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("fedid"),
                jobj.getString("resname"),
                jobj.getString("platid"));
        return ans;
    }
    @PostMapping("/symbiote/registerUser")
    public String postRegisterSymbioteUser(@RequestBody String postInfo) {
        JSONObject jobj = new JSONObject(postInfo);
        String ans = SymbioteAdmin.registerUser(
                jobj.getString("token"),
                exampleHomePlatformIdentifier,
                jobj.getString("username"),
                jobj.getString("password"),
                jobj.getString("email"),
                jobj.getString("platid")
        );
        return ans;
    }
}
