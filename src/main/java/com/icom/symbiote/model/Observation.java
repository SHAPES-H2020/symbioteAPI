package com.icom.symbiote.model;

import java.util.List;

public class Observation {
    private String resourceId;
    private List<Value> obsValues;

    public Observation() {
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public List<Value> getObsValues() {
        return obsValues;
    }

    public void setObsValues(List<Value> obsValues) {
        this.obsValues = obsValues;
    }


    public static class Value {
        private String value;
        private Property obsProperty;
        private Uom uom;

        public Value() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Property getObsProperty() {
            return obsProperty;
        }

        public void setObsProperty(Property obsProperty) {
            this.obsProperty = obsProperty;
        }

        public Uom getUom() {
            return uom;
        }

        public void setUom(Uom uom) {
            this.uom = uom;
        }
    }

    public static class Property {
        private String name;
        private String iri;
        private List<String> description;

        public Property() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }

        public List<String> getDescription() {
            return description;
        }

        public void setDescription(List<String> description) {
            this.description = description;
        }
    }

    public static class Uom {
        private String symbol;
        private String name;
        private String iri;
        private List<String> description;

        public Uom() {
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIri() {
            return iri;
        }

        public void setIri(String iri) {
            this.iri = iri;
        }

        public List<String> getDescription() {
            return description;
        }

        public void setDescription(List<String> description) {
            this.description = description;
        }
    }
}
