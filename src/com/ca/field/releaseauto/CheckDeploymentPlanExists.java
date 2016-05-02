package com.ca.field.releaseauto;

import com.nolio.platform.shared.api.*;

/**
 * An example Nolio action
 * 
 * <p>Date: Aug 19, 2014</p>
 *
 * @author kouth01
 */
@ActionDescriptor(
    name = "Check If Deployment Plan Exists",
    description = "This action checks whether a deployment plan exists for a specific application and project",
    category="Release Operations Center.Field")

public class CheckDeploymentPlanExists extends ReleaseAutoBase {
    private static final long serialVersionUID = 1L;
    
    @ParameterDescriptor(name="Application Name", description="", in=true, out=false, nullable=false, order=1)
    private String applicationName;

    @ParameterDescriptor(name="Project Name", description="", in=true, out=false, nullable=false, order=2)
    private String projectName;

    @ParameterDescriptor(name="Deployment Plan Name", description="", in=true, out=false, nullable=false, order=3)
    private String deploymentPlanName;
    
    @ParameterDescriptor(name="Build", description="", in=true, out=false, nullable=false, order=4)
    private String buildName;
    
    @ParameterDescriptor(name="Error Message", description="", in=false, out=true, nullable=false, order=1)  
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
    	
		return new ActionResult(true, "Deployment Plan [" + deploymentPlanName + "] Build [" + buildName + "] found for Project [" + projectName + "] in Application [" + applicationName + "]");
    }
}
