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
    name = "Get Unique Deployment Name",
    description = "This action checks to see if the supplied Deployment Name is unique.  If not, it will add a number to the end of the name to make it unique.",
    category="Release Operations Center.Field")

public class GetUniqueDeploymentName extends ReleaseAutoBase {
    private static final long serialVersionUID = 1L;
    
    @ParameterDescriptor(name="Application Name", description="", in=true, out=false, nullable=false, order=1)
    private String applicationName;

    @ParameterDescriptor(name="Environment Name", description="", in=true, out=false, nullable=false, order=2)
    private String environmentName;

    @ParameterDescriptor(name="Deployment Name", description="", in=true, out=false, nullable=false, order=3)
    private String deploymentNameIn;
    
    @ParameterDescriptor(name="Deployment Name", description="", in=false, out=true, nullable=false, order=1)  
    private String deploymentName;
    
    @ParameterDescriptor(name="Error Message", description="", in=false, out=true, nullable=false, order=2)  
    private String errorMessage = "";    
    
    private String applicationId;
    private String environmentId;

	private ActionResult validateInputs() {
    	if (applicationName == null || applicationName.isEmpty()) {
    		return new ActionResult(false, "Application Name cannot be null/empty");
    	}
    	if (environmentName == null || environmentName.isEmpty()) {
    		return new ActionResult(false, "Environment Name cannot be null/empty");
    	}
    	if (deploymentNameIn == null || deploymentNameIn.isEmpty()) {
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
    	
    	applicationId = getApplicationId(applicationName);
    	if (applicationId == null) {
    		errorMessage = "Application [" + applicationName + "] not found";
    		return new ActionResult(false, errorMessage);
    	}
    
    	environmentId = getEnvironmentId(applicationId, environmentName);
    	if (environmentId == null) {
    		errorMessage = "Environment [" + environmentName + "] not found for Application [" + applicationName + "]";
    		return new ActionResult(false, errorMessage);
    	}
    	
    	Integer nextNumber = 2;
    	deploymentName = deploymentNameIn;
    	
    	String releaseId = getReleaseId(applicationId, environmentId, deploymentName);
    	while (releaseId != null) {
    		deploymentName = deploymentNameIn + " (" + nextNumber++ + ")";
    		releaseId = getReleaseId(applicationId, environmentId, deploymentName);
    	}
    	
		return new ActionResult(true, deploymentName);
    }
}
