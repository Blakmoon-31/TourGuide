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
import tourGuide.domain.NearbyAttraction;
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

	/**
	 * For test purpose only, get the list of generated user's names.
	 * 
	 * @return - A list of strings containing the user's names
	 */
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

	/**
	 * Get the closest tourist attractions to the user - no matter how far away they
	 * are.
	 * 
	 * @param userName - The name of the specific user
	 * 
	 * @return - A JSON object from a list of NearbyAttraction that contains : Name
	 *         of Tourist attraction, Tourist attractions lat/long, The user's
	 *         location lat/long, The distance in miles between the user's location
	 *         and each of the attractions, The reward points for visiting each
	 *         Attraction
	 */
	@RequestMapping("/getNearbyAttractions")
	public List<NearbyAttraction> getNearbyAttractions(@RequestParam String userName) {
		VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		User user = getUser(userName);
		return tourGuideService.getNearbyAttractions(user, visitedLocation);
	}

	@RequestMapping("/getRewards")
	public List<UserReward> getRewards(@RequestParam String userName) {
		return tourGuideService.getUserRewards(getUser(userName));
	}

	/**
	 * Get a list of every user's most recent location as JSON. Does not use gpsUtil
	 * to query for their current location, but gathers the user's last location
	 * from their stored location history.
	 * 
	 * @return - A JSON object containing the list of users and their location
	 */
	@RequestMapping("/getAllCurrentLocations")
	public Map<String, LocationBean> getAllCurrentLocations() {

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

	/**
	 * Update preferences of a user. Use a DTO object to perform JSON
	 * serialization/deserialization without having to manage currencies fields.
	 * 
	 * @param userName           - The name of the user to update
	 * @param userPreferencesDto - A DTO object
	 * 
	 * @return - A User object updated
	 */
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