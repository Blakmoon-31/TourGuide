package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.money.Monetary;

import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tourGuide.beans.LocationBean;
import tourGuide.beans.ProviderBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.domain.NearAttraction;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class TestTourGuideService {

	@Autowired
	private TourGuideService tourGuideService;

	@BeforeAll
	public void configTest() {
		Locale.setDefault(Locale.US);
	}

	@Test
	public void getUserLocation() {
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user);
		tourGuideService.tracker.stopTracking();

		assertThat(visitedLocation.userId.equals(user.getUserId())).isTrue();
	}

	@Test
	public void addUser() {
		RewardsService rewardsService = new RewardsService();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);

		User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user1);
		tourGuideService.addUser(user2);

		User retrivedUser1 = tourGuideService.getUser(user1.getUserName());
		User retrivedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();

		assertThat(user1).isEqualTo(retrivedUser1);
		assertThat(user2).isEqualTo(retrivedUser2);
	}

	@Test
	public void getAllUsers() {
		RewardsService rewardsService = new RewardsService();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);

		User user1 = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user1);
		tourGuideService.addUser(user2);

		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();

		assertThat(allUsers).contains(user1);
		assertThat(allUsers).contains(user2);
	}

	@Test
	public void updateUserPreferences() {
		RewardsService rewardsService = new RewardsService();
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		tourGuideService.addUser(user);

		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setAttractionProximity(100);
		userPreferences.setCurrency(Monetary.getCurrency("USD"));
		userPreferences.setLowerPricePoint(Money.of(10, userPreferences.getCurrency()));
		userPreferences.setHighPricePoint(Money.of(150, userPreferences.getCurrency()));
		userPreferences.setNumberOfAdults(2);
		userPreferences.setNumberOfChildren(3);
		userPreferences.setTripDuration(7);
		userPreferences.setTicketQuantity(5);

		tourGuideService.updateUserPreferences("jon", userPreferences);

		User retrivedUser = tourGuideService.getUser(user.getUserName());

		tourGuideService.tracker.stopTracking();

		assertThat(retrivedUser.getUserPreferences().getNumberOfAdults()
				+ retrivedUser.getUserPreferences().getNumberOfChildren()).isEqualTo(5);

	}

	@Test
	public void trackUser() {
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user);

		tourGuideService.tracker.stopTracking();

		assertThat(user.getUserId()).isEqualTo(visitedLocation.userId);
	}

	@Test
	public void getNearbyAttractions() {
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocationBean visitedLocation = tourGuideService.trackUserLocation(user);

		List<NearAttraction> nearAttractions = tourGuideService.get5ClosestAttractions(user, visitedLocation);

		tourGuideService.tracker.stopTracking();

		assertThat(nearAttractions.size()).isEqualTo(5);
	}

	@Test
	public void getAllUserLocations() {
		RewardsService rewardsService = new RewardsService();
		InternalTestHelper.setInternalUserNumber(3);
		TourGuideService tourGuideService = new TourGuideService(rewardsService);

		Map<String, LocationBean> allUserLocations = tourGuideService.getAllUserLocations();

		assertThat(allUserLocations.size()).isEqualTo(3);
	}

	@Test
	public void getTripDeals() {
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		List<ProviderBean> providers = tourGuideService.getTripDeals(user);

		tourGuideService.tracker.stopTracking();

		assertThat(providers.size()).isEqualTo(5);
	}

}