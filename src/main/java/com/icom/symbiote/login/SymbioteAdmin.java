package com.icom.symbiote.login;

import com.icom.symbiote.utils.LocalData;
import com.icom.symbiote.utils.ResultUtils;
import eu.h2020.symbiote.client.AbstractSymbIoTeClientFactory;
import eu.h2020.symbiote.client.interfaces.RHClient;
import eu.h2020.symbiote.cloud.model.internal.CloudResource;
import eu.h2020.symbiote.cloud.model.internal.RdfCloudResourceList;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.core.internal.RDFInfo;
import eu.h2020.symbiote.model.cim.*;
import eu.h2020.symbiote.security.accesspolicies.common.AccessPolicyType;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleTokenAccessPolicySpecifier;
import eu.h2020.symbiote.security.commons.enums.AccountStatus;
import eu.h2020.symbiote.security.commons.enums.ManagementStatus;
import eu.h2020.symbiote.security.commons.enums.OperationType;
import eu.h2020.symbiote.security.commons.enums.UserRole;
import eu.h2020.symbiote.security.commons.exceptions.custom.AAMException;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.IAAMClient;
import eu.h2020.symbiote.security.communication.payloads.Credentials;
import eu.h2020.symbiote.security.communication.payloads.UserDetails;
import eu.h2020.symbiote.security.communication.payloads.UserManagementRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class SymbioteAdmin {
    public static String registerL1ResRes(
            String key,
            String PlatformIdentifier,
            String intId,
            int restype,
            String PlatformId){

        List<CloudResource> lst = null;
        try {

            String str = LocalData.getInfo(key);
            JSONObject jobj = new JSONObject(str);

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
                factory =
                        (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
                /*factory = SymbioteResources.getNewFactory(
                        config,
                        usr,
                        psw,exampleHomePlatformIdentifier
                );*/
                //===========================================
                RHClient rhClient = factory.getRHClient(PlatformId);
                lst = addresources(rhClient,intId,restype);
                /*
                List lst = rhClient.addL1RdfResources(getRdfList(
                        rdfStr,
                        intId,
                        plugId,
                        mapKey
                ));*/
            }catch(Exception fe){
                System.out.println("resource problem");
                return ResultUtils.getResult("2","resource not found");
            }
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
            return ResultUtils.getResult("1","Exception searchClient.search");
        }
        finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("0",Integer.toString(lst.size()));
    }

    private static RdfCloudResourceList getRdfList(
            String rdfStr,
            String intId,
            String plugId,
            String mapKey
    ) {
        RdfCloudResourceList list = new RdfCloudResourceList();
        CloudResource cloudResource = new CloudResource();
        cloudResource.setInternalId(intId);
        cloudResource.setPluginId(plugId);

        try {
            cloudResource.setAccessPolicy(new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC, null));
            cloudResource.setFilteringPolicy(new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC, null));
        } catch (InvalidArgumentsException e) {
            e.printStackTrace();
        }

        cloudResource.setResource(null);
        list.getIdMappings().put(mapKey, cloudResource);
        RDFInfo rdfInfo = new RDFInfo();
        rdfInfo.setRdf(rdfStr);

        rdfInfo.setRdfFormat(RDFFormat.JSONLD);
        list.setRdfInfo(rdfInfo);

        return list;
    }
    static void getFields(CloudResource cloudResource, int type ) {
        // long randomizer = System.currentTimeMillis();

        cloudResource.setPluginId("RapPluginExample");
        Resource resource = cloudResource.getResource();

        if( type==0 ) {
            //if( randomizer%5==4 ) {
            //log.debug("Adding temperature, humidity to cloudResource");
            StationarySensor sensor = new StationarySensor();
            sensor.setName(resource.getName());
            //sensor.setId(resource.getName());
            sensor.setInterworkingServiceURL(resource.getInterworkingServiceURL());
            sensor.setDescription(Collections.singletonList("temperature"));
            FeatureOfInterest featureOfInterest = new FeatureOfInterest();
            featureOfInterest.setName("outside air");
            featureOfInterest.setDescription(Collections.singletonList("outside temperature and humidity"));
            featureOfInterest.setHasProperty(Arrays.asList("temperature,humidity".split(",")));
            sensor.setObservesProperty(Arrays.asList("temperature,humidity".split(",")));
            sensor.setLocatedAt(new WGS84Location(2.35, 40.8646, 12,
                    "Paris", Collections.singletonList("Somewhere in Paris")));
            cloudResource.setResource(sensor);
        } else if ( type==1) {
            //log.debug("Adding atmosphericPressure, carbonMonoxideConcentration to cloudResource");
            StationarySensor sensor = new StationarySensor();
            sensor.setName(resource.getName());
            sensor.setInterworkingServiceURL(resource.getInterworkingServiceURL());
            sensor.setDescription(Collections.singletonList("temperature"));
            FeatureOfInterest featureOfInterest = new FeatureOfInterest();
            featureOfInterest.setName("outside air");
            featureOfInterest.setDescription(Collections.singletonList("outside air quality"));
            featureOfInterest.setHasProperty(Arrays.asList("atmosphericPressure,carbonMonoxideConcentration".split(",")));
            sensor.setObservesProperty(Arrays.asList("atmosphericPressure,carbonMonoxideConcentration".split(",")));
            sensor.setLocatedAt(new WGS84Location(52.513681, 13.363782, 15,
                    "Berlin", Collections.singletonList("Grosser Tiergarten")));
            cloudResource.setResource(sensor);
        } else if (type==2) {
            //log.debug("Adding fields to service");

            Service service = new Service();
            service.setInterworkingServiceURL(resource.getInterworkingServiceURL());
            service.setName(resource.getName());
            List<String> descriptionList = Arrays.asList("@type=Beacon", "@beacon.id=f7826da6-4fa2-4e98-8024-bc5b71e0893e", "@beacon.major=44933", "@beacon.minor=46799", "@beacon.tx=0x50");
            service.setDescription(descriptionList);
            Parameter parameter = new Parameter();
            service.setParameters(Collections.singletonList(parameter));
            parameter.setName("inputParam1");
            parameter.setMandatory(true);
            // restriction
            LengthRestriction restriction = new LengthRestriction();
            restriction.setMin(2);
            restriction.setMax(10);
            parameter.setRestrictions(Collections.singletonList(restriction));

            PrimitiveDatatype datatype = new PrimitiveDatatype();
            datatype.setArray(false);
            datatype.setBaseDatatype("http://www.w3.org/2001/XMLSchema#string");
            parameter.setDatatype(datatype);
            cloudResource.setResource(service);

        } else if (type==3) {
            //log.debug("Adding fields to actuator");
            Actuator actuator = new Actuator();
            actuator.setInterworkingServiceURL(resource.getInterworkingServiceURL());
            actuator.setName(resource.getName());
            actuator.setDescription(Collections.singletonList("light"));
            actuator.setInterworkingServiceURL(resource.getInterworkingServiceURL());

            Capability capability = new Capability();
            actuator.setCapabilities(Collections.singletonList(capability));

            capability.setName("OnOffCapabililty");

            // parameters
            Parameter parameter = new Parameter();
            capability.setParameters(Collections.singletonList(parameter));
            parameter.setName("on");
            parameter.setMandatory(true);
            PrimitiveDatatype datatype = new PrimitiveDatatype();
            parameter.setDatatype(datatype);
            datatype.setBaseDatatype("boolean");
            actuator.setLocatedAt(new WGS84Location(2.645, 41.246, 15,
                    "Paris", Collections.singletonList("Somewhere in Paris")));
            cloudResource.setResource(actuator);

        } else  {
            //log.debug("Adding fields to actuator");
            Actuator actuator = new Actuator();
            actuator.setInterworkingServiceURL(resource.getInterworkingServiceURL());
            actuator.setName(resource.getName());
            actuator.setDescription(Collections.singletonList("light"));
            actuator.setInterworkingServiceURL(resource.getInterworkingServiceURL());

            Capability capability = new Capability();
            actuator.setCapabilities(Collections.singletonList(capability));

            capability.setName("OnOffCapabililty");

            // parameters
            Parameter parameter = new Parameter();
            capability.setParameters(Collections.singletonList(parameter));
            parameter.setName("on");
            parameter.setMandatory(true);
            PrimitiveDatatype datatype = new PrimitiveDatatype();
            parameter.setDatatype(datatype);
            datatype.setBaseDatatype("boolean");
            actuator.setLocatedAt(new WGS84Location(52.513681, 13.363782, 15,
                    "Berlin", Collections.singletonList("Grosser Tiergarten")));
            cloudResource.setResource(actuator);
        }
    }
    private static List<CloudResource> addresources(
            RHClient rhClient,
            String name,
            int restype){
        List<CloudResource> resources = new ArrayList<>();
        List<String> resourceNames = new ArrayList<>();
        {
            CloudResource cloudResource = new CloudResource();
            Resource resource = new Resource();
            cloudResource.setResource(resource);
            Long timeStamp = System.currentTimeMillis();
            cloudResource.setInternalId(name);

            String internalId = cloudResource.getInternalId();
            resource.setName(internalId);
            //resource.setId(timeStamp+internalId);
            resourceNames.add(resource.getName());
            resource.setDescription(Collections.singletonList("demo l1 resource"));
            resource.setInterworkingServiceURL("https://intracom.symbiote-h2020.eu");
            try {
                cloudResource.setAccessPolicy(new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC, null));
                cloudResource.setFilteringPolicy(new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC, null));
            } catch (Exception e) {
                e.printStackTrace();
            }
            getFields(cloudResource,restype);
            resources.add(cloudResource);
        }
        List lst = rhClient.addL1Resources(resources);
        return lst;
    }
    private static Object registerToPAAM(IAAMClient aamClient,
                                       String userUsername, String userPassword,
                                       String email,
                                       Map<String, String> attributes) {
        ManagementStatus mans = null;
        try {
            UserManagementRequest userManagementRequest = new UserManagementRequest(
                    new Credentials("icom", "icom"),
                    new Credentials(userUsername, userPassword),
                    new UserDetails(
                            new Credentials(userUsername, userPassword), // userCredentials
                            email, // recoveryMail
                            UserRole.USER, // UserRole
                            AccountStatus.ACTIVE, // AccountStatus
                            new HashMap<>(), // Map<String, String> attributes
                            new HashMap<>(), // Map<String, Certificate> clients
                            true, // serviceConsent
                            true // analyticsAndResearchConsent
                    ),
                    OperationType.CREATE);

            try {
                mans = aamClient.manageUser(userManagementRequest);
                //logInfo("User registration done");
            } catch (AAMException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mans;
    }
    public static String registerUser(
            String key,
            String PlatformIdentifier,
            String usrnm,
            String usrpsw,
            String email){

        List<CloudResource> lst = null;
        ManagementStatus mans = null;
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
                factory =
                        (AbstractSymbIoTeClientFactory) LocalData.getFac(key);
                /*factory = SymbioteResources.getNewFactory(
                        config,
                        usr,
                        psw,exampleHomePlatformIdentifier
                );*/
                //===========================================
                IAAMClient myaam = factory.getAAMClient(PlatformId);
                HashMap hm = new HashMap();
                hm.put("accessflag", "1");
                mans = (ManagementStatus) registerToPAAM(
                        myaam,
                        usrnm, usrpsw,
                        email,
                        hm);
            }catch(Exception fe){
                System.out.println("resource problem");
                return "resource not found";
            }
        }catch(Exception ex){
            System.out.println("Exception searchClient.search");
        }
        finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("2",mans.toString());
    }

}
