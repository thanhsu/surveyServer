package com.survey.baokim;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Hashtable;

import io.vertx.core.json.JsonObject;

public class BaoKimConnection {
	// URL checkout của baokim.vn
	private String baokim_url = "https://www.baokim.vn/payment/customize_payment/order";

	// Mã merchante site
	private String merchant_id = "100001"; // Biến này được baokim.vn cung cấp khi bạn đăng ký merchant site

	// Mật khẩu bảo mật
	private String secure_pass = "DED01D1CFF3BE2767196FF0080F6DB6D5C"; // Biến này được baokim.vn cung cấp khi bạn đăng
																		// ký merchant site
	public static final int MD5 = 1;
	public static final int SHA1 = 2;
	private static String algorithmName = "MD5";

	/**
	 * Hàm thực hiện việc mã hóa, tạo khóa trên đường dẫn
	 * 
	 * @param messages
	 *            xâu gốc
	 * @return kết quả mã hóa
	 * @throws Exception
	 */

	private static BaoKimConnection instance = null;

	public static BaoKimConnection getInstance() {
		if (instance == null) {
			synchronized (BaoKimConnection.class) {
				instance = new BaoKimConnection();
			}
		}
		return instance;
	}

	public void init(JsonObject config) {
		baokim_url = config.getString("BaoKim_URL");
		merchant_id = config.getString("BaoKim_MerchantID");
		secure_pass = config.getString("BaoKim_SecurePass");
	}

	public static String getMD5Hash(String messages) throws Exception {
		if (messages == null)
			throw new NullPointerException();

		StringBuffer result = null;
		byte[] data = messages.getBytes();
		try {
			MessageDigest md = MessageDigest.getInstance(algorithmName);
			md.reset();
			md.update(data);
			byte[] msgDigest = md.digest();
			result = new StringBuffer();
			for (int i = 0; i < msgDigest.length; i++) {
				String hex = Integer.toHexString(0xff & msgDigest[i]);
				if (hex.length() == 1)
					result.append('0');
				result.append(hex);
			}
		} catch (NoSuchAlgorithmException ex) {
			throw new Exception(ex.toString());
		}
		return result.toString();
	}

	/**
	 * Hàm xây dựng url chuyển đến BaoKim.vn thực hiện thanh toán, trong đó có tham
	 * số mã hóa (còn gọi là public key)
	 * 
	 * @param $order_id
	 *            Mã đơn hàng
	 * @param $business
	 *            Email tài khoản người bán
	 * @param $total_amount
	 *            Giá trị đơn hàng
	 * @param $shipping_fee
	 *            Phí vận chuyển
	 * @param $tax_fee
	 *            Thuế
	 * @param $order_description
	 *            Mô tả đơn hàng
	 * @param $url_success
	 *            Url trả về khi thanh toán thành công
	 * @param $url_cancel
	 *            Url trả về khi hủy thanh toán
	 * @param $url_detail
	 *            Url chi tiết đơn hàng
	 * @return url cần tạo
	 */
	public String createRequestUrl(String order_id, String business, String total_amount, String shipping_fee,
			String tax_fee, String order_description, String url_success, String url_cancel, String url_detail) {
		Hashtable<String, String> params = new Hashtable<>();
		params.put("merchant_id", this.merchant_id);
		params.put("order_id", order_id);
		params.put("business", business);
		params.put("total_amount", total_amount);
		params.put("shipping_fee", shipping_fee);
		params.put("tax_fee", tax_fee);
		params.put("order_description", order_description);
		params.put("url_success", url_success);
		params.put("url_cancel", url_cancel);
		params.put("url_detail", url_detail);

		// Sap xep mang thuoc tinh don hang theo key
		String[] keys = (String[]) params.keySet().toArray(new String[0]);
		Arrays.sort(keys);

		// Tao public key
		String str_combined = this.secure_pass;
		for (String key : keys) {
			str_combined += (String) params.get(key);
		}
		// Thực hiện mã hóa tạo checksum
		String checksum = "";
		try {
			checksum = this.getMD5Hash(str_combined).toUpperCase();
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}

		// Kiểm tra url_success xem có '?' không, nếu chưa có, bổ sung vào
		String redirect_url = baokim_url;
		if (redirect_url.indexOf("?") == -1) {
			redirect_url += "?";
		} else if (redirect_url.substring(redirect_url.length() - 1, redirect_url.length()) == "?"
				&& redirect_url.indexOf("&") == -1) {
			// Nếu url_success có '?' nhưng không kết thúc bằng '?' và chưa có dấu '&' thì
			// bổ sung vào cuối
			redirect_url += "&";
		}

		// Tạo url, phần chứa các tham số
		String url_params = "";
		for (String key : keys) {
			String val = (String) params.get(key);
			if (url_params == "") {
				url_params += key + "=" + java.net.URLEncoder.encode(val);
			} else {
				url_params += "&" + key + "=" + java.net.URLEncoder.encode(val);
			}
		}
		url_params += "&checksum=" + checksum;

		return redirect_url + url_params;
	}

	/**
	 * Hàm thực hiện xác minh tính chính xác thông tin trả về từ BaoKim.vn
	 * 
	 * @param $_GET
	 *            chứa tham số trả về trên url
	 * @return true nếu thông tin là chính xác, false nếu thông tin không chính xác
	 */
	public Boolean verifyResponseUrl(Hashtable get_params) {
		// Sắp xếp mảng thông tin trả về theo key để mã hóa
		String[] keys = (String[]) get_params.keySet().toArray(new String[0]);
		Arrays.sort(keys);

		// Thực hiện mã hóa dữ liệu trả về để kiểm tra
		String str_combined = this.secure_pass;
		String checksum = "";
		for (String key : keys) {
			if (!(key.equals("checksum") || key == "checksum"))
				str_combined += get_params.get(key);
			else
				checksum = get_params.get(key).toString();
		}

		String verify_checksum = "";
		try {
			verify_checksum = this.getMD5Hash(str_combined);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		// Kiểm tra kết quả mã hóa thu được xem có trùng với mã trả về từ BaoKim.vn
		// không
		if (verify_checksum == checksum || verify_checksum.equalsIgnoreCase(checksum)) {
			return true;
		}
		return false;
	}
}
