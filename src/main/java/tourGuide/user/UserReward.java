package tourGuide.user;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;

public class UserReward {

	public final VisitedLocationBean visitedLocation;
	public final AttractionBean attraction;
	private int rewardPoints;

	public UserReward(VisitedLocationBean visitedLocationBean, AttractionBean attractionBean, int rewardPoints) {
		this.visitedLocation = visitedLocationBean;
		this.attraction = attractionBean;
		this.rewardPoints = rewardPoints;
	}

	public UserReward(VisitedLocationBean visitedLocationBean, AttractionBean attractionBean) {
		this.visitedLocation = visitedLocationBean;
		this.attraction = attractionBean;
	}

	public void setRewardPoints(int rewardPoints) {
		this.rewardPoints = rewardPoints;
	}

	public int getRewardPoints() {
		return rewardPoints;
	}

}
