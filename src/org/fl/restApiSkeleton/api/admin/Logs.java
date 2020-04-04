package org.fl.restApiSkeleton.api.admin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fl.restApiSkeleton.security.HMACUtils;
import org.fl.restApiSkeleton.RestApiSkeletonControl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ibm.lge.fl.util.CompressionUtils;
import com.ibm.lge.fl.util.ExecutionDurations;
import com.ibm.lge.fl.util.api.ApiErrorCodeBuilder;
import com.ibm.lge.fl.util.api.ApiReturn;
import com.ibm.lge.fl.util.json.JsonUtils;

@Path("/v1/admin/logs")
public class Logs  {

	private final static String GET_LOGS 		= "GET /admin/logs " ;
	private final static String DELETE_LOGS 	= "DELETE /admin/logs " ;
	private final static String PUT_LOGS 		= "PUT /admin/logs " ;
	private final static String GET_LOG_LEVELS 	= "GET /admin/logs/levels " ;
	
	// property name for execution duration
	private final static String appProp = "apiProcess" ;
	
	public Logs() {
		// MUST BE EMPTY
		// Putting "SkylineControl.init(); " makes an invocation exception in bluemix (and not on local liberty)	
	}

	// Get log
	@GET
	public Response get(@HeaderParam("Authorization") String authorizationHeader,
			 			@HeaderParam("Device-Id") String deviceId,
			 			@HeaderParam("Timestamp") String timestamp,
			 			@DefaultValue("false")
			 			@QueryParam("compressReturn") boolean compressReturn, 
			 			@Context UriInfo uriInfo) {
		// Get latest logs of the application 
		
		ExecutionDurations execDuration = new ExecutionDurations(appProp) ;		
		if (! RestApiSkeletonControl.isInitDone()) {
			RestApiSkeletonControl.init(execDuration);
		}
		
		Logger gLog = RestApiSkeletonControl.getLogger() ;
		execDuration.setTriggerLevel(gLog, RestApiSkeletonControl.getAdditionalInfosAndLoggingLogLevel());
		ApiReturn apiReturn = new ApiReturn(execDuration, RestApiSkeletonControl.getResponseCharsetSet(), gLog) ;
		
		try {
			StringBuilder logsContent ;
			if (HMACUtils.checkHmacAdmin(authorizationHeader, "GET", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
			// Check Hmac - successful
	

				logsContent = RestApiSkeletonControl.getRunningContext().getMemoryLogs() ;
				if (logsContent == null) {
					logsContent = new StringBuilder("Logging not enabled") ;
				}
				
				apiReturn.setDataReturn(new JsonPrimitive(logsContent.toString())) ;
			} else {
				// Hmac checking has failed
				apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, GET_LOGS) ;
			}
		} catch (Exception e) {
			gLog.log(Level.SEVERE, "Exception in API " + GET_LOGS, e) ;
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.GENERAL_EXCEPTION_CODE, GET_LOGS + ": " + e.toString()) ;
		}

		if (compressReturn) {
			byte[] logReturn = apiReturn.getCompressedApiReturn(GET_LOGS, CompressionUtils.SupportedCompression.GZIP) ;
			if (! apiReturn.isOnError()) {
				return Response.ok(logReturn, "application/zip").encoding("gzip").build() ;
			}		
		} 
		
		String logReturn = apiReturn.getApiReturnJson(GET_LOGS) ;			
		return Response.ok(logReturn, RestApiSkeletonControl.getResponseContentType()).build() ;
				
	}
	
	// Delete log
	@DELETE
	public Response delete(@HeaderParam("Authorization") String authorizationHeader,
			 			   @HeaderParam("Device-Id") String deviceId,
			 			   @HeaderParam("Timestamp") String timestamp,
			 			   @Context UriInfo uriInfo) {
		
		ApiReturn apiReturn = deleteResize(	authorizationHeader,
											deviceId,
											timestamp,
											uriInfo,
											0) ;
		
		return Response.ok(apiReturn.getApiReturnJson(DELETE_LOGS), RestApiSkeletonControl.getResponseContentType()).build() ;
	}

	// Delete log and resize the memory buffers
	@DELETE @Path("/{newSize}")
	public Response deleteAndResize(@HeaderParam("Authorization") String authorizationHeader,
			 			   			@HeaderParam("Device-Id")	  String deviceId,
			 			   			@HeaderParam("Timestamp") 	  String timestamp,
			 			   			@PathParam("newSize") 		  int 	 newSize, 
			 			   			@Context 					  UriInfo uriInfo) {
		
		ApiReturn apiReturn = deleteResize(	authorizationHeader,
											deviceId,
											timestamp,
											uriInfo,
											newSize) ;
		
		return Response.ok(apiReturn.getApiReturnJson(DELETE_LOGS), RestApiSkeletonControl.getResponseContentType()).build() ;
	}
	
	private ApiReturn deleteResize(	String authorizationHeader,
								String deviceId,
								String timestamp,
								UriInfo uriInfo,
								int newSize) {
		
		ExecutionDurations execDuration = new ExecutionDurations(appProp) ;
		if (! RestApiSkeletonControl.isInitDone()) {
			RestApiSkeletonControl.init(execDuration);
		}

		Logger gLog = RestApiSkeletonControl.getLogger() ;
		execDuration.setTriggerLevel(gLog, RestApiSkeletonControl.getAdditionalInfosAndLoggingLogLevel());
		ApiReturn apiReturn = new ApiReturn(execDuration, RestApiSkeletonControl.getResponseCharsetSet(), gLog) ;
		
		try {
			if (HMACUtils.checkHmacAdmin(authorizationHeader, "DELETE", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
			// Check Hmac - successful
	
				StringBuilder resp = new StringBuilder() ;
				
				// delete and eventually resize in-memory logs
				if (newSize == 0) {
					// no resize requested
					resp.append(RestApiSkeletonControl.getRunningContext().deleteMemoryLogs()) ;
				} else {
					// resize requested
					resp.append(RestApiSkeletonControl.getRunningContext().deleteAndResizeMemoryLogs(newSize)) ;
				}
				
				apiReturn.setDataReturn(new JsonPrimitive(resp.toString())) ;
			} else {
				// Hmac checking has failed
				apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, DELETE_LOGS) ;
			}
		} catch (Exception e) {
			gLog.log(Level.SEVERE, "Exception in API " + DELETE_LOGS, e) ;
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.GENERAL_EXCEPTION_CODE, DELETE_LOGS + ": " + e.toString()) ;
		}

		return apiReturn ;
	}
	
	// Change log level
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response put(@HeaderParam("Authorization") String authorizationHeader,
			   @HeaderParam("Device-Id") String deviceId,
			   @HeaderParam("Timestamp") String timestamp,
			   InputStream logLevelStream,
			   @Context UriInfo uriInfo) {
	
		ExecutionDurations execDuration = new ExecutionDurations(appProp) ;
		if (! RestApiSkeletonControl.isInitDone()) {
			RestApiSkeletonControl.init(execDuration);
		}

		Logger gLog = RestApiSkeletonControl.getLogger() ;
		execDuration.setTriggerLevel(gLog, RestApiSkeletonControl.getAdditionalInfosAndLoggingLogLevel());
		ApiReturn apiReturn = new ApiReturn(execDuration, RestApiSkeletonControl.getResponseCharsetSet(), gLog) ;
		
		try {
			if (HMACUtils.checkHmacAdmin(authorizationHeader, "PUT", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
			// Check Hmac - successful
			
				// Get the body of the PUT
				JsonObject logLevelJson = JsonUtils.getJsonObjectFromInputStream(logLevelStream, StandardCharsets.UTF_8, gLog) ;
	
				if (logLevelJson != null) {
				// Get the level element
					
					boolean success = RestApiSkeletonControl.setLogsLevel(logLevelJson);
					JsonObject newLevels = RestApiSkeletonControl.getLogsLevels() ;
	
					if (success) {
						apiReturn.setDataReturn(newLevels);
					} else {
						StringBuilder msg = new StringBuilder() ;
						msg.append(PUT_LOGS).append("with json body parameter\n") ;
						msg.append(logLevelJson.toString()) ;
						msg.append("\nNew levels:\n") ;
						msg.append(newLevels.toString()) ;
						apiReturn.setErrorReturn(ApiErrorCodeBuilder.ERROR_PROCESSING_JSON_BODY_CODE, msg.toString()) ;
					} 
				}else {
					apiReturn.setErrorReturn(ApiErrorCodeBuilder.NO_LOGLEVEL_CODE, PUT_LOGS) ;
				}
				
			} else {
				// Hmac checking has failed
				apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, PUT_LOGS) ;
			}
		} catch (Exception e) {
			gLog.log(Level.SEVERE, "Exception in API " + PUT_LOGS, e) ;
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.GENERAL_EXCEPTION_CODE, PUT_LOGS + ": " + e.toString()) ;
		}
		return Response.ok(apiReturn.getApiReturnJson(PUT_LOGS), RestApiSkeletonControl.getResponseContentType()).build() ;
	}
	
	// Get log levels
	@GET @Path("/levels")
	public Response getLevels(@HeaderParam("Authorization") String authorizationHeader,
			 				  @HeaderParam("Device-Id") String deviceId,
			 				  @HeaderParam("Timestamp") String timestamp,
			 				 @Context UriInfo uriInfo) {
		// Get log levels of the application 
		
		ExecutionDurations execDuration = new ExecutionDurations(appProp) ;
		if (! RestApiSkeletonControl.isInitDone()) {
			RestApiSkeletonControl.init(execDuration);
		}
		
		Logger gLog = RestApiSkeletonControl.getLogger() ;
		execDuration.setTriggerLevel(gLog, RestApiSkeletonControl.getAdditionalInfosAndLoggingLogLevel());
		ApiReturn apiReturn = new ApiReturn(execDuration, RestApiSkeletonControl.getResponseCharsetSet(), gLog) ;
		
		try {
			if (HMACUtils.checkHmacAdmin(authorizationHeader, "GET", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
			// Check Hmac - successful
	
				apiReturn.setDataReturn(RestApiSkeletonControl.getLogsLevels());
				
			} else {
				// Hmac checking has failed
				apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, GET_LOG_LEVELS) ;
			}
		} catch (Exception e) {
			gLog.log(Level.SEVERE, "Exception in API " + GET_LOG_LEVELS, e) ;
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.GENERAL_EXCEPTION_CODE, GET_LOG_LEVELS + ": " + e.toString()) ;
		}

		return Response.ok(apiReturn.getApiReturnJson(GET_LOG_LEVELS), RestApiSkeletonControl.getResponseContentType()).build() ;
	}
}
