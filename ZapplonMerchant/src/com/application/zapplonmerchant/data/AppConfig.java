package com.application.zapplonmerchant.data;

import java.io.Serializable;

public class AppConfig implements Serializable {
	
	private double version;
	private String contact;
	
	public AppConfig(){
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}
	
}
