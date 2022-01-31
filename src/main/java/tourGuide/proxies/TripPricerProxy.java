package tourGuide.proxies;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import tourGuide.beans.ProviderBean;

//Container: trippricer-microservice
@FeignClient(name = "TripPricer-Microservice", url = "trippricer-microservice:8083")
public interface TripPricerProxy {

	@GetMapping("/providers")
	List<ProviderBean> getPrice(@RequestParam("apiKey") String apiKey, @RequestParam("attraction") UUID attraction,
			@RequestParam("adults") int adults, @RequestParam("children") int children,
			@RequestParam("duration") int duration,
			@RequestParam("cumulatativeRewardPoints") int cumulatativeRewardPoints);

}
