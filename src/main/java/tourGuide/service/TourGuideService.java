package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.ProviderBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.domain.NearAttraction;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.proxies.TripPricerProxy;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;

@Service
public class TourGuideService {

	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final RewardsService rewardsService;
	public final Tracker tracker;
	boolean testMode = true;

	@Autowired
	private TripPricerProxy tripPricerProxy;

	@Autowired
	private GpsUtilProxy gpsUtilProxy;

	public TourGuideService(RewardsService rewardsService) {

		this.rewardsService = rewardsService;

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}

	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	public VisitedLocationBean getUserLocation(User user) {
		VisitedLocationBean visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<ProviderBean> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<ProviderBean> providers = tripPricerProxy.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocationBean trackUserLocation(User user) {
		VisitedLocationBean visitedLocation = gpsUtilProxy.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public void trackAllUsersLocations(List<User> users) {

		final ExecutorService executorTourGuideService = Executors.newFixedThreadPool(100);

		List<Callable<VisitedLocationBean>> callables = new ArrayList<Callable<VisitedLocationBean>>();

		for (User user : users) {
			callables.add(new Callable<VisitedLocationBean>() {
				public VisitedLocationBean call() throws Exception {
					VisitedLocationBean visitedLocation = gpsUtilProxy.getUserLocation(user.getUserId());
					user.addToVisitedLocations(visitedLocation);
					rewardsService.calculateRewards(user);
					return visitedLocation;

				}
			});
		}

		try {
			List<Future<VisitedLocationBean>> futures = executorTourGuideService.invokeAll(callables);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			executorTourGuideService.shutdown();
		}

	}

//	public List<AttractionBean> getNearByAttractions(VisitedLocationBean visitedLocationBean) {
//		List<AttractionBean> nearbyAttractions = new ArrayList<>();
//		for (AttractionBean attraction : gpsUtilProxy.getAllAttractions()) {
//			if (rewardsService.isWithinAttractionProximity(attraction, visitedLocationBean.location)) {
//				nearbyAttractions.add(attraction);
//			}
//		}
//
//		return nearbyAttractions;
//	}

	public List<NearAttraction> get5ClosestAttractions(User user, VisitedLocationBean visitedLocationBean) {
		double attractionBeanDistance;
		HashMap<AttractionBean, Double> closestAttractions = new HashMap<AttractionBean, Double>();

		for (AttractionBean attractionBean : gpsUtilProxy.getAllAttractions()) {
			attractionBeanDistance = rewardsService.getDistance(attractionBean, visitedLocationBean.location);
			closestAttractions.put(attractionBean, attractionBeanDistance);
		}

		List<AttractionBean> closestNAttractions = topNAttractions(closestAttractions, 5);

		List<NearAttraction> nearNClosestAttraction = new ArrayList<>();

		for (AttractionBean attraction : closestNAttractions) {
			NearAttraction nearAttraction = new NearAttraction();

			nearAttraction.setAttractionName(attraction.attractionName);
			nearAttraction.setAttractionLatitude(attraction.latitude);
			nearAttraction.setAttractionLongitude(attraction.longitude);
			nearAttraction.setUserLocation(visitedLocationBean.location);
			nearAttraction
					.setAttractionDistance(rewardsService.getDistance(attraction, nearAttraction.getUserLocation()));
			nearAttraction.setAttractionRewardpoints(rewardsService.getRewardPoints(attraction, user));

			nearNClosestAttraction.add(nearAttraction);

		}

		return nearNClosestAttraction;
	}

	public static List<AttractionBean> topNAttractions(final HashMap<AttractionBean, Double> map, int n) {
		PriorityQueue<AttractionBean> topN = new PriorityQueue<AttractionBean>(n, new Comparator<AttractionBean>() {
			public int compare(AttractionBean s1, AttractionBean s2) {
				return Double.compare(map.get(s1), map.get(s2));
			}
		});

		for (AttractionBean key : map.keySet()) {
			if (topN.size() < n)
				topN.add(key);
			else if (map.get(topN.peek()) < map.get(key)) {
				topN.poll();
				topN.add(key);
			}
		}
		return (List) Arrays.asList(topN.toArray());
	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	public void updateUserPreferences(String userName, UserPreferences userPreferences) {
		User userToUpdate = getUser(userName);
		userToUpdate.setUserPreferences(userPreferences);

	}

	public Map<String, LocationBean> getAllUserLocations() {
		Map<String, LocationBean> allLocations = new HashMap<String, LocationBean>();

		List<User> users = getAllUsers();

		for (User user : users) {
			allLocations.put(user.getUserId().toString(), user.getLastVisitedLocation().location);
		}

		return allLocations;
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(),
					new LocationBean(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}