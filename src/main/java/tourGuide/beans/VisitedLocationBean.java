package tourGuide.beans;

import java.util.Date;
import java.util.UUID;

public class VisitedLocationBean {

	public UUID userId;

	public LocationBean location;

	public Date timeVisited;

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public LocationBean getLocation() {
		return location;
	}

	public void setLocation(LocationBean locationBean) {
		this.location = locationBean;
	}

	public Date getTimeVisited() {
		return timeVisited;
	}

	public void setTimeVisited(Date timeVisited) {
		this.timeVisited = timeVisited;
	}

	public VisitedLocationBean() {
		super();
	}

	public VisitedLocationBean(UUID userId, LocationBean locationBean, Date timeVisited) {
		super();
		this.userId = userId;
		this.location = locationBean;
		this.timeVisited = timeVisited;
	}

}
