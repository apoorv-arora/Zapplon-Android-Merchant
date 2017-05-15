package com.application.zapplonmerchant.data;

import java.io.Serializable;
import java.util.Date;

import android.text.format.Time;

public class StoreCatalogueItem implements Serializable {

	private int storeItemId;

	private int storeId;
	private String details;
	private int storeType;
	private String contactNumber;
	private double latitude;
	private double longitude;
	private String address;
	private String storeName;

	// deal structure
	private int dealType;
	private int dealSubType;
	private int discountAmount;
	private int discountSecondAmount;
	private int minOrder;
	private int maxOrder;
	private int count;

	private String productName;

	private int status;

	// active days
	private boolean mon;
	private boolean tue;
	private boolean wed;
	private boolean thu;
	private boolean fri;
	private boolean sat;
	private boolean sun;

	// active time
	private int startingHour;
	private int endingHour;
	private int startingMin;
	private int endingMin;

	private long endTime;
	
	private boolean hasProductDeal;

	private String productNameMon;
	private String productNameTue;
	private String productNameWed;
	private String productNameThu;
	private String productNameFri;
	private String productNameSat;
	private String productNameSun;

	private int discountAmountMon;
	private int discountAmountTue;
	private int discountAmountWed;
	private int discountAmountThu;
	private int discountAmountFri;
	private int discountAmountSat;
	private int discountAmountSun;
	
	private int maximumDiscount;
	
	private int actionMode;

	public StoreCatalogueItem() {
	}

	public int getStoreItemId() {
		return storeItemId;
	}

	public void setStoreItemId(int storeItemId) {
		this.storeItemId = storeItemId;
	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
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

	public int getDealType() {
		return dealType;
	}

	public void setDealType(int dealType) {
		this.dealType = dealType;
	}

	public int getDealSubType() {
		return dealSubType;
	}

	public void setDealSubType(int dealSubType) {
		this.dealSubType = dealSubType;
	}

	public int getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(int discountAmount) {
		this.discountAmount = discountAmount;
	}

	public int getMinOrder() {
		return minOrder;
	}

	public void setMinOrder(int minOrder) {
		this.minOrder = minOrder;
	}

	public int getMaxOrder() {
		return maxOrder;
	}

	public void setMaxOrder(int maxOrder) {
		this.maxOrder = maxOrder;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getDiscountSecondAmount() {
		return discountSecondAmount;
	}

	public void setDiscountSecondAmount(int discountSecondAmount) {
		this.discountSecondAmount = discountSecondAmount;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public boolean isMon() {
		return mon;
	}

	public void setMon(boolean mon) {
		this.mon = mon;
	}

	public boolean isTue() {
		return tue;
	}

	public void setTue(boolean tue) {
		this.tue = tue;
	}

	public boolean isWed() {
		return wed;
	}

	public void setWed(boolean wed) {
		this.wed = wed;
	}

	public boolean isThu() {
		return thu;
	}

	public void setThu(boolean thu) {
		this.thu = thu;
	}

	public boolean isFri() {
		return fri;
	}

	public void setFri(boolean fri) {
		this.fri = fri;
	}

	public boolean isSat() {
		return sat;
	}

	public void setSat(boolean sat) {
		this.sat = sat;
	}

	public boolean isSun() {
		return sun;
	}

	public void setSun(boolean sun) {
		this.sun = sun;
	}

	public int getStartingHour() {
		return startingHour;
	}

	public void setStartingHour(int startingHour) {
		this.startingHour = startingHour;
	}

	public int getEndingHour() {
		return endingHour;
	}

	public void setEndingHour(int endingHour) {
		this.endingHour = endingHour;
	}

	public int getStartingMin() {
		return startingMin;
	}

	public void setStartingMin(int startingMin) {
		this.startingMin = startingMin;
	}

	public int getEndingMin() {
		return endingMin;
	}

	public void setEndingMin(int endingMin) {
		this.endingMin = endingMin;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public boolean isHasProductDeal() {
		return hasProductDeal;
	}

	public void setHasProductDeal(boolean hasProductDeal) {
		this.hasProductDeal = hasProductDeal;
	}

	public String getProductNameMon() {
		return productNameMon;
	}

	public void setProductNameMon(String productNameMon) {
		this.productNameMon = productNameMon;
	}

	public String getProductNameTue() {
		return productNameTue;
	}

	public void setProductNameTue(String productNameTue) {
		this.productNameTue = productNameTue;
	}

	public String getProductNameWed() {
		return productNameWed;
	}

	public void setProductNameWed(String productNameWed) {
		this.productNameWed = productNameWed;
	}

	public String getProductNameThu() {
		return productNameThu;
	}

	public void setProductNameThu(String productNameThu) {
		this.productNameThu = productNameThu;
	}

	public String getProductNameFri() {
		return productNameFri;
	}

	public void setProductNameFri(String productNameFri) {
		this.productNameFri = productNameFri;
	}

	public String getProductNameSat() {
		return productNameSat;
	}

	public void setProductNameSat(String productNameSat) {
		this.productNameSat = productNameSat;
	}

	public String getProductNameSun() {
		return productNameSun;
	}

	public void setProductNameSun(String productNameSun) {
		this.productNameSun = productNameSun;
	}

	public int getDiscountAmountMon() {
		return discountAmountMon;
	}

	public void setDiscountAmountMon(int discountAmountMon) {
		this.discountAmountMon = discountAmountMon;
	}

	public int getDiscountAmountTue() {
		return discountAmountTue;
	}

	public void setDiscountAmountTue(int discountAmountTue) {
		this.discountAmountTue = discountAmountTue;
	}

	public int getDiscountAmountWed() {
		return discountAmountWed;
	}

	public void setDiscountAmountWed(int discountAmountWed) {
		this.discountAmountWed = discountAmountWed;
	}

	public int getDiscountAmountThu() {
		return discountAmountThu;
	}

	public void setDiscountAmountThu(int discountAmountThu) {
		this.discountAmountThu = discountAmountThu;
	}

	public int getDiscountAmountFri() {
		return discountAmountFri;
	}

	public void setDiscountAmountFri(int discountAmountFri) {
		this.discountAmountFri = discountAmountFri;
	}

	public int getDiscountAmountSat() {
		return discountAmountSat;
	}

	public void setDiscountAmountSat(int discountAmountSat) {
		this.discountAmountSat = discountAmountSat;
	}

	public int getDiscountAmountSun() {
		return discountAmountSun;
	}

	public void setDiscountAmountSun(int discountAmountSun) {
		this.discountAmountSun = discountAmountSun;
	}

	public int getMaximumDiscount() {
		return maximumDiscount;
	}

	public void setMaximumDiscount(int maximumDiscount) {
		this.maximumDiscount = maximumDiscount;
	}

	public int getActionMode() {
		return actionMode;
	}

	public void setActionMode(int actionMode) {
		this.actionMode = actionMode;
	}
	
}
