package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsUtilProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserReward;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class TestRewardsService {

	@Autowired
	private GpsUtilProxy gpsUtilProxy;

	@Autowired
	private TourGuideService tourGuideService;

	@Autowired
	private RewardsService rewardsService;

	@BeforeAll
	public void configTest() {
		Locale.setDefault(Locale.US);
	}

	@Test
	public void userGetRewards() {
		InternalTestHelper.setInternalUserNumber(0);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		AttractionBean attraction = gpsUtilProxy.getAllAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();

		assertThat(userRewards.size()).isEqualTo(1);
	}

	@Test
	public void isWithinAttractionProximity() {
		AttractionBean attraction = gpsUtilProxy.getAllAttractions().get(0);

		assertThat(rewardsService.isWithinAttractionProximity(attraction, attraction)).isTrue();
	}

	@Test
	public void nearAllAttractions() {
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);

		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));
		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		rewardsService.setProximityBuffer(10);

		assertThat(gpsUtilProxy.getAllAttractions().size()).isEqualTo(userRewards.size());
	}

}