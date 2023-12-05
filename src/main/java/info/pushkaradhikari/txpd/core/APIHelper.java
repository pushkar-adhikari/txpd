package info.pushkaradhikari.txpd.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import info.pushkaradhikari.txpd.core.business.error.ErrorMessage;
import info.pushkaradhikari.txpd.core.business.error.exception.UnexpectedSituationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class APIHelper {

	private APIHelper() {
		throw new IllegalStateException("Utility Class");
	}

	private static final HostnameVerifier defaultVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
	private static final SSLSocketFactory defaultFactory = HttpsURLConnection.getDefaultSSLSocketFactory();

	public static String fetchFromAPI(String apiUrl) throws Exception {
		try {
			HttpsURLConnection urlConn = setUpConnection(apiUrl);
			String response = fetchResponse(urlConn);
			urlConn.disconnect();
			reset();
			return response;
		} catch (Exception e) {
			reset();
			throw e;
		}
	}

	private static HttpsURLConnection setUpConnection(String apiUrl) throws Exception {
		setSocketFactory();
		setAllHostnameVerifier();
		URL url = new URL(apiUrl);
		HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
		urlConn.setRequestMethod("GET");
		urlConn.setRequestProperty("Accept", "application/json;charset=UTF8");
		urlConn.setRequestProperty("Cache-Control", "no-cache");
		urlConn.connect();
		return urlConn;
	}

	private static String fetchResponse(HttpsURLConnection urlConn) throws Exception {
		int code = urlConn.getResponseCode();
		if (code > 400) {
			throw new UnexpectedSituationException(new ErrorMessage(code, urlConn.getResponseMessage()));
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		int BUFFER_SIZE = 1024;
		char[] buffer = new char[BUFFER_SIZE]; // or some other size,
		int charsRead = 0;
		try {
			while ((charsRead = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
				sb.append(buffer, 0, charsRead);
			}
			in.close();
			return sb.toString();
		} catch (Exception e) {
			in.close();
			throw e;
		}
	}

	private static void setSocketFactory() {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, getAllTrustedManager(), new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static TrustManager[] getAllTrustedManager() {
		return new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
	}

	private static void setAllHostnameVerifier() {
		HostnameVerifier hv = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	private static void reset() {
		HttpsURLConnection.setDefaultSSLSocketFactory(defaultFactory);
		HttpsURLConnection.setDefaultHostnameVerifier(defaultVerifier);
	}

}
