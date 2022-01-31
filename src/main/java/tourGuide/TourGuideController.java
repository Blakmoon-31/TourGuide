package tourGuide;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tourGuide.beans.LocationBean;
import tourGuide.beans.ProviderBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.domain.NearAttraction;
import tourGuide.dto.UserPreferencesDto;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

	@RequestMapping("/")
	public String index() {
		return "Greetings from TourGuide!";
	}

	// For tests, get generated user's names
	@RequestMapping("/users")
	public List<String> getUsersNames() {
		List<User> users = tourGuideService.getAllUsers();
		List<String> names = new ArrayList<String>();

		for (User user : users) {
			names.add(user.getUserName());
		}

		return names;
	}

	@RequestMapping("/getLocation")
	public LocationBean getLocation(@RequestParam String userName) {
		VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return visitedLocation.location;
	}

	// TODO: Change this method to no longer return a List of Attractions.
	// Instead: Get the closest five tourist attractions to the user - no matter how
	// far away they are.
	// Return a new JSON object that contains:
	// Name of Tourist attraction,
	// Tourist attractions lat/long,
	// The user's location lat/long,
	// The distance in miles between the user's location and each of the
	// attractions.
	// The reward points for visiting each Attraction.
	// Note: Attraction reward points can be gathered from RewardsCentral
	@RequestMapping("/getNearbyAttractions")
	public List<NearAttraction> getNearbyAttractions(@RequestParam String userName) {
		VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		User user = getUser(userName);
		return tourGuideService.get5ClosestAttractions(user, visitedLocation);
	}

	@RequestMapping("/getRewards")
	public List<UserReward> getRewards(@RequestParam String userName) {
		return tourGuideService.getUserRewards(getUser(userName));
	}

	@RequestMapping("/getAllCurrentLocations")
	public Map<String, LocationBean> getAllCurrentLocations() {
		// TODO: Get a list of every user's most recent location as JSON
		// - Note: does not use gpsUtil to query for their current location,
		// but rather gathers the user's current location from their stored location
		// history.
		//
		// Return object should be the just a JSON mapping of userId to Locations
		// similar to:
		// {
		// "019b04a9-067a-4c76-8817-ee75088c3822":
		// {"longitude":-48.188821,"latitude":74.84371}
		// ...
		// }

		Map<String, LocationBean> allUserLocations = tourGuideService.getAllUserLocations();

		return allUserLocations;
	}

	@RequestMapping("/getTripDeals")
	public List<ProviderBean> getTripDeals(@RequestParam String userName) {
		List<ProviderBean> providers = tourGuideService.getTripDeals(getUser(userName));
		return providers;
	}

	private User getUser(String userName) {
		return tourGuideService.getUser(userName);
	}

	@RequestMapping("/updateUserPreferences")
	public User updateUserPreferences(@RequestParam String userName,
			@RequestBody UserPreferencesDto userPreferencesDto) {

		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setAttractionProximity(userPreferencesDto.getAttractionProximity());
		userPreferences.setCurrency(Monetary.getCurrency(userPreferencesDto.getCurrency()));
		userPreferences
				.setLowerPricePoint(Money.of(userPreferencesDto.getLowerPricePoint(), userPreferences.getCurrency()));
		userPreferences
				.setHighPricePoint(Money.of(userPreferencesDto.getHighPricePoint(), userPreferences.getCurrency()));
		userPreferences.setTicketQuantity(userPreferencesDto.getTicketQuantity());
		userPreferences.setAttractionProximity(userPreferencesDto.getAttractionProximity());
		userPreferences.setNumberOfAdults(userPreferencesDto.getNumberOfAdults());
		userPreferences.setNumberOfChildren(userPreferencesDto.getNumberOfChildren());

		tourGuideService.updateUserPreferences(userName, userPreferences);
		User user = tourGuideService.getUser(userName);
		return user;
	}

}