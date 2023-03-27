package com.tarento.frac.validation;

import java.security.PublicKey;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KeyData {
	private String keyId;
	private PublicKey publicKey;

	public KeyData(String keyId, PublicKey publicKey) {
		this.keyId = keyId;
		this.publicKey = publicKey;
	}
}
