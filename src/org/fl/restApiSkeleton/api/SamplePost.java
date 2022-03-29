package org.fl.restApiSkeleton.api;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fl.restApiSkeleton.RestApiSkeletonControl;
import org.fl.restApiSkeleton.security.HMACUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fl.util.CompressionUtils;
import org.fl.util.ExecutionDurations;
import org.fl.util.api.ApiErrorCodeBuilder;
import org.fl.util.api.ApiJsonPropertyName;
import org.fl.util.api.ApiReturn;
import org.fl.util.json.JsonUtils;

@Path("/v1/samplePost")
public class SamplePost {

	// property name for execution duration
	private final static String appProp = "apiProcess" ;
	
	// Name for the API end point
	private final static String POST_SAMPLE = "POST /sample " ;
	
	public SamplePost() {
	   	// MUST BE EMPTY
    	// Putting "SkylineControl.init(); " makes an invocation exception in bluemix (and not on local liberty)	
    }

    @POST @Path("/{domain}")
	@Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
	public Response post(@HeaderParam("Authorization") String authorizationHeader,
			 @HeaderParam("Device-Id") String deviceId,
			 @HeaderParam("Timestamp") String timestamp,
			 @PathParam("domain") String domain,
			 @Context UriInfo uriInfo,
			 InputStream queryParams) {
		// Get latest logs of the application 
		
    	ExecutionDurations execDuration = new ExecutionDurations(appProp) ;
		if (! RestApiSkeletonControl.isInitDone()) {
			RestApiSkeletonControl.init(execDuration);			
		}
		
		Logger gLog = RestApiSkeletonControl.getLogger() ;
		execDuration.setTriggerLevel(gLog, RestApiSkeletonControl.getAdditionalInfosAndLoggingLogLevel());
		ApiReturn apiReturn = new ApiReturn(execDuration, RestApiSkeletonControl.getResponseCharsetSet(), gLog) ;
		boolean doCompression = false ;
		
		if (HMACUtils.checkHmac(authorizationHeader, "POST", uriInfo.getAbsolutePath().getPath(), deviceId, timestamp, gLog)) {
			// Check Hmac - successful
			
			// parse the POST body to get a JsonObject. Assume UTF-8 charset, JSON default norm
			JsonObject paramsJson = JsonUtils.getJsonObjectFromInputStream(queryParams, StandardCharsets.UTF_8, gLog) ;

			if ((paramsJson == null) || (paramsJson.size() == 0)) {
				// no data provided
				apiReturn.setErrorReturn(ApiErrorCodeBuilder.NO_DATA_PROVIDED_CODE, POST_SAMPLE) ;
			} else {
				
				JsonObject sampleReturn = new JsonObject() ;
				sampleReturn.addProperty("pathParam", domain);
				sampleReturn.add("bodyParam", paramsJson);
				apiReturn.setDataReturn(sampleReturn);				
				
				JsonElement compressElem = paramsJson.get(ApiJsonPropertyName.COMPRESS_RETURN) ;
				if (compressElem != null) {
					try {
						doCompression = compressElem.getAsBoolean() ;
					} catch (Exception e) {
						apiReturn.setErrorReturn(ApiErrorCodeBuilder.INVALID_COMPRESSION_ELEM_CODE, paramsJson.toString()) ;
					}
				}
			}
			
		} else {
			// Hmac checking has failed
			apiReturn.setErrorReturn(ApiErrorCodeBuilder.UNAUTHORIZED_CODE, POST_SAMPLE) ;
		}
		
		if (doCompression && (! apiReturn.isOnError())) {
			// if compression is asked
			
			// Get the compressed the API return
			byte[] lineItemsReturn = apiReturn.getCompressedApiReturn(POST_SAMPLE, CompressionUtils.SupportedCompression.GZIP) ;
			
			if (! apiReturn.isOnError()) {
				// if there has been no error (during compression), send the compressed return 
				return Response.ok(lineItemsReturn, "application/zip").encoding("gzip").build() ;
			}		
		} 
		
		// return without compression
		return Response.ok(apiReturn.getApiReturnJson(POST_SAMPLE), RestApiSkeletonControl.getResponseContentType()).build() ;
	}
}
