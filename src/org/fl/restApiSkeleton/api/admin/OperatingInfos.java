package org.fl.restApiSkeleton.api.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fl.restApiSkeleton.RestApiSkeletonControl;
import org.fl.restApiSkeleton.security.HMACUtils;

import com.google.gson.JsonObject;
import com.ibm.lge.fl.util.ExecutionDurations;
import com.ibm.lge.fl.util.api.ApiErrorCodeBuilder;
import com.ibm.lge.fl.util.api.ApiReturn;

@Path("/v1/admin/operatingInfos")
public class OperatingInfos {

	private final static String GET_OPERATING_INFOS = "GET /admin/operatingInfos " ;
	
	// property name for execution duration
	private final static String appProp = "apiProcess" ;

	public OperatingInfos() {
		// MUST BE EMPTY
		// Putting "RestApiSkeletonControl.init(); " makes an invocation exception in bluemix (and not on local liberty)	
	}
	
	// Get operating infos
	@GET
	public Response get(@HeaderParam("Authorization") String authorizationHeader,
			 			@HeaderParam("Device-Id") String deviceId,
			 			@HeaderParam("Timestamp") String timestamp,
			 			@DefaultValue("false")
			 			@QueryParam("IpLookUp") boolean ipLookUp,
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
		if (HMACUtils.checkHmacAdmin(authorizationHeader, "GET", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
		// Check Hmac - successful

			JsonObject operatingContext = RestApiSkeletonControl.getRunningContext().getApplicationInfo(ipLookUp) ;

			apiReturn.setDataReturn(operatingContext) ;
		} else {
			// Hmac checking has failed
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, GET_OPERATING_INFOS) ;
		}
			} catch (Exception e) {
			gLog.log(Level.SEVERE, "Exception in API " + GET_OPERATING_INFOS, e) ;
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.GENERAL_EXCEPTION_CODE, GET_OPERATING_INFOS + ": " + e.toString()) ;
		}

		return Response.ok(apiReturn.getApiReturnJson(GET_OPERATING_INFOS), RestApiSkeletonControl.getResponseContentType()).build() ;
	}
}
