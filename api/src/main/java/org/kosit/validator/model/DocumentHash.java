package org.kosit.validator.model;

public final record DocumentHash(String hashAlgorithm, byte[] hashValue) {
}
