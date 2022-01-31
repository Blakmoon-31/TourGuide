package tourGuide.proxies;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;

//Container: gpsutil-microservice
@FeignClient(name = "GpsUtil-Microservice", url = "gpsutil-microservice:8081")
public interface GpsUtilProxy {

	@GetMapping("/getAttractions")
	List<AttractionBean> getAllAttractions();

	@GetMapping("/getUserLocation/{id}")
	VisitedLocationBean getUserLocation(@PathVariable("id") UUID userId);
}