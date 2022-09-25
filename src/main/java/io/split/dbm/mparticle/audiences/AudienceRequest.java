package io.split.dbm.mparticle.audiences;

import java.util.List;

public class AudienceRequest {

	String apiToken;
	String workspaceId;
	String environmentId;
	String trafficTypeId;
	String verb;
	String segment;
	
	List<String> mpids; // NOT a part of hashcode/equals
	
	public String getApiToken() {
		return apiToken;
	}




	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}




	public String getWorkspaceId() {
		return workspaceId;
	}




	public void setWorkspaceId(String workspaceId) {
		this.workspaceId = workspaceId;
	}




	public String getEnvironmentId() {
		return environmentId;
	}




	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}




	public String getTrafficTypeId() {
		return trafficTypeId;
	}




	public void setTrafficTypeId(String trafficTypeId) {
		this.trafficTypeId = trafficTypeId;
	}




	public List<String> getMpids() {
		return mpids;
	}




	public void setMpids(List<String> mpids) {
		this.mpids = mpids;
	}


	

	@Override
	public String toString() {
		return "AudienceRequest [apiToken=" + apiToken + ", workspaceId=" + workspaceId + ", environmentId="
				+ environmentId + ", trafficTypeId=" + trafficTypeId + ", verb=" + verb + ", segment=" + segment
				+ ", mpids=" + mpids + "]";
	}




	public String getVerb() {
		return verb;
	}




	public void setVerb(String verb) {
		this.verb = verb;
	}




	public String getSegment() {
		return segment;
	}




	public void setSegment(String segment) {
		this.segment = segment;
	}




	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((apiToken == null) ? 0 : apiToken.hashCode());
		result = prime * result + ((environmentId == null) ? 0 : environmentId.hashCode());
		result = prime * result + ((segment == null) ? 0 : segment.hashCode());
		result = prime * result + ((trafficTypeId == null) ? 0 : trafficTypeId.hashCode());
		result = prime * result + ((verb == null) ? 0 : verb.hashCode());
		result = prime * result + ((workspaceId == null) ? 0 : workspaceId.hashCode());
		return result;
	}




	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AudienceRequest other = (AudienceRequest) obj;
		if (apiToken == null) {
			if (other.apiToken != null)
				return false;
		} else if (!apiToken.equals(other.apiToken))
			return false;
		if (environmentId == null) {
			if (other.environmentId != null)
				return false;
		} else if (!environmentId.equals(other.environmentId))
			return false;
		if (segment == null) {
			if (other.segment != null)
				return false;
		} else if (!segment.equals(other.segment))
			return false;
		if (trafficTypeId == null) {
			if (other.trafficTypeId != null)
				return false;
		} else if (!trafficTypeId.equals(other.trafficTypeId))
			return false;
		if (verb == null) {
			if (other.verb != null)
				return false;
		} else if (!verb.equals(other.verb))
			return false;
		if (workspaceId == null) {
			if (other.workspaceId != null)
				return false;
		} else if (!workspaceId.equals(other.workspaceId))
			return false;
		return true;
	}


}
