package com.icom.symbiote.login;

import com.icom.symbiote.utils.LocalData;
import com.icom.symbiote.utils.ResultUtils;
import eu.h2020.symbiote.client.AbstractSymbIoTeClientFactory;
import eu.h2020.symbiote.client.interfaces.*;
import eu.h2020.symbiote.cloud.model.internal.*;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.core.internal.RDFInfo;
import eu.h2020.symbiote.core.internal.cram.ResourceUrlsResponse;
import eu.h2020.symbiote.model.cim.Observation;
import eu.h2020.symbiote.model.cim.Resource;
import eu.h2020.symbiote.security.accesspolicies.common.AccessPolicyType;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.*;

import static eu.h2020.symbiote.client.AbstractSymbIoTeClientFactory.getFactory;

public class SymbioteResources {
    public static String accessL2Res(
            String key,
            String PlatformIdentifier,
            String federationId,
            String resL2Id){

        JSONArray jar = new JSONArray();
        Observation ob = null;
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);
            String PlatformId = "";
            String usr = "";
            String psw = "";
            try {
                PlatformId = jobj.getString("plat");
                usr = jobj.getString("usr");
                psw = jobj.getString("psw");
            } catch (Exception e) {
                e.printStackTrace();
            }
            AbstractSymbIoTeClientFactory factory = null;
            String coreAddress  = "https://intracom-core.symbiote-h2020.eu";
            String keystorePath = "testKeystore";
            String keystorePassword = "testKeystore";
            String exampleHomePlatformIdentifier = "SymbIoTe_Core_AAM";
            AbstractSymbIoTeClientFactory.Type type = AbstractSymbIoTeClientFactory.Type.FEIGN;
            try{
                AbstractSymbIoTeClientFactory.Config config =
                        new AbstractSymbIoTeClientFactory.Config(
                                coreAddress, keystorePath, keystorePassword, type);
                factory = getNewFactory(
                        config,
                        usr,
                        psw,exampleHomePlatformIdentifier
                );
                Set<String> platformIds = new HashSet<>(Collections.singletonList(
                        PlatformIdentifier));
                PRClient prClient = factory.getPRClient(PlatformId);
                CRAMClient cramClient     = factory.getCramClient();
                RAPClient rapClient = factory.getRapClient();
                PlatformRegistryQuery query =
                        new PlatformRegistryQuery.Builder()
                                .federationIds(Collections.singletonList(federationId))
                                .ids(Collections.singletonList(resL2Id))
                                //.names(Collections.singletonList(resL2Id))
                                .build();
                Set<String> homePlatformIds =
                        new HashSet<>(Collections.singletonList(PlatformId));
                FederationSearchResult result =
                        prClient.search(query, false, homePlatformIds);
                int num = 0;
                if (result.getResources().size()>0) {
                    FederatedResource fr = result.getResources().get(num);
                    CloudResource clr = fr.getCloudResource();
                    Resource rs = clr.getResource();
                    ResourceUrlsResponse resourceUrlsResponse =
                            cramClient.getResourceUrl(rs.getId(), true, platformIds);
                    String resourceUrl = resourceUrlsResponse.getBody().get(rs.getId());
                    ob = rapClient.getLatestObservation(resourceUrl, false, platformIds);
                }
                else
                    return ResultUtils.getResult("0","resource not found");

            }catch(Exception fe){
                System.out.println("resource problem");
                return ResultUtils.getResult("0","resource not found");
            }
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
            return ResultUtils.getResult("0","Exception searchClient.search");
        }
        finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("0",ob.toString());
    }

    public static String accessL2Res_deprecated(
            String key,
            String PlatformIdentifier,
            String federationId,
            String resL2Id){
        String exampleHomePlatformIdentifier = "SymbIoTe_Core_AAM";
        String ret = "";
        JSONArray jar = new JSONArray();
        Observation ob = null;
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
            RAPClient rapClient = factory.getRapClient();
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


                if ((fedobj!=null)&&(resourceId!=null)&&(resourceId.equals(resL2Id))) {
                    String type = "";
                    String resourceUrl = rs.getResource().getInterworkingServiceURL();
                    List deslist = rs.getResource().getDescription();
                    Iterator desit = deslist.iterator();
                    while (desit.hasNext()){
                        String ds = (String)it.next();
                        if (ds.startsWith("type.")){
                            String dsar[] = ds.split(".");
                            type = dsar[1];
                        }
                    }
                    resourceUrl = resourceUrl + "/rap/"+type+"('" + resourceId +
                            "')/Observations?$top=1";
                    Set<String> platformIds = new HashSet<>(Collections.singletonList(exampleHomePlatformIdentifier));
                    ob = rapClient.getLatestObservation(resourceUrl, false,platformIds);
                }
            }
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
        }
        if (ob!=null)
            return ob.toString();
        else
            return "an error occurs";
    }

    static int shareRs(
            RHClient rhClient
            ,String resname
            ,String fedname
    ){
        Map<String, Map<String, Boolean>> toShare = new HashMap<>();
        Map<String, Boolean> resourceMap = new HashMap<>();
        String[] resarr = resname.split(",");
        String[] fedarr = fedname.split(",");
        for (int i=0;i<resarr.length;i++){
            resourceMap.put(resarr[i], true);
        }
        for (int i=0;i<fedarr.length;i++){
            toShare.put(fedarr[i], resourceMap);
        }
        Map<String, List<CloudResource>> result = rhClient.shareL2Resources(toShare);
        int i = result.size();
        return i;
    }
    public static String shareL1Res(
            String key,
            String PlatformIdentifier,
            String fedName,
            String resName,
            String platid) {
        Object ret = "";
        JSONArray jar = new JSONArray();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);
            //String PlatformId = "";
            AbstractSymbIoTeClientFactory factory =
                    (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
            //========================================================
            RHClient rhClient = factory.getRHClient(platid);
            int i = shareRs(rhClient, resName, fedName);
            JSONObject jo = new JSONObject();
            jo.put("res", i);
            ret = jo;
        } catch (Exception e1) {
            e1.printStackTrace();
            return ResultUtils.getResult("1",e1.getMessage());
        }
        return ResultUtils.getResult("0",ret);
    }
    public static String accessL1Res(String key,String PlatformIdentifier,String resid){
        String ret = "";
        //JSONArray jar = new JSONArray();
        Observation ob = null;
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
            //==========================================
            SearchClient searchClient = factory.getSearchClient();
            CRAMClient cramClient     = factory.getCramClient();
            RAPClient rapClient = factory.getRapClient();

            Set<String> platformIds = new HashSet<>(Collections.singletonList(
                    PlatformIdentifier));
            // But before this end point asks for a home token
            CoreQueryRequest coreQueryRequest = new CoreQueryRequest.Builder()
                    .platformId(PlatformIdentifier)
                    .id(resid)
                    .build();
            QueryResponse queryResponse = null;
            try {
                queryResponse = searchClient.search(
                        coreQueryRequest, true, platformIds);
            }catch(Exception ex){
                System.out.println("Exception searchClient.search");
            }
            int numberOfResourcesFound = queryResponse.getResources().size();
            //ret = "NUMBER OF RESOURCES " + numberOfResourcesFound + "<BR>";
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
                ob = rapClient.getLatestObservation(resourceUrl, false,platformIds);

                //ret+= "name=" + name
                //        + ", id=" + resourceId
                //        + ", url=" + resourceUrl
                //        + "<BR>";
                //JSONObject jo = new JSONObject();
                //jo.put("name",name);
                //jo.put("id",resourceId);
                //jo.put("url",resourceUrl);
                //jar.put(jo);
            }
            //==========================================
        }catch(Exception e){
            e.printStackTrace();
            return ResultUtils.getResult("1","ticket expired or user not logged in");
        }
        if (ob!=null)
            return ResultUtils.getResult("0",ob.toString());
        else
            return ResultUtils.getResult("2","an error occurs");
    }
    public static String getListL2Res(
            String key,
            String PlatformIdentifier,
            String federationId,
            String platid){

        JSONArray jar = new JSONArray();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);
            //String PlatformId = "";
            String usr = "";
            String psw = "";
            try {
                //PlatformId = jobj.getString("plat");
                usr = jobj.getString("usr");
                psw = jobj.getString("psw");
            } catch (Exception e) {
                e.printStackTrace();
            }
            AbstractSymbIoTeClientFactory factory = null;
            String coreAddress  = "https://intracom-core.symbiote-h2020.eu";
            String keystorePath = "testKeystore";
            String keystorePassword = "testKeystore";
            String exampleHomePlatformIdentifier = "SymbIoTe_Core_AAM";
            AbstractSymbIoTeClientFactory.Type type = AbstractSymbIoTeClientFactory.Type.FEIGN;
            try{
                AbstractSymbIoTeClientFactory.Config config =
                        new AbstractSymbIoTeClientFactory.Config(
                                coreAddress, keystorePath, keystorePassword, type);
                //factory =
                  //      (AbstractSymbIoTeClientFactory) LocalData.getFac(key);

                factory = getNewFactory(
                        config,
                        usr,
                        psw,exampleHomePlatformIdentifier
                );
                PRClient prClient = factory.getPRClient(platid);
                PlatformRegistryQuery query =
                        new PlatformRegistryQuery.Builder()
                                .federationIds(Collections.singletonList(federationId))
                                .build();
                Set<String> homePlatformIds =
                        new HashSet<>(Collections.singletonList(platid));
                FederationSearchResult result =
                        prClient.search(query, false, homePlatformIds);
                for (int num=0;num<result.getResources().size();num++){
                    FederatedResource fr = result.getResources().get(num);
                    CloudResource clr = fr.getCloudResource();
                    Resource rs = clr.getResource();
                    JSONObject jo = new JSONObject();
                    jo.put("id",rs.getId());
                    jo.put("name",rs.getName());
                    jo.put("url",rs.getInterworkingServiceURL());
                    jar.put(jo);
                }
            }catch(Exception fe){
                System.out.println("resource problem");
                return ResultUtils.getResult("1","resource not found");
            }
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
            return ResultUtils.getResult("2","Exception searchClient.search");
        }
        finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("0",jar);
    }
    public static String getL2Res(
            String key,
            String PlatformIdentifier,
            String federationId,
            String resL2Id,
            String platid){

        JSONArray jar = new JSONArray();
        JSONObject j = new JSONObject();
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);
            //String PlatformId = "";
            String usr = "";
            String psw = "";
            try {
                //PlatformId = jobj.getString("plat");
                usr = jobj.getString("usr");
                psw = jobj.getString("psw");
            } catch (Exception e) {
                e.printStackTrace();
            }
            AbstractSymbIoTeClientFactory factory = null;
            String coreAddress  = "https://intracom-core.symbiote-h2020.eu";
            String keystorePath = "testKeystore";
            String keystorePassword = "testKeystore";
            String exampleHomePlatformIdentifier = "SymbIoTe_Core_AAM";
            AbstractSymbIoTeClientFactory.Type type = AbstractSymbIoTeClientFactory.Type.FEIGN;
            try{
                AbstractSymbIoTeClientFactory.Config config =
                        new AbstractSymbIoTeClientFactory.Config(
                                coreAddress, keystorePath, keystorePassword, type);
                factory = getNewFactory(
                        config,
                        usr,
                        psw,exampleHomePlatformIdentifier
                );
                PRClient prClient = factory.getPRClient(platid);
                PlatformRegistryQuery query =
                        new PlatformRegistryQuery.Builder()
                                .federationIds(Collections.singletonList(federationId))
                                //.ids(Collections.singletonList(resL2Id))
                                .names(Collections.singletonList(resL2Id))
                                .build();
                Set<String> homePlatformIds =
                        new HashSet<>(Collections.singletonList(platid));
                FederationSearchResult result =
                        prClient.search(query, false, homePlatformIds);
                for (int num=0;num<result.getResources().size();num++){
                    FederatedResource fr = result.getResources().get(num);
                    CloudResource clr = fr.getCloudResource();
                    Resource rs = clr.getResource();
                    JSONObject jo = new JSONObject();
                    jo.put("id",rs.getId());
                    jo.put("name",rs.getName());
                    jo.put("url",rs.getInterworkingServiceURL());
                    jar.put(jo);
                }
            }catch(Exception fe){
                System.out.println("resource problem");
                return ResultUtils.getResult("1","resource not found");
            }
        }catch(Exception ex){
            return ResultUtils.getResult("2",
                    "Exception searchClient.search");
        }
        finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("0",jar);
    }
    public static AbstractSymbIoTeClientFactory getNewFactory(
            AbstractSymbIoTeClientFactory.Config config,
            String usr,
            String psw,
            String exampleHomePlatformIdentifier
    ){
        AbstractSymbIoTeClientFactory factory = null;
            try {
                factory = getFactory(config);
                Set<AbstractSymbIoTeClientFactory.HomePlatformCredentials> platformCredentials = new HashSet<>();

                // example credentials
                String username = usr;
                String password = psw;
                String clientId = "ktClient31";
                AbstractSymbIoTeClientFactory.HomePlatformCredentials exampleHomePlatformCredentials = new AbstractSymbIoTeClientFactory.HomePlatformCredentials(
                        exampleHomePlatformIdentifier,
                        username,
                        password,
                        clientId);
                platformCredentials.add(exampleHomePlatformCredentials);
                platformCredentials.add(new AbstractSymbIoTeClientFactory.HomePlatformCredentials("icom-platform", username, password,clientId));


                // Get Certificates for the specified platforms
                factory.initializeInHomePlatforms(platformCredentials);
                //==========================================
                factory = getFactory(config);
                //==========================================
            }catch(Exception ex){
                System.out.println("Exception");
            }
            return factory;
        }
    private static ResponseEntity<FederationSearchResult> searchL2Resources(
            PRClient prClient, Set<String>  homePlatformIds, PlatformRegistryQuery query) {

        return new ResponseEntity<>(
                prClient.search(query,
                        true,
                        homePlatformIds),
                HttpStatus.OK);
    }

}
