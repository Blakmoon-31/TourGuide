package tourGuide.proxies;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Proxy used to access the microservice RewardCentral. URL - The url use the
 * name of the container (rewardcentral-microservice) to communicate between
 * containers. Use localhost if application is not run from a container.
 */
@FeignClient(name = "RewardCentral-Microservice", url = "rewardcentral-microservice:8082")
public interface RewardCentralProxy {

	/**
	 * Obtains the RewardsPoints gained by a specific user for a specific
	 * attraction.
	 * 
	 * @param attractionId - The id of the attraction
	 * @param userId       - The id of the user
	 * 
	 * @return - An integer representing the RewardsPoints calculated by the
	 *         microservice RewardCentral
	 */
	@GetMapping("/getRewardPoints")
	int getAttractionRewardPoints(@RequestParam("attractionId") UUID attractionId, @RequestParam("userId") UUID userId);
}
