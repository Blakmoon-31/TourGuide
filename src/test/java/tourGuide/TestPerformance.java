package tourGuide;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
public class TestPerformance {

	private int numberOfUsers = 100000;

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

	/*
	 * A note on performance improvements:
	 * 
	 * The number of users generated for the high volume tests can be easily
	 * adjusted via this method:
	 * 
	 * InternalTestHelper.setInternalUserNumber(100000);
	 * 
	 * 
	 * These tests can be modified to suit new solutions, just as long as the
	 * performance metrics at the end of the tests remains consistent.
	 * 
	 * These are performance metrics that we are trying to hit:
	 * 
	 * highVolumeTrackLocation: 100,000 users within 15 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 *
	 * highVolumeGetRewards: 100,000 users within 20 minutes:
	 * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
	 * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */

	@Test
	public void highVolumeTrackLocation() {
		// Users should be incremented up to 100,000, and test finishes within 15
		// minutes
		InternalTestHelper.setInternalUserNumber(numberOfUsers);
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);

		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		tourGuideService.trackAllUsersLocations(allUsers);

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("User's visited locations : " + allUsers.get(0).getVisitedLocations().size()
				+ " and user's rewards points : " + allUsers.get(0).getUserRewards().size());

		System.out.println("highVolumeTrackLocation: Time Elapsed: "
				+ TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");

		assertThat(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())).isTrue();
	}

	@Test
	public void highVolumeGetRewards() {
		// Users should be incremented up to 100,000, and test finishes within 20
		// minutes

		InternalTestHelper.setInternalUserNumber(numberOfUsers);

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		TourGuideService tourGuideService = new TourGuideService(gpsUtilProxy, rewardsService);

		AttractionBean attraction = gpsUtilProxy.getAllAttractions().get(0);
		List<User> allUsers = new ArrayList<>();
		allUsers = tourGuideService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocationBean(u.getUserId(), attraction, new Date())));

		rewardsService.calculateAllUsersRewards(allUsers);

		for (User user : allUsers) {
			assertThat(user.getUserRewards().size() > 0).isTrue();
		}

		stopWatch.stop();
		tourGuideService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");

		assertThat(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())).isTrue();
	}

}