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
import tourGuide.domain.NearbyAttraction;
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

	private int numberOfNearbyAttractions = 5;

	@Autowired
	private TripPricerProxy tripPricerProxy;

	@Autowired
	private GpsUtilProxy gpsUtilProxy;

	public TourGuideService(GpsUtilProxy gpsUtilProxy, RewardsService rewardsService) {

		this.gpsUtilProxy = gpsUtilProxy;
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

	/**
	 * Obtains the location of a user from the microservice GpsUtil.
	 * 
	 * @param user - A User object containging the user to track
	 * 
	 * @return - A VisitedLocationBean object
	 */
	public VisitedLocationBean trackUserLocation(User user) {
		VisitedLocationBean visitedLocation = gpsUtilProxy.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	/**
	 * Obtains the location of a list of users from the microservice GpsUtil. Uses a
	 * pool of 100 threads.
	 * 
	 * @param user - An list of User objects containging the users to track
	 */
	public void trackAllUsersLocations(List<User> users) {

		final ExecutorService executorTourGuideService = Executors.newFixedThreadPool(100);

		List<Callable<VisitedLocationBean>> callables = new ArrayList<Callable<VisitedLocationBean>>();

		for (User user : users) {
			callables.add(new Callable<VisitedLocationBean>() {
				public VisitedLocationBean call() throws Exception {
					VisitedLocationBean visitedLocation = gpsUtilProxy.getUserLocation(user.getUserId());
					user.addToVisitedLocations(visitedLocation);
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

		rewardsService.calculateAllUsersRewards(users);

	}

	/**
	 * Obtains a list of attraction nearby the user's location. The number of
	 * attractions is set by "numberOfNearbyAttractions".
	 * 
	 * @param user                - The user
	 * @param visitedLocationBean - The location of the user
	 * 
	 * @return - A list of NearbyAttraction objects
	 */
	public List<NearbyAttraction> getNearbyAttractions(User user, VisitedLocationBean visitedLocationBean) {
		double attractionBeanDistance;
		HashMap<AttractionBean, Double> closestAttractions = new HashMap<AttractionBean, Double>();

		for (AttractionBean attractionBean : gpsUtilProxy.getAllAttractions()) {
			attractionBeanDistance = rewardsService.getDistance(attractionBean, visitedLocationBean.location);
			closestAttractions.put(attractionBean, attractionBeanDistance);
		}

		List<AttractionBean> closestNAttractions = topNAttractions(closestAttractions, numberOfNearbyAttractions);

		List<NearbyAttraction> nearbyNClosestAttraction = new ArrayList<>();

		for (AttractionBean attraction : closestNAttractions) {
			NearbyAttraction nearbyAttraction = new NearbyAttraction();

			nearbyAttraction.setAttractionName(attraction.attractionName);
			nearbyAttraction.setAttractionLatitude(attraction.latitude);
			nearbyAttraction.setAttractionLongitude(attraction.longitude);
			nearbyAttraction.setUserLocation(visitedLocationBean.location);
			nearbyAttraction
					.setAttractionDistance(rewardsService.getDistance(attraction, nearbyAttraction.getUserLocation()));
			nearbyAttraction.setAttractionRewardpoints(rewardsService.getRewardPoints(attraction, user));

			nearbyNClosestAttraction.add(nearbyAttraction);

		}

		return nearbyNClosestAttraction;
	}

	/**
	 * Determines the list of the N closests attractions, that is those at the
	 * smallest distance.
	 * 
	 * @param map - The list of attractions and distances to sort
	 * @param n   - The number of attractions to keep
	 * 
	 * @return - The list of the N closests AttractionBeans
	 */
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

	/**
	 * Update the user's preferences
	 * 
	 * @param userName        - The name of the user to update
	 * @param userPreferences - A UserPreferences object with data to update
	 */
	public void updateUserPreferences(String userName, UserPreferences userPreferences) {
		User userToUpdate = getUser(userName);
		userToUpdate.setUserPreferences(userPreferences);

	}

	/**
	 * Obtains a list of the last location of all users.
	 * 
	 * @return - A list with user's id and their locations
	 */
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