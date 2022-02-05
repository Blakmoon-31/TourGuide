package tourGuide.domain;

import tourGuide.beans.LocationBean;

/**
 * Represent an attraction nearby the user.
 * 
 */
public class NearbyAttraction {

	private String attractionName;
	private double attractionLatitude;
	private double attractionLongitude;
	private LocationBean userLocation;
	// Distance between the attraction and the user's location
	private double attractionDistance;
	private int attractionRewardpoints;

	public String getAttractionName() {
		return attractionName;
	}

	public void setAttractionName(String attractionName) {
		this.attractionName = attractionName;
	}

	public double getAttractionLatitude() {
		return attractionLatitude;
	}

	public void setAttractionLatitude(double attractionLatitude) {
		this.attractionLatitude = attractionLatitude;
	}

	public double getAttractionLongitude() {
		return attractionLongitude;
	}

	public void setAttractionLongitude(double attractionLongitude) {
		this.attractionLongitude = attractionLongitude;
	}

	public LocationBean getUserLocation() {
		return userLocation;
	}

	public void setUserLocation(LocationBean userLocationBean) {
		this.userLocation = userLocationBean;
	}

	public double getAttractionDistance() {
		return attractionDistance;
	}

	public void setAttractionDistance(double attractionDistance) {
		this.attractionDistance = attractionDistance;
	}

	public int getAttractionRewardpoints() {
		return attractionRewardpoints;
	}

	public void setAttractionRewardpoints(int attractionRewardpoints) {
		this.attractionRewardpoints = attractionRewardpoints;
	}

}
