package com.example.gamepadkeyboard;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RadialMenuSet {

    @JsonProperty("base") // Annotation for Jackson
    private RadialMenuPair base;

    @JsonProperty("alt") // Annotation for Jackson
    private RadialMenuPair alt;

    // Default constructor for deserialization
    public RadialMenuSet() {
    }

    public RadialMenuSet(RadialMenuPair base, RadialMenuPair alt) {
        this.base = base;
        this.alt = alt;
    }

    public RadialMenuPair getBase() {
        return base;
    }

    public void setBase(RadialMenuPair base) {
        this.base = base;
    }

    public RadialMenuPair getAlt() {
        return alt;
    }

    public void setAlt(RadialMenuPair alt) {
        this.alt = alt;
    }

    public RadialMenuPair getPair(boolean isAlt) {
        return isAlt ? alt : base;
    }

    public static class RadialMenuPair {

        @JsonProperty("left") // Annotation for Jackson
        private RadialMenuKeys left;

        @JsonProperty("right") // Annotation for Jackson
        private RadialMenuKeys right;

        // Default constructor for deserialization
        public RadialMenuPair() {
        }

        public RadialMenuPair(RadialMenuKeys left, RadialMenuKeys right) {
            this.left = left;
            this.right = right;
        }

        public RadialMenuKeys getLeft() {
            return left;
        }

        public void setLeft(RadialMenuKeys left) {
            this.left = left;
        }

        public RadialMenuKeys getRight() {
            return right;
        }

        public void setRight(RadialMenuKeys right) {
            this.right = right;
        }
    }

    public static class RadialMenuKeys {

        @JsonProperty("lower") // Annotation for Jackson, good practice
        private List<String> lower;

        @JsonProperty("upper") // Annotation for Jackson, good practice
        private List<String> upper;

        // Default constructor is needed by many JSON deserialization libraries
        public RadialMenuKeys() {
        }

        public RadialMenuKeys(List<String> lower, List<String> upper) {
            this.lower = lower;
            this.upper = upper;
        }

        public List<String> getLower() {
            return lower;
        }

        public void setLower(List<String> lower) {
            this.lower = lower;
        }

        public List<String> getUpper() {
            return upper;
        }

        public void setUpper(List<String> upper) {
            this.upper = upper;
        }
    }
}