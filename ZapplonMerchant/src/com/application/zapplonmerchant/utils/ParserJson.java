package com.application.zapplonmerchant.utils;

import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.application.zapplonmerchant.data.AppConfig;
import com.application.zapplonmerchant.data.Merchant;
import com.application.zapplonmerchant.data.Store;
import com.application.zapplonmerchant.data.StoreCatalogueItem;
import com.application.zapplonmerchant.data.User;
import com.application.zapplonmerchant.data.UserWish;

public class ParserJson {

	@SuppressWarnings("resource")
	public static JSONObject convertInputStreamToJSON(InputStream is) throws JSONException {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		String responseJSON = s.hasNext() ? s.next() : "";

		CommonLib.ZLog("response", responseJSON);
		JSONObject map = new JSONObject(responseJSON);
		CommonLib.ZLog("RESPONSE", map.toString(2));
		return map;
	}

	public static Object[] parseSignupResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseRedeemCouponResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseCouponUploadResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseInstitutionResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseLoginResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject responseJson = responseObject.getJSONObject("response");
					Object[] outputArr = new Object[3];
					if (responseJson.has("access_token"))
						outputArr[0] = String.valueOf(responseJson.get("access_token"));

					if (responseJson.has("merchant") && responseJson.get("merchant") instanceof JSONObject
							&& responseJson.getJSONObject("merchant").has("merchant")
							&& responseJson.getJSONObject("merchant").get("merchant") instanceof JSONObject) {
						outputArr[1] = parse_Merchant(responseJson.getJSONObject("merchant").getJSONObject("merchant"));
					}

					if (responseJson.has("store") && responseJson.get("store") instanceof JSONObject
							&& responseJson.getJSONObject("store").has("store")
							&& responseJson.getJSONObject("store").get("store") instanceof JSONObject) {
						outputArr[2] = parse_Store(responseJson.getJSONObject("store").getJSONObject("store"));
					}

					output[1] = outputArr;
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static JSONObject parseFBLoginResponse(InputStream is) throws JSONException {

		JSONObject output = new JSONObject();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			String out = responseObject.getString("status");
			if (out.equals("success")) {
				if (responseObject.has("response")) {
					output = new JSONObject(String.valueOf(responseObject.get("response")));
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output.put("error", responseObject.getString("errorMessage"));
				}
			}
		}
		return output;
	}

	public static Object[] parseLogoutResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseAddStoreResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject responseJson = responseObject.getJSONObject("response");

					if (responseJson.has("store") && responseJson.get("store") instanceof JSONObject) {
						output[1] = parse_Store(responseJson.getJSONObject("store"));
					}
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseLocationUpdateResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject responseJson = responseObject.getJSONObject("response");

					if (responseJson.has("store") && responseJson.get("store") instanceof JSONObject) {
						output[1] = parse_Store(responseJson.getJSONObject("store"));
					}
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseUpdateAvailabilityResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject responseJson = responseObject.getJSONObject("response");

					if (responseJson.has("store") && responseJson.get("store") instanceof JSONObject) {
						output[1] = parse_Store(responseJson.getJSONObject("store"));
					}
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseWishPostResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parsePasswordUpdateResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseUpdateOccupancyResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}
	
	

	public static Object[] parseWishDeletePostResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}
	
	public static Object[] parseGenericResponse(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parse_Deals(InputStream is) throws JSONException {

		Object[] objects = new Object[2];
		ArrayList<StoreCatalogueItem> wishes = new ArrayList<StoreCatalogueItem>();
		int size = 0;
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject categoriesObject = responseObject.getJSONObject("response");
					if (categoriesObject.has("total") && categoriesObject.get("total") instanceof Integer)
						size = categoriesObject.getInt("total");
					if (categoriesObject.has("stores") && categoriesObject.get("stores") instanceof JSONArray) {
						JSONArray categoriesArr = categoriesObject.getJSONArray("stores");
						for (int i = 0; i < categoriesArr.length(); i++) {
							JSONObject categoryJson = categoriesArr.getJSONObject(i);
							if (categoryJson.has("store") && categoryJson.get("store") instanceof JSONObject) {
								categoryJson = categoryJson.getJSONObject("store");
								wishes.add(parse_StoreCatalogueItem(categoryJson));
							}
						}
					}
				}
			}
		}
		objects[0] = size;
		objects[1] = wishes;
		return objects;
	}

	public static Object[] parse_VerificationMessage(InputStream is) throws JSONException {

		Object[] output = new Object[] { "failed", "", null };

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					UserWish category = new UserWish();

					JSONObject responseJson = responseObject.getJSONObject("response");

					if (responseJson.has("key")) {
						category.setUserKey(String.valueOf(responseJson.get("key")));
					}

					if (responseJson.has("pin")) {
						category.setPin(String.valueOf(responseJson.get("pin")));
					}

					if (responseJson.has("bill_amount") && responseJson.get("bill_amount") instanceof Double) {
						category.setBillAmount(responseJson.getDouble("bill_amount"));
					}

					if (responseJson.has("store") && responseJson.get("store") instanceof JSONObject) {
						Store store = new Store();
						JSONObject userWishJson = responseJson.getJSONObject("store");

						if (userWishJson.has("store") && userWishJson.get("store") instanceof JSONObject) {
							JSONObject storeJson = userWishJson.getJSONObject("store");
							store = parse_Store(storeJson);
						}
						category.setStore(store);
					}

					output[0] = category;
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static AppConfig parse_AppConfig(InputStream is) throws JSONException {

		AppConfig object = new AppConfig();
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
					JSONArray categoryArr = responseObject.getJSONArray("response");

					for (int i = 0; i < categoryArr.length(); i++) {
						JSONObject categoryJson = categoryArr.getJSONObject(i);

						if (categoryJson.has("key") && categoryJson.get("key").equals(CommonLib.SERVER_VERSION_STRING)
								&& categoryJson.has("value")) {
							try {
								double version = Double.parseDouble(String.valueOf(categoryJson.get("value")));
								object.setVersion(version);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							}
						}

						if (categoryJson.has("key") && categoryJson.get("key").equals(CommonLib.SERVER_SUPPORT_STRING)
								&& categoryJson.has("value")) {
							object.setContact(String.valueOf(categoryJson.get("value")));
						}

					}
				}
			}
		}
		return object;
	}

	public static ArrayList<Store> parse_Stores(InputStream is) throws JSONException {

		ArrayList<Store> object = new ArrayList<Store>();
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject response = responseObject.getJSONObject("response");

					if (response.has("stores") && response.get("stores") instanceof JSONArray) {
						JSONArray storesArr = response.getJSONArray("stores");

						for (int i = 0; i < storesArr.length(); i++) {
							Store store = null;
							JSONObject storeJson = storesArr.getJSONObject(i);
							if (storeJson.has("store") && storeJson.get("store") instanceof JSONObject) {
								store = parse_Store(storeJson.getJSONObject("store"));
								object.add(store);
							}
						}
					}
				}
			}
		}
		return object;
	}

	public static StoreCatalogueItem parse_CurrentDiscount(InputStream is) throws JSONException {

		StoreCatalogueItem object = new StoreCatalogueItem();
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject response = responseObject.getJSONObject("response");
					if (response.has("store") && response.get("store") instanceof JSONObject) {
						object = parse_StoreCatalogueItem(response.getJSONObject("store"));
					}
				}
			}
		}
		return object;
	}

	public static User parse_User(JSONObject userObject) {
		if (userObject == null)
			return null;

		User returnUser = new User();
		try {
			if (userObject.has("user_id") && userObject.get("user_id") instanceof Integer) {
				returnUser.setUserId(userObject.getInt("user_id"));
			}

			if (userObject.has("email")) {
				returnUser.setEmail(String.valueOf(userObject.get("email")));
			}

			if (userObject.has("profile_pic")) {
				returnUser.setImageUrl(String.valueOf(userObject.get("profile_pic")));
			}

			if (userObject.has("user_name")) {
				returnUser.setUserName(String.valueOf(userObject.get("user_name")));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnUser;
	}

	public static Merchant parse_Merchant(JSONObject userObject) {
		if (userObject == null)
			return null;

		Merchant returnUser = new Merchant();
		try {
			if (userObject.has("merchant_id") && userObject.get("merchant_id") instanceof Integer) {
				returnUser.setMerchantId(userObject.getInt("merchant_id"));
			}

			if (userObject.has("email")) {
				returnUser.setEmail(String.valueOf(userObject.get("email")));
			}

			if (userObject.has("merchant_name")) {
				returnUser.setMerchantName(String.valueOf(userObject.get("merchant_name")));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnUser;
	}

	public static Store parse_Store(JSONObject userObject) {
		if (userObject == null)
			return null;

		Store returnUser = new Store();
		try {
			if (userObject.has("storeId") && userObject.get("storeId") instanceof Integer) {
				returnUser.setStoreId(userObject.getInt("storeId"));
			}

			if (userObject.has("storeType") && userObject.get("storeType") instanceof Integer) {
				returnUser.setStoreType(userObject.getInt("storeType"));
			}

			if (userObject.has("address")) {
				returnUser.setAddress(String.valueOf(userObject.get("address")));
			}

			if (userObject.has("storeName")) {
				returnUser.setStoreName(String.valueOf(userObject.get("storeName")));
			}

			if (userObject.has("contactNumber")) {
				returnUser.setContactNumber(String.valueOf(userObject.get("contactNumber")));
			}

			if (userObject.has("latitude") && userObject.get("latitude") instanceof Double) {
				returnUser.setLatitude(userObject.getDouble("latitude"));
			}

			if (userObject.has("longitude") && userObject.get("longitude") instanceof Double) {
				returnUser.setLongitude(userObject.getDouble("longitude"));
			}

			if (userObject.has("availability") && userObject.get("availability") instanceof Integer) {
				returnUser.setAvailability(userObject.getInt("availability"));
			}

			if (userObject.has("max_occupancy") && userObject.get("max_occupancy") instanceof Integer) {
				returnUser.setMaxOccupancy(userObject.getInt("max_occupancy"));
			}

			if (userObject.has("current_occupancy") && userObject.get("current_occupancy") instanceof Integer) {
				returnUser.setCurrentOccupancy(userObject.getInt("current_occupancy"));
			}
			
			if (userObject.has("accepts_reservation") && userObject.get("accepts_reservation") instanceof Boolean) {
				returnUser.setAcceptsReservation(userObject.getBoolean("accepts_reservation"));
			} else if (userObject.has("accepts_reservation") && userObject.get("accepts_reservation") instanceof Integer) {
				returnUser.setAcceptsReservation(userObject.getInt("accepts_reservation") == 1);
			}
			
			if (userObject.has("storeItem") && userObject.get("storeItem") instanceof JSONObject) {
				JSONObject categoryJson = userObject.getJSONObject("storeItem");
				returnUser.setStoreItem(parse_StoreCatalogueItem(categoryJson));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnUser;
	}

	public static StoreCatalogueItem parse_StoreCatalogueItem(JSONObject categoryJson) {

		if (categoryJson == null)
			return null;

		StoreCatalogueItem storeItem = new StoreCatalogueItem();
		try {
			if (categoryJson.has("store") && categoryJson.get("store") instanceof JSONObject)
				categoryJson = categoryJson.getJSONObject("store");

			if (categoryJson.has("storeId") && categoryJson.get("storeId") instanceof Integer)
				storeItem.setStoreId(categoryJson.getInt("storeId"));

			if (categoryJson.has("storeItemId") && categoryJson.get("storeItemId") instanceof Integer)
				storeItem.setStoreItemId(categoryJson.getInt("storeItemId"));

			if (categoryJson.has("dealType") && categoryJson.get("dealType") instanceof Integer)
				storeItem.setDealType(categoryJson.getInt("dealType"));
			if (categoryJson.has("dealSubType") && categoryJson.get("dealSubType") instanceof Integer)
				storeItem.setDealSubType(categoryJson.getInt("dealSubType"));
			if (categoryJson.has("discountAmount") && categoryJson.get("discountAmount") instanceof Integer)
				storeItem.setDiscountAmount(categoryJson.getInt("discountAmount"));
			if (categoryJson.has("minOrder") && categoryJson.get("minOrder") instanceof Integer)
				storeItem.setMinOrder(categoryJson.getInt("minOrder"));
			if (categoryJson.has("maxOrder") && categoryJson.get("maxOrder") instanceof Integer)
				storeItem.setMaxOrder(categoryJson.getInt("maxOrder"));
			if (categoryJson.has("count") && categoryJson.get("count") instanceof Integer)
				storeItem.setCount(categoryJson.getInt("count"));

			if (categoryJson.has("productName"))
				storeItem.setProductName(String.valueOf(categoryJson.get("productName")));

			if (categoryJson.has("mon") && categoryJson.get("mon") instanceof Boolean)
				storeItem.setMon(categoryJson.getBoolean("mon"));
			else if (categoryJson.has("mon") && categoryJson.get("mon") instanceof Integer)
				storeItem.setMon(categoryJson.getInt("mon") == 1);

			if (categoryJson.has("tue") && categoryJson.get("tue") instanceof Boolean)
				storeItem.setTue(categoryJson.getBoolean("tue"));
			else if (categoryJson.has("tue") && categoryJson.get("tue") instanceof Integer)
				storeItem.setTue(categoryJson.getInt("tue") == 1);

			if (categoryJson.has("wed") && categoryJson.get("wed") instanceof Boolean)
				storeItem.setWed(categoryJson.getBoolean("wed"));
			else if (categoryJson.has("wed") && categoryJson.get("wed") instanceof Integer)
				storeItem.setWed(categoryJson.getInt("wed") == 1);

			if (categoryJson.has("thu") && categoryJson.get("thu") instanceof Boolean)
				storeItem.setThu(categoryJson.getBoolean("thu"));
			else if (categoryJson.has("thu") && categoryJson.get("thu") instanceof Integer)
				storeItem.setThu(categoryJson.getInt("thu") == 1);

			if (categoryJson.has("fri") && categoryJson.get("fri") instanceof Boolean)
				storeItem.setFri(categoryJson.getBoolean("fri"));
			else if (categoryJson.has("fri") && categoryJson.get("fri") instanceof Integer)
				storeItem.setFri(categoryJson.getInt("fri") == 1);

			if (categoryJson.has("sat") && categoryJson.get("sat") instanceof Boolean)
				storeItem.setSat(categoryJson.getBoolean("sat"));
			else if (categoryJson.has("sat") && categoryJson.get("sat") instanceof Integer)
				storeItem.setSat(categoryJson.getInt("sat") == 1);

			if (categoryJson.has("sun") && categoryJson.get("sun") instanceof Boolean)
				storeItem.setSun(categoryJson.getBoolean("sun"));
			else if (categoryJson.has("sun") && categoryJson.get("sun") instanceof Integer)
				storeItem.setSun(categoryJson.getInt("sun") == 1);

			if (categoryJson.has("start_hour") && categoryJson.get("start_hour") instanceof Integer)
				storeItem.setStartingHour(categoryJson.getInt("start_hour"));

			if (categoryJson.has("start_min") && categoryJson.get("start_min") instanceof Integer)
				storeItem.setStartingMin(categoryJson.getInt("start_min"));

			if (categoryJson.has("end_hour") && categoryJson.get("end_hour") instanceof Integer)
				storeItem.setEndingHour(categoryJson.getInt("end_hour"));

			if (categoryJson.has("end_min") && categoryJson.get("end_min") instanceof Integer)
				storeItem.setEndingMin(categoryJson.getInt("end_min"));

			if (categoryJson.has("end_time") && categoryJson.get("end_time") instanceof Long)
				storeItem.setEndTime(categoryJson.getLong("end_time"));
			else if (categoryJson.has("end_time") && categoryJson.get("end_time") instanceof Integer)
				storeItem.setEndTime(categoryJson.getInt("end_time"));

			if (categoryJson.has("discountSecondAmount") && categoryJson.get("discountSecondAmount") instanceof Integer)
				storeItem.setDiscountSecondAmount(categoryJson.getInt("discountSecondAmount"));

			if (categoryJson.has("status") && categoryJson.get("status") instanceof Integer)
				storeItem.setStatus(categoryJson.getInt("status"));

			if (categoryJson.has("discountAmountMon") && categoryJson.get("discountAmountMon") instanceof Integer)
				storeItem.setDiscountAmountMon(categoryJson.getInt("discountAmountMon"));

			if (categoryJson.has("discountAmountTue") && categoryJson.get("discountAmountTue") instanceof Integer)
				storeItem.setDiscountAmountTue(categoryJson.getInt("discountAmountTue"));

			if (categoryJson.has("discountAmountWed") && categoryJson.get("discountAmountWed") instanceof Integer)
				storeItem.setDiscountAmountWed(categoryJson.getInt("discountAmountWed"));

			if (categoryJson.has("discountAmountThu") && categoryJson.get("discountAmountThu") instanceof Integer)
				storeItem.setDiscountAmountThu(categoryJson.getInt("discountAmountThu"));

			if (categoryJson.has("discountAmountFri") && categoryJson.get("discountAmountFri") instanceof Integer)
				storeItem.setDiscountAmountFri(categoryJson.getInt("discountAmountFri"));

			if (categoryJson.has("discountAmountSat") && categoryJson.get("discountAmountSat") instanceof Integer)
				storeItem.setDiscountAmountSat(categoryJson.getInt("discountAmountSat"));

			if (categoryJson.has("discountAmountSun") && categoryJson.get("discountAmountSun") instanceof Integer)
				storeItem.setDiscountAmountSun(categoryJson.getInt("discountAmountSun"));

			if (categoryJson.has("productNameMon"))
				storeItem.setProductNameMon(String.valueOf(categoryJson.get("productNameMon")));

			if (categoryJson.has("productNameTue"))
				storeItem.setProductNameTue(String.valueOf(categoryJson.get("productNameTue")));

			if (categoryJson.has("productNameWed"))
				storeItem.setProductNameWed(String.valueOf(categoryJson.get("productNameWed")));

			if (categoryJson.has("productNameThu"))
				storeItem.setProductNameThu(String.valueOf(categoryJson.get("productNameThu")));

			if (categoryJson.has("productNameFri"))
				storeItem.setProductNameFri(String.valueOf(categoryJson.get("productNameFri")));

			if (categoryJson.has("productNameSat"))
				storeItem.setProductNameSat(String.valueOf(categoryJson.get("productNameSat")));

			if (categoryJson.has("productNameSun"))
				storeItem.setProductNameSun(String.valueOf(categoryJson.get("productNameSun")));

			if (categoryJson.has("hasProductDeal") && categoryJson.get("hasProductDeal") instanceof Boolean)
				storeItem.setHasProductDeal(categoryJson.getBoolean("hasProductDeal"));
			else if (categoryJson.has("hasProductDeal") && categoryJson.get("hasProductDeal") instanceof Integer)
				storeItem.setHasProductDeal(categoryJson.getInt("hasProductDeal") == 1);

			if (categoryJson.has("max_discount") && categoryJson.get("max_discount") instanceof Integer)
				storeItem.setMaximumDiscount(categoryJson.getInt("max_discount"));
			
			if (categoryJson.has("action_mode") && categoryJson.get("action_mode") instanceof Integer)
				storeItem.setActionMode(categoryJson.getInt("action_mode"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return storeItem;
	}

	public static UserWish parse_UserWish(JSONObject userObject) {
		if (userObject == null)
			return null;

		UserWish returnUser = new UserWish();
		try {
			if (userObject.has("key")) {
				returnUser.setUserKey(String.valueOf(userObject.get("key")));
			}

			if (userObject.has("pin")) {
				returnUser.setPin(String.valueOf(userObject.get("pin")));
			}

			if (userObject.has("bill_amount") && userObject.get("bill_amount") instanceof Double) {
				returnUser.setBillAmount(userObject.getDouble("bill_amount"));
			} else if (userObject.has("bill_amount") && userObject.get("bill_amount") instanceof Integer) {
				returnUser.setBillAmount(userObject.getInt("bill_amount"));
			}

			if (userObject.has("user_id") && userObject.get("user_id") instanceof Integer) {
				returnUser.setUserId(userObject.getInt("user_id"));
			}

			if (userObject.has("store_id") && userObject.get("store_id") instanceof Integer) {
				returnUser.setStoreItemId(userObject.getInt("store_id"));
			}

			if (userObject.has("user_wish_id") && userObject.get("user_wish_id") instanceof Integer) {
				returnUser.setUserWishId(userObject.getInt("user_wish_id"));
			}

			if (userObject.has("store_id") && userObject.get("store_id") instanceof Integer) {
				returnUser.setStoreItemId(userObject.getInt("store_id"));
			}

			if (userObject.has("store_id") && userObject.get("store_id") instanceof Integer) {
				returnUser.setStoreId(userObject.getInt("store_id"));
			}

			if (userObject.has("crowd") && userObject.get("crowd") instanceof Integer) {
				returnUser.setCrowd(userObject.getInt("crowd"));
			}

			if (userObject.has("start_date") && userObject.get("start_date") instanceof Long) {
				returnUser.setStartDate(userObject.getInt("start_date"));
			}

			if (userObject.has("end_date") && userObject.get("end_date") instanceof Long) {
				returnUser.setEndDate(userObject.getInt("end_date"));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return returnUser;
	}

	public static Object[] parse_BookingList(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		Object[] objects = new Object[2];
		int size = 0;
		ArrayList<Object> returnObjects = new ArrayList<Object>();

		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {

					JSONObject categoriesObject = responseObject.getJSONObject("response");

					if (categoriesObject.has("total") && categoriesObject.get("total") instanceof Integer)
						size = categoriesObject.getInt("total");

					if (categoriesObject.has("bookings") && categoriesObject.get("bookings") instanceof JSONArray) {
						JSONArray categoriesArr = categoriesObject.getJSONArray("bookings");

						for (int i = 0; i < categoriesArr.length(); i++) {

							JSONObject categoryJson = categoriesArr.getJSONObject(i);
							UserWish userWish = null;
							if (categoryJson.has("booking") && categoryJson.get("booking") instanceof JSONObject) {
								userWish = parse_UserWish(categoryJson.getJSONObject("booking"));
							}
							
							if (userWish != null && categoryJson.has("user") && categoryJson.get("user") instanceof JSONObject
									&& categoryJson.getJSONObject("user").has("user")
									&& categoryJson.getJSONObject("user") instanceof JSONObject) {
								userWish.setUser(parse_User(categoryJson.getJSONObject("user").getJSONObject("user")));
							}

							returnObjects.add(userWish);
						}
					}

				}
			}
		}
		objects[0] = size;
		objects[1] = returnObjects;
		return objects;
	}

}
