package cotg.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class GetData {

	public static String gR = "https://w9.crownofthegods.com/includes/gR.php?a=0";
	public static String gPi = "https://w9.crownofthegods.com/includes/gPi.php";
	
	
	public static void sendPostToGetCityData() throws Exception {
		String rawData = "a=Leafer";
		String type = "application/x-www-form-urlencoded";
		String encodedData = URLEncoder.encode( rawData, "UTF-8" ); 
		URL u = new URL(gPi);
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty( "Content-Type", type );
		conn.setRequestProperty( "Content-Length", String.valueOf(encodedData.length()));
		OutputStream os = conn.getOutputStream();
		os.write(encodedData.getBytes());
		System.out.println(conn.getResponseCode());
	}
	
	public static void getClient () {
		try {

			URL url = new URL(gR);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.addRequestProperty("Cookie", "remember_me=647ceb90e3; sec_session_id=hc0f4rr3rqs63ftvpnk021pop1; _ga=GA1.2.2113084975.1511238429; _gid=GA1.2.1512252645.1511238429");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}

			conn.disconnect();

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		  }
	}
	
	public static void postClient() {
		try {

			URL url = new URL(gR);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.addRequestProperty("Cookie", "remember_me=647ceb90e3; sec_session_id=hc0f4rr3rqs63ftvpnk021pop1; _ga=GA1.2.2113084975.1511238429; _gid=GA1.2.1512252645.1511238429");

			String input = "{a=0}";

			OutputStream os = conn.getOutputStream();
			os.write(input.getBytes());
			os.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;
			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {
				System.out.println(output);
			}
			
			System.out.println(conn);

			conn.disconnect();

		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		 }
	}
	
	public static void main(String[] args) {
		getClient();
	}
}
