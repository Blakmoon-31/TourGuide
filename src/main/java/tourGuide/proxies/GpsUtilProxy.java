package tourGuide.proxies;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;

/**
 * Proxy used to access the microservice GpsUtil. URL - The url use the name of
 * the container (gpsutil-microservice) to communicate between containers. Use
 * localhost if application is not run from a container.
 */
@FeignClient(name = "GpsUtil-Microservice", url = "gpsutil-microservice:8081")
public interface GpsUtilProxy {

	/**
	 * Obtains the list of attractions from GpsUtil.
	 * 
	 * @return - A list of AttractionBean objects
	 */
	@GetMapping("/getAttractions")
	List<AttractionBean> getAllAttractions();

	/**
	 * Obtains the current location of a specific user.
	 * 
	 * @param userId - The id of the user
	 * 
	 * @return - A VisitedLocation object
	 */
	@GetMapping("/getUserLocation/{id}")
	VisitedLocationBean getUserLocation(@PathVariable("id") UUID userId);
}