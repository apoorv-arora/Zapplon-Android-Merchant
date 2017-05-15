package com.application.zapplonmerchant.data;

import java.io.Serializable;

public class UserWish implements Serializable{
	private int userWishId;
	private int storeItemId;// storeItemId
	private int userId;

	private String userKey;
	private String pin;
	private double billAmount;
	
	private Store store;
	
	private User user;
	private int storeId;
	private long startDate;
	private long endDate;
	private int crowd;

	public UserWish() {
	}

	public int getUserWishId() {
		return userWishId;
	}

	public void setUserWishId(int userWishId) {
		this.userWishId = userWishId;
	}

	public int getStoreItemId() {
		return storeItemId;
	}

	public void setStoreItemId(int storeId) {
		this.storeItemId = storeId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public double getBillAmount() {
		return billAmount;
	}

	public void setBillAmount(double billAmount) {
		this.billAmount = billAmount;
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public long getStartDate() {
		return startDate;
	}

	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}

	public long getEndDate() {
		return endDate;
	}

	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	public int getCrowd() {
		return crowd;
	}

	public void setCrowd(int crowd) {
		this.crowd = crowd;
	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
