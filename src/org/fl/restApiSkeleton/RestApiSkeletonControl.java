package org.fl.restApiSkeleton;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import com.google.gson.JsonObject;
import org.fl.util.AdvancedProperties;
import org.fl.util.ExecutionDurations;
import org.fl.util.RunningContext;

public class RestApiSkeletonControl {

	private static final String DEFAULT_PROP_FILE = "restApiSkeleton.properties";
	
	private static boolean initDone = false ;
	
	private static Logger logger ;
	private static AdvancedProperties properties ;
	
	// Charset returned by the REST API
	// This will materialize inside the "content-type" http header
	private static Charset responseCharsetSet ;
	private static String  responseContentType ;
	
	// Running context (log and properties)
	private static RunningContext runningContext ;

	//	# logging level that trigger 
	//	# - putting duration json property in the API return
	//	# - putting additionnal information in the API return 
	//	#   (number of listings, number of line items returned, smart engine return code...)
	private static Level additionalInfosAndLoggingLogLevel ;
	
	// Application initialization : this must be done before doing anything else
	public synchronized static void init(ExecutionDurations execDuration) {
		
		if (! initDone) {
	
			// Get a logger and the properties		
			runningContext = new RunningContext("Api skeleton", null, DEFAULT_PROP_FILE);
			logger 		   = runningContext.getpLog() ;
			properties 	   = runningContext.getProps() ;
	
			//==========================================================
			// Init parameters from property file

			// Charset returned by the REST API
			// This will materialize inside the "content-type" http header
			String respCharsetString  = properties.getProperty("skeletonRestApi.charSetReturned", "UTF-8") ;
			responseContentType = MediaType.APPLICATION_JSON  + "; charset=" + respCharsetString ;

			try {
				responseCharsetSet  = Charset.forName(respCharsetString) ;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception when getting charset for responses. Chartset default to UTF-8", e);
				responseCharsetSet	= StandardCharsets.UTF_8 ;
			}
			
			additionalInfosAndLoggingLogLevel = properties.getLevel("skeletonRestApi.additionnalReturnAndLogging.level", Level.FINEST) ;

			logger.info("Rest skeleton API initialization done");
			
			initDone = true ;
		}
	}

	public static boolean isInitDone() {
		return initDone;
	}

	public static Charset getResponseCharsetSet() {
		return responseCharsetSet;
	}

	public static String getResponseContentType() {
		return responseContentType;
	}

	public static Logger getLogger() {
		return logger;
	}
	
	public static Level getAdditionalInfosAndLoggingLogLevel() {
		return additionalInfosAndLoggingLogLevel;
	}

	public static RunningContext getRunningContext() {
		return runningContext ;
	}
	
	public static boolean setLogsLevel(JsonObject levelsJson) {
		return runningContext.setLogsLevel(levelsJson);
	}
	
	public static JsonObject getLogsLevels() {
		return runningContext.getLogsLevel();
	}
	
}
