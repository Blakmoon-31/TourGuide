package tourGuide.proxies;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//Container: rewardcentral-microservice
@FeignClient(name = "RewardCentral-Microservice", url = "rewardcentral-microservice:8082")
public interface RewardCentralProxy {

	@GetMapping("/getRewardPoints")
	int getAttractionRewardPoints(@RequestParam("attractionId") UUID attractionId, @RequestParam("userId") UUID userId);
}
