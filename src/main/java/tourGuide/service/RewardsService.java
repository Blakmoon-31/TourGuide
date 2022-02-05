package tourGuide.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.RewardCentralProxy;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@Service
public class RewardsService {

	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;

	@Autowired
	private RewardCentralProxy rewardCentralProxy;

	@Autowired
	private GpsUtilProxy gpsUtilProxy;

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	/**
	 * Calculate rewards points for a user.
	 * 
	 * @param user - A User object
	 */
	public synchronized void calculateRewards(User user) {
		List<VisitedLocationBean> locationsOfUser = user.getVisitedLocations();
		List<VisitedLocationBean> userLocations = new CopyOnWriteArrayList<VisitedLocationBean>(locationsOfUser);

		List<AttractionBean> attractions = gpsUtilProxy.getAllAttractions();

		for (VisitedLocationBean visitedLocation : userLocations) {
			for (AttractionBean attraction : attractions) {
				if (user.getUserRewards().stream()
						.filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if (nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(
								new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}

	/**
	 * Calculate rewards points for a list of users. Use a pool of 100 threads.
	 * 
	 * @param user - An list of User objects
	 */
	public synchronized void calculateAllUsersRewards(List<User> users) {

		final ExecutorService executorRewardsService = Executors.newFixedThreadPool(100);

		List<AttractionBean> attractions = gpsUtilProxy.getAllAttractions();
		List<Callable<String>> callables = new ArrayList<Callable<String>>();

		for (User user : users) {
			callables.add(new Callable<String>() {

				public String call() throws Exception {
					List<VisitedLocationBean> locationsOfUser = user.getVisitedLocations();
					for (VisitedLocationBean visitedLocation : locationsOfUser) {
						for (AttractionBean attraction : attractions) {
							if (user.getUserRewards().stream()
									.filter(r -> r.attraction.attractionName.equals(attraction.attractionName))
									.count() == 0) {
								if (nearAttraction(visitedLocation, attraction)) {
									user.addUserReward(new UserReward(visitedLocation, attraction,
											getRewardPoints(attraction, user)));
								}
							}
						}
					}
					return "Done";
				}
			});
		}

		try {
			executorRewardsService.invokeAll(callables);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				executorRewardsService.shutdown();
				executorRewardsService.awaitTermination(30L, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isWithinAttractionProximity(AttractionBean attraction, LocationBean location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	private boolean nearAttraction(VisitedLocationBean visitedLocation, AttractionBean attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	public int getRewardPoints(AttractionBean attraction, User user) {
		return rewardCentralProxy.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(LocationBean loc1, LocationBean loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math
				.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
		return statuteMiles;
	}

}