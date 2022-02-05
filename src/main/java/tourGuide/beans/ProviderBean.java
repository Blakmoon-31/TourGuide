package tourGuide.beans;

import java.util.UUID;

/**
 * Class replacing the former object Provider from the library TripPricer.
 *
 */
public class ProviderBean {

	public UUID tripId;

	public String name;

	public double price;

	public UUID getTripId() {
		return tripId;
	}

	public void setTripId(UUID tripId) {
		this.tripId = tripId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public ProviderBean() {
		super();
	}

	public ProviderBean(UUID tripId, String name, double price) {
		super();
		this.tripId = tripId;
		this.name = name;
		this.price = price;
	}

}
