package com.ca.field.releaseauto;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import net.minidev.json.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.nolio.platform.shared.api.*;

/**
 * An example Nolio action
 * 
 * <p>Date: Aug 19, 2014</p>
 *
 * @author kouth01
 */

public abstract class ReleaseAutoBase implements NolioAction {
    private static final long serialVersionUID = 1L;
       
    @ParameterDescriptor(name="Base RA URL", description="In the form of http(s)://server:port", in=true, out=false, nullable=false, order=101)
    protected String baseUrl;
    
    @ParameterDescriptor(name="Username", description="", in=true, out=false, nullable=false, order=102)
    protected String username;
    
    @ParameterDescriptor(name="Password", description="", in=true, out=false, nullable=false, order=103)    
    protected Password password;
    
    protected String basePath = "/datamanagement/a/api/v3";
    protected Client client = null;
    
	protected String getApplicationId(String appName) {
		String jsonString = null;
		String result = null;
		String name = null;
		
		if (client == null) {
			return null;
		}
		
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").request();
		jsonString = request.get(String.class);
		
		List<JSONObject> apps = JsonPath.read(jsonString, "[*]");
		for (JSONObject app : apps) {
			name = JsonPath.read(app, "$.name");
			if (appName.equalsIgnoreCase(name)) {
				result = JsonPath.read(app, "$.id");
				break;
			}
		}
		
		return result;
	}    

	protected String getEnvironmentId(String applicationId, String environmentName) {
		String jsonString = null;
		String result = null;
		String name = null;
		
		if (client == null) {
			return null;
		}
		
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").path(applicationId).path("environments").request();
		jsonString = request.get(String.class);
		
		List<JSONObject> projects = JsonPath.read(jsonString, "[*]");
		for (JSONObject project : projects) {
			name = JsonPath.read(project, "$.name");
			if (environmentName.equalsIgnoreCase(name)) {
				result = JsonPath.read(project, "$.id");
				break;
			}
		}
		
		return result;
	} 	
	
	protected String getReleaseId(String applicationId, String environmentId, String deploymentName) {
		String jsonString = null;
		String result = null;
		String name = null;
		
		if (client == null) {
			return null;
		}
		
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").path(applicationId).path("environments")
				.path(environmentId).path("releases").request();
		jsonString = request.get(String.class);
		
		List<JSONObject> projects = JsonPath.read(jsonString, "[*]");
		for (JSONObject project : projects) {
			name = JsonPath.read(project, "$.name");
			if (deploymentName.equalsIgnoreCase(name)) {
				result = JsonPath.read(project, "$.id");
				break;
			}
		}
		
		return result;
	}   	
		
	protected String getProjectId(String applicationId, String projectName) {
		String jsonString = null;
		String result = null;
		String name = null;
		
		if (client == null) {
			return null;
		}
		
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").path(applicationId).path("projects").request();
		jsonString = request.get(String.class);
		
		List<JSONObject> projects = JsonPath.read(jsonString, "[*]");
		for (JSONObject project : projects) {
			name = JsonPath.read(project, "$.name");
			if (projectName.equalsIgnoreCase(name)) {
				result = JsonPath.read(project, "$.id");
				break;
			}
		}
		
		return result;
	}  
	
	protected String getDeploymentPlanId(String applicationId, String projectId, String deploymentPlanName, String buildName) {
		String jsonString = null;
		String result = null;
		String name = null;
		String build = null;
		
		if (client == null) {
			return null;
		}
		
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").path(applicationId).path("projects")
				.path(projectId).path("deployment-plans").request();
		jsonString = request.get(String.class);
		
		List<JSONObject> projects = JsonPath.read(jsonString, "[*]");
		for (JSONObject project : projects) {
			name = JsonPath.read(project, "$.deploymentPlan");
			build = JsonPath.read(project, "$.build");
			if (deploymentPlanName.equalsIgnoreCase(name) && buildName.equals(build)) {
				result = JsonPath.read(project, "$.deploymentPlanId");
				break;
			}
		}
		
		return result;
	}  	
	
    public ActionResult executeAction() {
    	client = ClientBuilder.newClient();
    	HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(username, password.toString());
    	client.register(auth);
		return null;
    }
}
