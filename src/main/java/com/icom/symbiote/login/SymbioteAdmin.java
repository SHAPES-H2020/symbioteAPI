package com.icom.symbiote.login;

import com.icom.symbiote.utilities.LocalData;
import com.icom.symbiote.utilities.ResourceJsonParser;
import com.icom.symbiote.utilities.ResultUtils;
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
            String PlatformId,
            JSONObject resource) {

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
            String coreAddress = "https://intracom-core.symbiote-h2020.eu";
            String keystorePath = "testKeystore";
            String keystorePassword = "testKeystore";
            String exampleHomePlatformIdentifier = "SymbIoTe_Core_AAM";
            AbstractSymbIoTeClientFactory.Type type = AbstractSymbIoTeClientFactory.Type.FEIGN;
            try {
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
                lst = addresources(rhClient, intId, restype, resource);
                /*
                List lst = rhClient.addL1RdfResources(getRdfList(
                        rdfStr,
                        intId,
                        plugId,
                        mapKey
                ));*/
            } catch (Exception fe) {
                System.out.println("resource problem");
                return ResultUtils.getResult("2", "resource not found");
            }
        } catch (Exception ex) {
            System.out.println("Exception searchClient.search");
            return ResultUtils.getResult("1", "Exception searchClient.search");
        } finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("0", Integer.toString(lst.size()));
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

    static void getFields(CloudResource cloudResource, int type) {
        // long randomizer = System.currentTimeMillis();

        cloudResource.setPluginId("RapPluginExample");
        Resource resource = cloudResource.getResource();

        if (type == 0) {
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
        } else if (type == 1) {
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
        } else if (type == 2) {
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

        } else if (type == 3) {
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

        } else {
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
            int restype,
            JSONObject resourceJson) {
        List<CloudResource> resources = new ArrayList<CloudResource>();
        List<String> resourceNames = new ArrayList<String>();
        {
            CloudResource cloudResource = new CloudResource();
            ResourceJsonParser resourceJsonParser = new ResourceJsonParser();
            try{
                Resource resource = resourceJsonParser.ReadAddedResource(resourceJson);
                cloudResource.setResource(resource);
                Long timeStamp = System.currentTimeMillis();
                cloudResource.setInternalId(name);

                String internalId = cloudResource.getInternalId();
                resource.setName(internalId);
                //resource.setId(timeStamp+internalId);
                resourceNames.add(resource.getName());
            }
            catch (Exception e){
                e.printStackTrace();
            }
//            resource.setDescription(Collections.singletonList("demo l1 resource"));
//            resource.setInterworkingServiceURL("https://intracom.symbiote-h2020.eu");
            try {
                cloudResource.setAccessPolicy(new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC, null));
                cloudResource.setFilteringPolicy(new SingleTokenAccessPolicySpecifier(AccessPolicyType.PUBLIC, null));
            } catch (Exception e) {
                e.printStackTrace();
            }
//            getFields(cloudResource, restype);
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
            String email) {

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
            String coreAddress = "https://intracom-core.symbiote-h2020.eu";
            String keystorePath = "testKeystore";
            String keystorePassword = "testKeystore";
            String exampleHomePlatformIdentifier = "SymbIoTe_Core_AAM";
            AbstractSymbIoTeClientFactory.Type type = AbstractSymbIoTeClientFactory.Type.FEIGN;
            try {
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
            } catch (Exception fe) {
                System.out.println("resource problem");
                return "resource not found";
            }
        } catch (Exception ex) {
            System.out.println("Exception searchClient.search");
        } finally {
            File ksFile = new File("testKeystore");
            if (ksFile.exists()) ksFile.delete();
        }
        return ResultUtils.getResult("2", mans.toString());
    }

    private static Service ReadServiceResource(JSONObject serviceJson) {
        Service service = new Service();
        //TODO all the properties are now optional, to decide if some are required
        //@name
        if (serviceJson.has("name"))
            service.setName(serviceJson.getString("name"));

        //@description
        if (serviceJson.has("description")) {
            List<String> descriptionList = new ArrayList<String>();
            JSONArray array = serviceJson.getJSONArray("description");
            for (int i = 0; i < array.length(); i++) {
                descriptionList.add(array.getJSONObject(i).toString());
            }
            service.setDescription(descriptionList);
        }
        //@interworkingServiceURL
        if (serviceJson.has("interworkingServiceURL"))
            service.setInterworkingServiceURL(serviceJson.getString("interworkingServiceURL"));

        //@resultType
        if (serviceJson.has("resultType")) {
            JSONObject resultTypeJson = serviceJson.getJSONObject("resultType");
            Datatype resultType = new Datatype();
            if (resultTypeJson.has("array")) {
                resultType.setArray(Boolean.parseBoolean(resultTypeJson.getString("array")));
                service.setResultType(resultType);
            }
        }

        //@Parameters
        if (serviceJson.has("parameters")) {
            List<Parameter> parameterList = new ArrayList<Parameter>();
            JSONArray parameterArray = serviceJson.getJSONArray("parameters");
            for (int i = 0; i < parameterArray.length(); i++) {
                JSONObject parameterJson = parameterArray.getJSONObject(i);
                Parameter parameter = new Parameter();
                //@Parameter.name
                if (parameterJson.has("name"))
                    parameter.setName(parameterJson.getString("name"));

                //@Paramater.mandatory
                if (parameterJson.has("mandatory"))
                    parameter.setMandatory(Boolean.parseBoolean(parameterJson.getString("mandatory")));

                //@Parameter.restrictions
                if (parameterJson.has("restrictions")) {
                    List<Restriction> restrictionsList = new ArrayList<Restriction>();
                    JSONArray restrictionArray = serviceJson.getJSONArray("restrictions");
                    for (int j = 0; j < restrictionArray.length(); j++) {
                        JSONObject restrictionJson = restrictionArray.getJSONObject(j);
                        try {
                            restrictionsList.add(ReadRestrictionJson(restrictionJson));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                //@Parameter DataType
//                if (parameterJson.has("datatype"))
            }
        }

        return service;
    }

    private static MobileSensor ReadMobileSensorResource(JSONObject mobileSensorJson) {
        MobileSensor mobileSensor = new MobileSensor();
        if (mobileSensorJson.has("name"))
            mobileSensor.setName(mobileSensorJson.getString("name"));

        if (mobileSensorJson.has("description")) {
            List<String> descriptionList = new ArrayList<String>();
            JSONArray array = mobileSensorJson.getJSONArray("description");
            for (int i = 0; i < array.length(); i++) {
                descriptionList.add(array.getJSONObject(i).toString());
            }
            mobileSensor.setDescription(descriptionList);
        }

        //@interworkingServiceURL
        if (mobileSensorJson.has("interworkingServiceURL"))
            mobileSensor.setInterworkingServiceURL(mobileSensorJson.getString("interworkingServiceURL"));
        if (mobileSensorJson.has("locatedAt")) {
            try {
                mobileSensor.setLocatedAt(ReadLocationJson(mobileSensorJson.getJSONObject("locatedAt")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mobileSensorJson.has("services")) {
            List<Service> services = new ArrayList<Service>();
            JSONArray servicesArray = mobileSensorJson.getJSONArray("services");
            for (int i = 0; i < servicesArray.length(); i++) {
                try {
                    services.add(ReadServiceResource(servicesArray.getJSONObject(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mobileSensor.setServices(services);
        }

        if (mobileSensorJson.has("observesProperty")) {
            List<String> observesPropertyList = new ArrayList<String>();
            JSONArray array = mobileSensorJson.getJSONArray("observesProperty");
            for (int i = 0; i < array.length(); i++) {
                observesPropertyList.add(array.getJSONObject(i).toString());
            }
            mobileSensor.setObservesProperty(observesPropertyList);
        }

        return mobileSensor;
    }

    private static Restriction ReadRestrictionJson(JSONObject restrictionJson) throws Exception {
        String restrictionType = restrictionJson.getString("@c");
        switch (restrictionType) {
            case ".EnumRestriction":
                EnumRestriction enumRestriction = new EnumRestriction();
                if (restrictionJson.has("values")) {
                    List<String> values = new ArrayList<String>();
                    JSONArray valuesArray = restrictionJson.getJSONArray("values");
                    for (int i = 0; i < valuesArray.length(); i++) {
                        values.add(valuesArray.getJSONObject(i).toString());
                    }
                    enumRestriction.setValues(values);
                }
                return enumRestriction;

            case ".LengthRestriction":
                LengthRestriction lengthRestriction = new LengthRestriction();
                if (restrictionJson.has("min"))
                    lengthRestriction.setMin(restrictionJson.getInt("min"));
                if (restrictionJson.has("max"))
                    lengthRestriction.setMax(restrictionJson.getInt("max"));
                return lengthRestriction;

            case ".RangeRestriction":
                RangeRestriction rangeRestriction = new RangeRestriction();
                if (restrictionJson.has("min"))
                    rangeRestriction.setMin(restrictionJson.getDouble("min"));
                if (restrictionJson.has("max"))
                    rangeRestriction.setMax(restrictionJson.getDouble("max"));
                return rangeRestriction;

            case ".RegExRestriction":
                RegExRestriction regExRestriction = new RegExRestriction();
                if (restrictionJson.has("pattern"))
                    regExRestriction.setPattern(restrictionJson.getString("pattern"));
                return regExRestriction;

            case ".InstanceOfRestriction":
                InstanceOfRestriction instanceOfRestriction = new InstanceOfRestriction();
                if (restrictionJson.has("instanceOfClass"))
                    instanceOfRestriction.setInstanceOfClass(restrictionJson.getString("instanceOfClass"));
                if (restrictionJson.has("valueProperty"))
                    instanceOfRestriction.setValueProperty(restrictionJson.getString("valueProperty"));
                return instanceOfRestriction;

            default:
                throw new Exception("The restriction needs to have a type. The restriction was not added.");
        }
    }

    private static Location ReadLocationJson(JSONObject locationJson) throws Exception {
        String locationType = locationJson.getString("@c");
        String name = "";
        List<String> descriptionList = new ArrayList<String>();
        if (locationJson.has("name"))
            name = locationJson.getString("name");
        if (locationJson.has("description")) {
            JSONArray array = locationJson.getJSONArray("description");
            for (int i = 0; i < array.length(); i++) {
                descriptionList.add(array.getJSONObject(i).toString());
            }
        }
        switch (locationType) {
            //TODO the WGS84Location seems to have all these properties required, the name and description can be blank
            case ".WGS84Location":
                double longtitude = locationJson.getDouble("longitude");
                double latitude = locationJson.getDouble("latitude");
                double altitude = locationJson.getDouble("altitude");

                WGS84Location wgs84Location = new WGS84Location(longtitude, latitude, altitude, name, descriptionList);
                return wgs84Location;

            case ".WKTLocation":
                String value = "";
                if (locationJson.has("value"))
                    value = locationJson.getString("value");

                WKTLocation wktLocation = new WKTLocation(value, name, descriptionList);
                return wktLocation;

            case ".SymbolicLocation":
                SymbolicLocation symbolicLocation = new SymbolicLocation();
                symbolicLocation.setName(name);
                symbolicLocation.setDescription(descriptionList);
                return symbolicLocation;

            default:
                throw new Exception("The location needs to have a type. The location was not added.");
        }
    }
}

//    private static Datatype ReadJsonDatatype(JSONObject datatypeJson) throws Exception{
//        String datatypeType = datatypeJson.getString("@c");
////        switch ()
//    }
