package tourGuide.proxies;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tourGuide.beans.ProviderBean;

//Container: trippricer-microservice
/**
 * Proxy used to access the microservice TripPricer. URL - The url use the name
 * of the container (trippricer-microservice) to communicate between containers.
 * Use localhost if application is not run from a container.
 */
@FeignClient(name = "TripPricer-Microservice", url = "trippricer-microservice:8083")
public interface TripPricerProxy {

	/**
	 * Obtains a list of providers for an attraction with an offer corresponding to
	 * the user's preferences.
	 * 
	 * @param apiKey                   - The API key of the TourGuide application
	 * @param attraction               - The id of the attraction
	 * @param adults                   - Number of adults (user's preference)
	 * @param children                 - Number of children (user's preference)
	 * @param duration                 - Duration of the trip (user's preference)
	 * @param cumulatativeRewardPoints - Cumulative RewardsPoints for the user
	 * 
	 * @return - A list of PriverdBeans objects
	 */
	@GetMapping("/providers")
	List<ProviderBean> getPrice(@RequestParam("apiKey") String apiKey, @RequestParam("attraction") UUID attraction,
			@RequestParam("adults") int adults, @RequestParam("children") int children,
			@RequestParam("duration") int duration,
			@RequestParam("cumulatativeRewardPoints") int cumulatativeRewardPoints);

}
