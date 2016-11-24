package uk.gov.register.core;

public enum HashingAlgorithm {
    SHA256 {
        @Override
        public String toString() {
            return "sha-256";
        }
    }
}