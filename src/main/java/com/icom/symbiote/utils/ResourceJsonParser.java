package com.icom.symbiote.utilities;

import eu.h2020.symbiote.model.cim.*;
import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class ResourceJsonParser {

    public static Resource ReadAddedResource(JSONObject resourceJson) throws Exception{
        String resourceType = resourceJson.getString("@c");
        switch (resourceType){
            case ".StationarySensor":
                StationarySensor  stationarySensor = ReadStationarySensor(resourceJson);
                return stationarySensor;

            case ".Actuator":
                Actuator actuator = ReadActuator(resourceJson);
                return actuator;

            case ".Service":
                Service service = ReadService(resourceJson);
                return service;

            case ".MobileSensor":
                MobileSensor mobileSensor = ReadMobileSensor(resourceJson);
                return mobileSensor;

            default:
                throw new Exception("The resource needs to have a type.");
        }
    }

    private static Service ReadService(JSONObject serviceJson) {
        Service service = new Service();
        Resource resource = ReadResource(serviceJson);

        List<Parameter> parameters = new ArrayList<Parameter>();
        Datatype resultType = new Datatype();
        //TODO all the properties are now optional, to decide if some are required
        //@interworkingServiceURL

        //@resultType
        if (serviceJson.has("resultType")) {
            JSONObject resultTypeJson = serviceJson.getJSONObject("resultType");
            try{
                resultType = ReadDatatype(resultTypeJson);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        //@Parameters
        if (serviceJson.has("parameters")) {
            JSONArray array = serviceJson.getJSONArray("parameters");
            for (int i = 0; i < array.length(); i++) {
                parameters.add(ReadParameter(array.getJSONObject(i)));
            }
        }

        service.setName(resource.getName());
        service.setDescription(resource.getDescription());
        service.setInterworkingServiceURL(resource.getInterworkingServiceURL());
        service.setResultType(resultType);
        service.setParameters(parameters);

        return service;
    }

    private static MobileSensor ReadMobileSensor(JSONObject mobileSensorJson) {
        MobileSensor mobileSensor = new MobileSensor();
        Sensor sensor = ReadSensor(mobileSensorJson);

        mobileSensor.setName(sensor.getName());
        mobileSensor.setInterworkingServiceURL(sensor.getInterworkingServiceURL());
        mobileSensor.setDescription(sensor.getDescription());
        mobileSensor.setServices(sensor.getServices());
        mobileSensor.setLocatedAt(sensor.getLocatedAt());
        mobileSensor.setObservesProperty(sensor.getObservesProperty());

        return mobileSensor;
    }

    private static Actuator ReadActuator(JSONObject actuatorJson){

        Actuator actuator = new Actuator();
        List<Capability> capabilities = new ArrayList<Capability>();
        Datatype datatype = new Datatype();

        Device device = ReadDevice(actuatorJson);

        if (actuatorJson.has("capabilities")) {
            JSONArray array = actuatorJson.getJSONArray("capabilities");
            for (int i = 0; i < array.length(); i++) {
                capabilities.add(ReadCapability(array.getJSONObject(i)));
            }
        }
        if (actuatorJson.has("datatype")){
            try{
                datatype = ReadDatatype(actuatorJson.getJSONObject("datatype"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        actuator.setName(device.getName());
        actuator.setDescription(device.getDescription());
        actuator.setServices(device.getServices());
        actuator.setLocatedAt(device.getLocatedAt());
        actuator.setInterworkingServiceURL(device.getInterworkingServiceURL());
        actuator.setCapabilities(capabilities);

        return actuator;
    }

    private static StationarySensor ReadStationarySensor(JSONObject stationarySensorJson){
        StationarySensor stationarySensor = new StationarySensor();

        Sensor sensor = ReadSensor(stationarySensorJson);
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();

        if (stationarySensorJson.has("featureOfInterest"))
            featureOfInterest = ReadFeatureOfInterest(stationarySensorJson.getJSONObject("featureOfInterest"));

        stationarySensor.setName(sensor.getName());
        stationarySensor.setDescription(sensor.getDescription());
        stationarySensor.setFeatureOfInterest(featureOfInterest);
        stationarySensor.setServices(sensor.getServices());
        stationarySensor.setInterworkingServiceURL(sensor.getInterworkingServiceURL());
        stationarySensor.setLocatedAt(sensor.getLocatedAt());
        stationarySensor.setServices(sensor.getServices());
        stationarySensor.setObservesProperty(sensor.getObservesProperty());

        return stationarySensor;
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
                descriptionList.add(array.get(i).toString());
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

    private static Resource ReadResource(JSONObject resourceJson){
        Resource resource = new Resource();
        String name = "";
        String interworkingServiceURL = "";
        List<String> description = new ArrayList<String>();
        if (resourceJson.has("name"))
            name = resourceJson.getString("name");

        if (resourceJson.has("interworkingServiceURL"))
                interworkingServiceURL = resourceJson.getString("interworkingServiceURL");

        if (resourceJson.has("description")) {
            JSONArray array = resourceJson.getJSONArray("description");
            for (int i = 0; i < array.length(); i++) {
                description.add(array.get(i).toString());
            }
        }
        resource.setName(name);
        resource.setInterworkingServiceURL(interworkingServiceURL);
        resource.setDescription(description);
        return resource;
    }

    private static Device ReadDevice(JSONObject deviceJson){
        Device device = new Device();

        if (deviceJson.has("locatedAt")) {
            try {
                device.setLocatedAt(ReadLocationJson(deviceJson.getJSONObject("locatedAt")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (deviceJson.has("services")) {
            List<Service> services = new ArrayList<Service>();
            JSONArray servicesArray = deviceJson.getJSONArray("services");
            for (int i = 0; i < servicesArray.length(); i++) {
                try {
                    services.add(ReadService(servicesArray.getJSONObject(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            device.setServices(services);
        }
        Resource resource = ReadResource(deviceJson);

        device.setName(resource.getName());
        device.setInterworkingServiceURL(resource.getInterworkingServiceURL());
        device.setDescription(resource.getDescription());

        return device;
    }

    private static Sensor ReadSensor(JSONObject sensorJson){
        Sensor sensor = new Sensor();
        if (sensorJson.has("observesProperty")) {
            List<String> observesPropertyList = new ArrayList<String>();
            JSONArray array = sensorJson.getJSONArray("observesProperty");
            for (int i = 0; i < array.length(); i++) {
                observesPropertyList.add(array.get(i).toString());
            }
            sensor.setObservesProperty(observesPropertyList);
        }

        Device device = ReadDevice(sensorJson);

        sensor.setName(device.getName());
        sensor.setInterworkingServiceURL(device.getInterworkingServiceURL());
        sensor.setDescription(device.getDescription());
        sensor.setServices(device.getServices());
        sensor.setLocatedAt(device.getLocatedAt());

        return sensor;
    }

    private static Parameter ReadParameter(JSONObject parameterJson){
        Parameter parameter = new Parameter();

        String name = "";
        Boolean mandatory = false;
        List<Restriction> restrictions = new ArrayList<Restriction>();
        Datatype datatype = new Datatype();

        if (parameterJson.has("name"))
            name = parameterJson.getString("name");

        if (parameterJson.has("mandatory"))
            mandatory = parameterJson.getBoolean("mandatory");

        if (parameterJson.has("restrictions")){
            JSONArray array = parameterJson.getJSONArray("restrictions");
            for (int i = 0; i < array.length(); i++) {
                try{
                    restrictions.add(ReadRestrictionJson(array.getJSONObject(i)));
                }
                catch(Exception e){
                    e.printStackTrace();
                }

            }
            parameter.setRestrictions(restrictions);
        }

//        if (parameterJson.has("datatype"))
//            datatype = parameterJson.getBoolean("mandatory");
        parameter.setName(name);
        parameter.setMandatory(mandatory);
        parameter.setRestrictions(restrictions);

        return parameter;
    }

    private static FeatureOfInterest ReadFeatureOfInterest(JSONObject featureOfInterestJson){
        FeatureOfInterest featureOfInterest = new FeatureOfInterest();

        String name = "";
        List<String> description = new ArrayList<String>();
        List<String> hasProperty = new ArrayList<String>();

        if (featureOfInterestJson.has("name"))
            name = featureOfInterestJson.getString("name");

        if (featureOfInterestJson.has("description")) {
            JSONArray array = featureOfInterestJson.getJSONArray("description");
            for (int i = 0; i < array.length(); i++) {
                description.add(array.get(i).toString());
            }
        }

        if (featureOfInterestJson.has("hasProperty")) {
            JSONArray array = featureOfInterestJson.getJSONArray("hasProperty");
            for (int i = 0; i < array.length(); i++) {
                hasProperty.add(array.get(i).toString());
            }
        }

        featureOfInterest.setName(name);
        featureOfInterest.setDescription(description);
        featureOfInterest.setHasProperty(hasProperty);

        return featureOfInterest;
    }

    private static Effect ReadEffect(JSONObject effectJson){
        Effect effect = new Effect();

        FeatureOfInterest actsOn = new FeatureOfInterest();
        List<String> affects = new ArrayList<String>();

        if (effectJson.has("actsOn"))
            actsOn = ReadFeatureOfInterest(effectJson.getJSONObject("actsOn"));

        if (effectJson.has("affects")) {
            JSONArray array = effectJson.getJSONArray("affects");
            for (int i = 0; i < array.length(); i++) {
                affects.add(array.get(i).toString());
            }
        }

        effect.setActsOn(actsOn);
        effect.setAffects(affects);

        return effect;
    }

    private static Capability ReadCapability(JSONObject capabilityJson){
        Capability capability = new Capability();

        String name = "";
        List<Parameter> parameters = new ArrayList<Parameter>();
        List<Effect> effects = new ArrayList<Effect>();


        if (capabilityJson.has("name"))
            name = capabilityJson.getString("name");

        if (capabilityJson.has("parameters")) {
            JSONArray array = capabilityJson.getJSONArray("parameters");
            for (int i = 0; i < array.length(); i++) {
                parameters.add(ReadParameter(array.getJSONObject(i)));
            }
        }

        if (capabilityJson.has("effects")) {
            JSONArray array = capabilityJson.getJSONArray("effects");
            for (int i = 0; i < array.length(); i++) {
                effects.add(ReadEffect(array.getJSONObject(i)));
            }
        }

        capability.setName(name);
        capability.setParameters(parameters);
        capability.setEffects(effects);

        return capability;
    }

    private static Datatype ReadDatatype(JSONObject datatypeJson) throws Exception{
        boolean isArray = false;
        String datatype = datatypeJson.getString("@c");

        if (datatypeJson.has("isArray"))
            isArray = datatypeJson.getBoolean("isArray");

        switch(datatype){
            case ".PrimitiveDatatype":
                PrimitiveDatatype primitiveDatatype = new PrimitiveDatatype();
                String baseDatatype = "";
                if (datatypeJson.has("baseDatatype"))
                    baseDatatype = datatypeJson.getString("baseDatatype");
                primitiveDatatype.setBaseDatatype(baseDatatype);
                primitiveDatatype.setArray(isArray);
                return primitiveDatatype;

            case ".ComplexDatatype":
                ComplexDatatype complexDatatype = new ComplexDatatype();
                String basedOnClass = "";
                List<DataProperty> dataProperties = new ArrayList<DataProperty>();

                if (datatypeJson.has("basedOnClass"))
                    basedOnClass = datatypeJson.getString("basedOnClass");

                if (datatypeJson.has("dataProperties")) {
                    JSONArray array = datatypeJson.getJSONArray("dataProperties");
                    for (int i = 0; i < array.length(); i++) {
                        try{
                            dataProperties.add(ReadDataProperty(array.getJSONObject(i)));
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                complexDatatype.setBasedOnClass(basedOnClass);
                complexDatatype.setArray(isArray);
                return complexDatatype;
            default:
                throw new Exception("The datatype needs to have a type.");
        }
    }

    private static DataProperty ReadDataProperty(JSONObject dataPropertyJson) throws Exception{
        String dataPropertyType = dataPropertyJson.getString("@c");
        String name = "";

        if (dataPropertyJson.has("name"))
            name = dataPropertyJson.getString("name");

        String basedOnProperty = "";
        if (dataPropertyJson.has("basedOnProperty"))
            basedOnProperty = dataPropertyJson.getString("basedOnProperty");

        switch (dataPropertyType){
            case ".PrimitiveProperty":
                PrimitiveProperty primitiveProperty = new PrimitiveProperty();

                if (dataPropertyJson.has("datatype")){
                    JSONObject object = dataPropertyJson.getJSONObject("datatype");
                    object.put("@c", ".PrimitiveDatatype");
                    PrimitiveDatatype primitiveDatatype = (PrimitiveDatatype) ReadDatatype(object);
                    primitiveProperty.setPrimitiveDatatype(primitiveDatatype);
                }
                primitiveProperty.setBasedOnProperty(basedOnProperty);
                primitiveProperty.setName(name);
                return primitiveProperty;

            case ".ComplexProperty":
                ComplexProperty complexProperty = new ComplexProperty();
                if (dataPropertyJson.has("datatype")){
                    JSONObject object = dataPropertyJson.getJSONObject("datatype");
                    object.put("@c", ".ComplexDatatype");
                    ComplexDatatype complexDatatype = (ComplexDatatype) ReadDatatype(object);
                    complexProperty.setDatatype(complexDatatype);
                }
                complexProperty.setBasedOnProperty(basedOnProperty);
                complexProperty.setName(name);
                return complexProperty;

            default:
                throw new Exception("The property needs to have a type. The property was not added.");
        }
    }
}
