package com.application.zapplonmerchant.data;

import java.io.Serializable;

public class Merchant implements Serializable {
	private int merchantId;
	private String email;
	private String merchantName;

	public Merchant() {
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

}