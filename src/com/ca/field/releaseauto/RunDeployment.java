package com.ca.field.releaseauto;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.nolio.platform.shared.api.*;

/**
 * An example Nolio action
 * 
 * <p>Date: Aug 19, 2014</p>
 *
 * @author kouth01
 */
@ActionDescriptor(
    name = "Run Deployment",
    description = "This action runs a deployment from a deployment plan and waits for the deployment to finish",
    category="Release Operations Center.Field")

public class RunDeployment extends ReleaseAutoBase {
    private static final long serialVersionUID = 1L;
    
    @ParameterDescriptor(name="Application Name", description="", in=true, out=false, nullable=false, order=1)
    private String applicationName;

    @ParameterDescriptor(name="Project Name", description="", in=true, out=false, nullable=false, order=2)
    private String projectName;

    @ParameterDescriptor(name="Deployment Plan Name", description="", in=true, out=false, nullable=false, order=3)
    private String deploymentPlanName;
    
    @ParameterDescriptor(name="Build", description="", in=true, out=false, nullable=false, order=4)
    private String buildName;
   
    @ParameterDescriptor(name="Environment", description="", in=true, out=false, nullable=false, order=5)
    private String environmentName;
    
    @ParameterDescriptor(name="Deployment Name", description="", in=true, out=false, nullable=false, order=6)
    private String deploymentName;

    @ParameterDescriptor(name="Error Message", description="", in=false, out=true, nullable=false, order=1)  
    private String errorMessage = "";
    
    private String postString = "";
    private Integer retrySeconds = 5;
    
    private void addToPostString(String name, String value) {
    	if (postString.isEmpty() == false) {
    		postString += ",";
    	}
    	postString += "\"" + name + "\":\"" + value + "\""; 
    }
    
    private void addToPostString(String name, String[] values) {
    	if (postString.isEmpty() == false) {
    		postString += ",";
    	}
    	postString += "\"" + name + "\":[";
    	for (int i = 0; i < values.length; i++) {
    		if (i > 0) {
    			postString += ",";
    		}
    		postString += "\"" + values[i] + "\"";
    	}
    	postString += "]";
    }
    
	private String waitForDeployEnd(String applicationId, String environmentId, String deploymentId) {
		String jsonString = null;
		String status = null;
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").path(applicationId)
				.path("environments").path(environmentId).path("releases").path(deploymentId).request();
		
		while (status == null || !(status.equals("100% Succeeded") || status.equals("100% Failed"))) {
			try {
				Thread.sleep(retrySeconds * 1000);
			} catch (InterruptedException e) {
				return null;
			}
			jsonString = request.get(String.class);
			try {
				status = JsonPath.read(jsonString, "$.status");
			} catch (PathNotFoundException e) {
				status = null;
				return null;
			}
		}
		return status;
	}   
	
	private ActionResult validateInputs() {
    	if (applicationName == null || applicationName.isEmpty()) {
    		return new ActionResult(false, "Application Name cannot be null/empty");
    	}
    	if (projectName == null || projectName.isEmpty()) {
    		return new ActionResult(false, "Project Name cannot be null/empty");
    	}
    	if (deploymentPlanName == null || deploymentPlanName.isEmpty()) {
    		return new ActionResult(false, "Deployment Plan Name cannot be null/empty");
    	}
    	if (buildName == null || buildName.isEmpty()) {
    		return new ActionResult(false, "Build cannot be null/empty");
    	}
    	if (environmentName == null || environmentName.isEmpty()) {
    		return new ActionResult(false, "Environment Name cannot be null/empty");
    	}
    	if (deploymentName == null || deploymentName.isEmpty()) {
    		return new ActionResult(false, "Deployment Name cannot be null/empty");
    	}
    	return null;
	}

    public ActionResult executeAction() {
    	ActionResult ar = validateInputs();
    	if (ar != null) { 
    		return ar; 
    	}
    	
    	super.executeAction();
    	
    	postString = "";
    	WebTarget wt = null;
    	
    	addToPostString("deployment", deploymentName);
    	addToPostString("application", applicationName);
    	String[] env = {environmentName};
    	addToPostString("environments", env);
    	addToPostString("deploymentPlan", deploymentPlanName);
    	addToPostString("build", buildName);
    	addToPostString("project", projectName);
    	addToPostString("stageToPerform", "Deployment");
    	postString = "{" + postString + "}";
    	
    	Response response = null;
    	wt = client.target(baseUrl + basePath).path("run-deployments");
    	response = wt.request().post(Entity.entity(postString, MediaType.TEXT_PLAIN));
		if (response.getStatus() != 200) {
			return new ActionResult(false, "Server responded with HTTP code " + response.getStatus());
		}
		String jsonString = response.readEntity(String.class);
        Boolean result = null;
        String description = null;
		try {
			result = JsonPath.read(jsonString, "$.result");
			description = JsonPath.read(jsonString, "$.description");
		} catch (PathNotFoundException e) {
			try {
				result = JsonPath.read(jsonString, "$[0].result");
				description = JsonPath.read(jsonString, "$[0].description");
			} catch (PathNotFoundException e1) {
			}
		}
		if (result == null || result == false) {
			errorMessage = description;
			return new ActionResult(false, "Unable to deploy: " + description);
		}
		
        String applicationId = getApplicationId(applicationName);
		String deploymentId = JsonPath.read(jsonString, "$[0].id");
		String environmentId = JsonPath.read(jsonString, "$[0].envId");
		String deploymentStatus = waitForDeployEnd(applicationId, environmentId, deploymentId);
		
		String message = "Deployment [" + deploymentName + "] created from Deployment Plan [" + deploymentPlanName + "] Build [" + buildName + "] for Project [" + projectName + "] in Application [" + applicationName + "] ended with status [" + deploymentStatus + "]";
		
		if (deploymentStatus.equalsIgnoreCase("100% Failed")) {
			errorMessage = message;
			return new ActionResult(false, errorMessage);
		}
		
		return new ActionResult(true, message);
    }
}
