package org.fl.restApiSkeleton.security;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class HMACUtils {

	// could be retrieve from DB or config file
	// to avoid having to edit the code in case of a secretkey change
	private final static String SECRET_KEY = "mFiqTjGJibMvkFD5jkr9bqMteqbjD0u4OavnwZCJnrHlXdjUbUu6ZAKGlXaaTXg" ;
	private final static String CLIENT_ID  = "REST_API_SKELETON_1.0.0" ;
	private final static String ALGORITHM  = "HmacSHA256" ;
	private final static String ALGORITHM_FOR_ADMIN = "HmacSHA256" ;
	
	// if ALGORITHM is null, the checking will always be true (no security)
//	private final static String ALGORITHM  = null ;
	private final static String CLIENT_ID_FOR_ADMIN  = "ADMIN_REST_API_SKELETON_1.0.0" ;
	
	private final static byte[] SECRET_KEY_ASBYTES = SECRET_KEY.getBytes() ;
	
	private final static String REQUEST         = "requestMethod=" ;
	private final static String URI				= "&uriPath=" ;
	private final static String CLIENT 			= "&clientId=" ;
	private final static String DEVICE 			= "&deviceId=" ;
	private final static String TIMESTAMP 		= "&timestamp=" ;
	
	private static int STRING_TO_HASH_MAX_SIZE = 256 ;
	
	private static Base64.Encoder base64Encoder = Base64.getEncoder() ;
	
	// Check the HMAC string 
	public static boolean checkHmac(String hmacReceivedString, String method, String path, String deviceId, String timestamp, Logger hLog) {
		
			return checkHmac(hmacReceivedString, method, path, deviceId, timestamp, CLIENT_ID, ALGORITHM, hLog) ;
	}
	
	// Check the HMAC string for admin function
	public static boolean checkHmacAdmin(String hmacReceivedString, String method, String path, String deviceId, String timestamp, Logger hLog) {
		
			return checkHmac(hmacReceivedString, method, path, deviceId, timestamp, CLIENT_ID_FOR_ADMIN, ALGORITHM_FOR_ADMIN, hLog) ;
	}
	
	// Check the HMAC string for admin function
	private static boolean checkHmac(String hmacReceivedString, String method, String path, String deviceId, String timestamp, String clientId, String algorithm, Logger hLog) {
		
			// Build the HMAC string to sign
			StringBuilder strToSign = new StringBuilder(STRING_TO_HASH_MAX_SIZE) ;
			strToSign.append(REQUEST).append(method) ;
			strToSign.append(URI).append(path);
			strToSign.append(CLIENT).append(clientId);
			strToSign.append(DEVICE).append(deviceId);
			strToSign.append(TIMESTAMP).append(timestamp);
			
			// sign the string
			String hmacBuiltString = sha256hash(strToSign.toString(), algorithm, hLog) ;
			
			// Check if HMAC are equals - return the result
			return hmacBuiltString.equals(hmacReceivedString);
	}
	
	// Sign a string with the secret key and the algorithm
	private static String sha256hash(String strToHash, String algorithm, Logger hLog) {

	    try {
	        Mac sha256_HMAC = Mac.getInstance(algorithm);
	        SecretKeySpec secret_key = new SecretKeySpec(SECRET_KEY_ASBYTES, algorithm);
	        sha256_HMAC.init(secret_key);

	        String hash = base64Encoder.encodeToString(sha256_HMAC.doFinal(strToHash.getBytes()));
	        hLog.finest(() -> "String to hash=" + strToHash + "\n hash result=" + hash);
	        return hash ;
	    } catch (Exception e){
	    	hLog.log(Level.SEVERE, "Exception when hashing the string " + strToHash , e);
	    	return "" ;
	    }
	}
}
