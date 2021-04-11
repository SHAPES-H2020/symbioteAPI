package com.icom.symbiote.login;


import com.icom.symbiote.utils.LocalData;
import com.icom.symbiote.utils.ResultUtils;
import eu.h2020.symbiote.client.AbstractSymbIoTeClientFactory;
import eu.h2020.symbiote.client.interfaces.CRAMClient;
import eu.h2020.symbiote.client.interfaces.PRClient;
import eu.h2020.symbiote.client.interfaces.RHClient;
import eu.h2020.symbiote.client.interfaces.SearchClient;
import eu.h2020.symbiote.cloud.model.internal.*;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.cram.ResourceUrlsResponse;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.security.ClientSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.Token;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.AAM;
import eu.h2020.symbiote.security.handler.ISecurityHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

import static eu.h2020.symbiote.client.AbstractSymbIoTeClientFactory.*;

public class SymbioteLogin {
    public static String getListL2Res_deprecated(
            String key,
            String PlatformIdentifier,
            String federationId){
        String ret = "";
        JSONArray jar = new JSONArray();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);
            String PlatformId = "";
            try {
                PlatformId = jobj.getString("plat");
            } catch (Exception e) {
                e.printStackTrace();
            }
            AbstractSymbIoTeClientFactory factory =
                    (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
            //==============================================================
            RHClient rhClient = factory.getRHClient(PlatformId);
            List<CloudResource> lst = rhClient.getResources();
            int sz = lst.size();
            ret = "NUMBER OF RESOURCES " + sz + "<BR>";
            Iterator it = lst.iterator();
            while (it.hasNext()) {
                CloudResource rs = (CloudResource) it.next();
                String fedstr = "";
                //============================================
                if (rs.getFederationInfo()!=null) {
                    Map mp = rs.getFederationInfo().getSharingInformation();
                    Iterator fedit = mp.keySet().iterator();
                    while (fedit.hasNext()){
                        String keyfed = (String) fedit.next();
                        fedstr+= keyfed+",";
                    }
                }
                //============================================
                String resourceId = rs.getResource().getId();
                String name = rs.getResource().getName();

                String resourceUrl = rs.getResource().getInterworkingServiceURL();
                //ret+= "name=" + name
                //        + ", id=" + resourceId
                //        + ", url=" + resourceUrl
                //        + "<BR>";
                JSONObject jo = new JSONObject();
                jo.put("name",name);
                jo.put("id",resourceId);
                jo.put("url",resourceUrl);
                jo.put("federations",fedstr);
                jar.put(jo);
            }
            //==============================================================
            /*PRClient searchClient = factory.
                    //getPRClient("SymbIoTe_Core_AAM");
                            getPRClient("icom-platform");

            Set<String> platformIds = new HashSet<>(
                    //Arrays.asList("icom-platform", "SymbIoTe_Core_AAM"));
                    Collections.
                            //singletonList("icom-platform"));
                            //singletonList("icom-platform"));
                                    singletonList("SymbIoTe_Core_AAM"));

            try {
                ResponseEntity<FederationSearchResult> query = searchL2Resources(searchClient, platformIds,
                        new PlatformRegistryQuery.Builder().build()
                );
                List<FederatedResource> lst = query.getBody().getResources();
                int i = lst.size();
                return "resource found";
            }catch(Exception fe){
                System.out.println("resource problem");
                return "resource not found";
            }*/
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
        }
        return ResultUtils.getResult("0",jar);
    }

    public static String getL2Res_deprecated(
            String key,
            String PlatformIdentifier,
            String federationId,
            String resL2Id
    ){
        String ret = "";
        JSONArray jar = new JSONArray();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);
            String PlatformId = "";
            try {
                PlatformId = jobj.getString("plat");
            } catch (Exception e) {
                e.printStackTrace();
            }
            AbstractSymbIoTeClientFactory factory =
                    (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
            //==============================================================
            RHClient rhClient = factory.getRHClient(PlatformId);
            List<CloudResource> lst = rhClient.getResources();
            int sz = lst.size();
            ret = "NUMBER OF RESOURCES " + sz + "<BR>";
            Iterator it = lst.iterator();
            while (it.hasNext()) {

                CloudResource rs = (CloudResource) it.next();
                Object fedobj = null;
                //============================================
                if (rs.getFederationInfo()!=null) {
                    Map mp = rs.getFederationInfo().getSharingInformation();
                    fedobj = mp.get(federationId);
                }
                //============================================
                String resourceId = rs.getResource().getId();
                String name = rs.getResource().getName();

                String resourceUrl = rs.getResource().getInterworkingServiceURL();
                if ((fedobj!=null)&&(resourceId!=null)&&(resourceId.equals(resL2Id))) {
                    JSONObject jo = new JSONObject();
                    jo.put("name", name);
                    jo.put("id", resourceId);
                    jo.put("url", resourceUrl);
                    jar.put(jo);
                }
            }
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
        }
        return jar.toString();
    }

    public static String getListL1Res(
            String key,String PlatformIdentifier,String platid){
        String ret = "";
        JSONArray jar = new JSONArray();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);

            AbstractSymbIoTeClientFactory factory =
                    (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
            //==========================================
            SearchClient searchClient = factory.getSearchClient();
            CRAMClient cramClient     = factory.getCramClient();

            Set<String> platformIds = new HashSet<>(Collections.singletonList(
                    PlatformIdentifier));
            // But before this end point asks for a home token
            CoreQueryRequest coreQueryRequest = null;
            if (platid.isEmpty()){
                coreQueryRequest = new CoreQueryRequest.Builder()
                        .build();
            }else {
                coreQueryRequest = new CoreQueryRequest.Builder()
                        .platformId(platid)
                        .build();
            }
            QueryResponse queryResponse = null;
            try {
                queryResponse = searchClient.search(
                        coreQueryRequest, true, platformIds);
            }catch(Exception ex){
                System.out.println("Exception searchClient.search");
            }
            int numberOfResourcesFound = queryResponse.getResources().size();
            ret = "NUMBER OF RESOURCES " + numberOfResourcesFound + "<BR>";
            for(int i=0;i<numberOfResourcesFound;i++){
                //System.out.println("Number of resources found: " + numberOfResourcesFound);
                String resourceId = queryResponse.getResources().get(i).getId();
                String pId = queryResponse.getResources().get(i).getPlatformId();
                String owner = queryResponse.getResources().get(i).getOwner();
                String name = queryResponse.getResources().get(i).getName();
                String description = queryResponse.getResources().get(i).getDescription();
                String locationName = queryResponse.getResources().get(i).getLocationName();
                ResourceUrlsResponse resourceUrlsResponse =
                        cramClient.getResourceUrl(resourceId, true, platformIds);
                String resourceUrl = resourceUrlsResponse.getBody().get(resourceId);
                //ret+= "name=" + name
                //        + ", id=" + resourceId
                //        + ", url=" + resourceUrl
                //        + "<BR>";
                JSONObject jo = new JSONObject();
                jo.put("name",name);
                jo.put("id",resourceId);
                jo.put("url",resourceUrl);
                jar.put(jo);
            }
            //==========================================
        }catch(Exception e){
            e.printStackTrace();
            return "ticket expired or user not logged in";
        }
        return ResultUtils.getResult("0",jar);
    }
    public static String getL1Res(
            String key,String PlatformIdentifier,String resid,String platid){
        String ret = "";
        JSONArray jar = new JSONArray();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);

            /*try {
                PlatformId = jobj.getString("plat");
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            AbstractSymbIoTeClientFactory factory =
                    (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
            //==========================================
            SearchClient searchClient = factory.getSearchClient();
            CRAMClient cramClient     = factory.getCramClient();

            Set<String> platformIds = new HashSet<>(Collections.singletonList(
                    PlatformIdentifier));
            // But before this end point asks for a home token
            CoreQueryRequest coreQueryRequest = null;
            if (platid.isEmpty()){
                coreQueryRequest = new CoreQueryRequest.Builder()
                        .build();
            }else {
                coreQueryRequest = new CoreQueryRequest.Builder()
                        .platformId(platid)
                        .build();
            }
            QueryResponse queryResponse = null;
            try {
                queryResponse = searchClient.search(
                        coreQueryRequest, true, platformIds);
            }catch(Exception ex){
                System.out.println("Exception searchClient.search");
            }
            int numberOfResourcesFound = queryResponse.getResources().size();
            ret = "NUMBER OF RESOURCES " + numberOfResourcesFound + "<BR>";
            for(int i=0;i<numberOfResourcesFound;i++){
                //System.out.println("Number of resources found: " + numberOfResourcesFound);
                String resourceId = queryResponse.getResources().get(i).getId();
                if ((resourceId==null)||(!resourceId.equals(resid))){
                    continue;
                }
                String pId = queryResponse.getResources().get(i).getPlatformId();
                String owner = queryResponse.getResources().get(i).getOwner();
                String name = queryResponse.getResources().get(i).getName();
                String description = queryResponse.getResources().get(i).getDescription();
                String locationName = queryResponse.getResources().get(i).getLocationName();
                ResourceUrlsResponse resourceUrlsResponse =
                        cramClient.getResourceUrl(resourceId, true, platformIds);
                String resourceUrl = resourceUrlsResponse.getBody().get(resourceId);
                //ret+= "name=" + name
                //        + ", id=" + resourceId
                //        + ", url=" + resourceUrl
                //        + "<BR>";
                JSONObject jo = new JSONObject();
                jo.put("name",name);
                jo.put("id",resourceId);
                jo.put("url",resourceUrl);
                jar.put(jo);
            }
            //==========================================
        }catch(Exception e){
            e.printStackTrace();
            return ResultUtils.getResult("1","ticket expired or user not logged in");
        }
        return ResultUtils.getResult("0",jar);
    }
    public static String Login(
            String coreAddress,
            String usr,
            String psw,
            String cln,
            String PlatformIdentifier
    ){
        Object ret = "";

        Type type = Type.FEIGN;

        AbstractSymbIoTeClientFactory.Config config = new AbstractSymbIoTeClientFactory.Config(
                coreAddress, "testKeystore", "testKeystore", type);
        // Get the factory
        AbstractSymbIoTeClientFactory factory;
        try {
            factory = getFactory(config);
            Set<AbstractSymbIoTeClientFactory.HomePlatformCredentials> platformCredentials = new HashSet<>();

            String username = usr;
            String password = psw;
            String clientId = cln;
            AbstractSymbIoTeClientFactory.HomePlatformCredentials exampleHomePlatformCredentials = new AbstractSymbIoTeClientFactory.HomePlatformCredentials(
                    PlatformIdentifier,
                    username,
                    password,
                    clientId);
            platformCredentials.add(exampleHomePlatformCredentials);

            factory.initializeInHomePlatforms(platformCredentials);
            ret = LocalData.addEntry(usr,psw,factory);
        } catch (SecurityHandlerException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ResultUtils.getResult("1","user not registered");
        }finally{
            //-------------------------------------------------------------------
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
            //-------------------------------------------------------------------

        }
        return ResultUtils.getResult("0",ret);
    }
}
