package com.ca.field.releaseauto;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Invocation;
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
@ActionDescriptor(
    name = "Get Deployment Plan Deployments",
    description = "This action returns a list of successful deployments and a list of failed deployments for a Deployment Plan.  Optionally, an Environment Name can be specified to filter results for that specific environnment.  Also, the action can be configured to fail if no successful deployments are found.",
    category="Release Operations Center.Field")

public class GetDeploymentPlanDeployments extends ReleaseAutoBase {
    private static final long serialVersionUID = 1L;
    
    @ParameterDescriptor(name="Application Name", description="", in=true, out=false, nullable=false, order=1)
    private String applicationName;

    @ParameterDescriptor(name="Project Name", description="", in=true, out=false, nullable=false, order=2)
    private String projectName;

    @ParameterDescriptor(name="Deployment Plan Name", description="", in=true, out=false, nullable=false, order=3)
    private String deploymentPlanName;
    
    @ParameterDescriptor(name="Build", description="", in=true, out=false, nullable=false, order=4)
    private String buildName;

    @ParameterDescriptor(name="Environment Name", description="Optionally filter for specific environment.  If left blank, the action will return results based on all environments.", in=true, out=false, nullable=true, order=5)
    private String environmentNameFilter;    

    @ParameterDescriptor(name="Fail If No Successful Deployments", description="If set to true, the action will fail if no successful deployments are found.", in=true, out=false, nullable=false, order=6)
    private Boolean failIfNoSuccess;    
    
    @ParameterDescriptor(name="Successful Deployments", description="", in=false, out=true, nullable=false, order=1)  
    private String[] successfulDeployments = new String[0];
    
    @ParameterDescriptor(name="Failed Deployments", description="", in=false, out=true, nullable=false, order=2)  
    private String[] failedDeployments = new String[0];
    
    @ParameterDescriptor(name="Error Message", description="", in=false, out=true, nullable=false, order=4)  
    private String errorMessage = "";
    
    private String applicationId;
    private String projectId;
    private String deploymentPlanId;
    
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
    	if (failIfNoSuccess == null) {
    		failIfNoSuccess = false;
    	}
    	return null;
	}

    public ActionResult executeAction() {
    	ActionResult ar = validateInputs();
    	if (ar != null) { 
    		return ar; 
    	}
    	
    	super.executeAction();
    	   	
    	applicationId = getApplicationId(applicationName);
    	if (applicationId == null) {
    		errorMessage = "Application [" + applicationName + "] not found";
    		return new ActionResult(false, errorMessage);
    	}
    
    	projectId = getProjectId(applicationId, projectName);
    	if (projectId == null) {
    		errorMessage = "Project [" + projectName + "] not found for Application [" + applicationName + "]";
    		return new ActionResult(false, errorMessage);
    	}
    	
    	deploymentPlanId = getDeploymentPlanId(applicationId, projectId, deploymentPlanName, buildName);
    	if (deploymentPlanId == null) {
    		errorMessage = "Deployment Plan [" + deploymentPlanName + "] Build [" + buildName + "] not found for Project [" + projectName + "] in Application [" + applicationName + "]";
    		return new ActionResult(false, errorMessage);
    	}
    	
    	if (environmentNameFilter != null && !environmentNameFilter.isEmpty()) {
    		if (getEnvironmentId(applicationId, environmentNameFilter) == null) {
        		errorMessage = "Environment [" + environmentNameFilter + "] not found for Application [" + applicationName + "]";
        		return new ActionResult(false, errorMessage);	
    		}
    	}
    	
		String jsonString = null;
		String environment = null;
		String status = null;
		String deploymentName = null;
		
		Invocation.Builder request = client.target(baseUrl + basePath).path("applications").path(applicationId).path("projects")
				.path(projectId).path("deployment-plans").path(deploymentPlanId).request();
		jsonString = request.get(String.class);
		
		Boolean environmentNameFilterExists = environmentNameFilter != null && !environmentNameFilter.isEmpty();
		
		List<String> successes = new ArrayList<String>();
		List<String> failures = new ArrayList<String>();
		
		List<JSONObject> deployments = JsonPath.read(jsonString, "$.deploymentsStatus");
		for (JSONObject deployment : deployments) {
			environment = JsonPath.read(deployment, "$.environment");
			if (!environmentNameFilterExists || environmentNameFilter.equals(environment)) {
				status = JsonPath.read(deployment, "$.status");
				deploymentName = JsonPath.read(deployment, "$.deployment");
				if (!environmentNameFilterExists) {
					deploymentName = deploymentName + " -> " + environment;
				}
				if (status.contains("succeeded")) {
					successes.add(deploymentName);
				} else if (status.contains("failed")) {
					failures.add(deploymentName);
				}
			}
		}
		
		successfulDeployments = (String[]) successes.toArray(new String[successes.size()]);
		failedDeployments = (String[]) failures.toArray(new String[failures.size()]);
		
		Boolean result = true;
		
		if (failIfNoSuccess && successfulDeployments.length == 0) {
			result = false;
		}
		
		if (environmentNameFilterExists) {
			return new ActionResult(result, successfulDeployments.length + " successful deployments and " + failedDeployments.length + " failed deployments found for Application [" + applicationName + "] Project [" + projectName + "] Deployment Plan [" + deploymentPlanName + "] Build [" + buildName + "] Environment [" + environmentNameFilter + "]");
		} else {
			return new ActionResult(result, successfulDeployments.length + " successful deployments and " + failedDeployments.length + " failed deployments found for Application [" + applicationName + "] Project [" + projectName + "] Deployment Plan [" + deploymentPlanName + "] Build [" + buildName + "]");
		}		
    }
}
