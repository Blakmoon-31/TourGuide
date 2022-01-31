package tourGuide.beans;

import java.util.UUID;

public class AttractionBean extends LocationBean {

	public UUID attractionId;

	public String attractionName;

	public String city;

	public String state;

	public UUID getAttractionId() {
		return attractionId;
	}

	public void setAttractionId(UUID attractionId) {
		this.attractionId = attractionId;
	}

	public String getAttractionName() {
		return attractionName;
	}

	public void setAttractionName(String attractionName) {
		this.attractionName = attractionName;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public AttractionBean() {
		super();
	}

	public AttractionBean(String attractionName, String city, String state, double latitude, double longitude) {
		this.attractionName = attractionName;
		this.city = city;
		this.state = state;
		this.latitude = latitude;
		this.longitude = longitude;
	}

}
