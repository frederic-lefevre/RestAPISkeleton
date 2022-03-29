package org.fl.restApiSkeleton.api;

import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fl.restApiSkeleton.RestApiSkeletonControl;
import org.fl.restApiSkeleton.security.HMACUtils;

import com.google.gson.JsonObject;
import org.fl.util.ExecutionDurations;
import org.fl.util.api.ApiErrorCodeBuilder;
import org.fl.util.api.ApiReturn;

@Path("/v1/sampleGet")
public class SampleGet {

	// property name for execution duration
	private final static String appProp = "apiProcess" ;
	
	// Name for API end point
	private final static String GET_SAMPLE = "GET /sampleGet " ;
	
	public  SampleGet() {
		// MUST BE EMPTY
		// Putting "RestApiSkeletonControl.init(); " makes an invocation exception in bluemix (and not on local liberty)		
	}
	
	@GET @Path("/{domain}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@HeaderParam("Authorization") String authorizationHeader,
			 			@HeaderParam("Device-Id") String deviceId,
			 			@HeaderParam("Timestamp") String timestamp,
						@PathParam("domain") 	String domain, 
			            @Context UriInfo uriInfo) {
		
		ExecutionDurations execDuration = new ExecutionDurations(appProp) ;
		if (! RestApiSkeletonControl.isInitDone()) {
			RestApiSkeletonControl.init(execDuration) ;
		}
		
		Logger gLog = RestApiSkeletonControl.getLogger() ;
		
		execDuration.setTriggerLevel(gLog, RestApiSkeletonControl.getAdditionalInfosAndLoggingLogLevel());
		ApiReturn apiReturn = new ApiReturn(execDuration, RestApiSkeletonControl.getResponseCharsetSet(), gLog) ;
		
		if (HMACUtils.checkHmac(authorizationHeader, "GET", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
			// Check Hmac - successful

			JsonObject sampleReturn = new JsonObject() ;
			sampleReturn.addProperty("pathParam", domain);
			apiReturn.setDataReturn(sampleReturn);
			
		} else {
			// Hmac checking has failed
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, GET_SAMPLE) ;
		}
		
		return Response.ok(apiReturn.getApiReturnJson(GET_SAMPLE), RestApiSkeletonControl.getResponseContentType()).build() ;
	}
}
