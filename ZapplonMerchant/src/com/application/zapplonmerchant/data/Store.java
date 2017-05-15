package com.application.zapplonmerchant.data;

import java.io.Serializable;

public class Store implements Serializable {

	private int storeId;
	private int storeType;
	private String contactNumber;
	private double latitude;
	private double longitude;
	private String address;
	private String storeName;
	private int merchantId;
	private int availability;
	private int maxOccupancy;
	private int currentOccupancy;
	
	private StoreCatalogueItem storeItem;
	private boolean acceptsReservation;

	public Store() {
	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public int getStoreType() {
		return storeType;
	}

	public void setStoreType(int storeType) {
		this.storeType = storeType;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
	
	public int getAvailability() {
		return availability;
	}

	public void setAvailability(int availability) {
		this.availability = availability;
	}

	public StoreCatalogueItem getStoreItem() {
		return storeItem;
	}

	public void setStoreItem(StoreCatalogueItem storeItem) {
		this.storeItem = storeItem;
	}

	public int getMaxOccupancy() {
		return maxOccupancy;
	}

	public void setMaxOccupancy(int maxOccupancy) {
		this.maxOccupancy = maxOccupancy;
	}

	public int getCurrentOccupancy() {
		return currentOccupancy;
	}

	public void setCurrentOccupancy(int currentOccupancy) {
		this.currentOccupancy = currentOccupancy;
	}

	public boolean isAcceptsReservation() {
		return acceptsReservation;
	}

	public void setAcceptsReservation(boolean acceptsReservation) {
		this.acceptsReservation = acceptsReservation;
	}
	
}