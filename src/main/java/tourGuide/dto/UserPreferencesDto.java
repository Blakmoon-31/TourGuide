package tourGuide.dto;

public class UserPreferencesDto {

	private int attractionProximity = 0;
	private String currency = "";
	private int lowerPricePoint = 0;
	private int highPricePoint = 0;
	private int tripDuration = 0;
	private int ticketQuantity = 0;
	private int numberOfAdults = 0;
	private int numberOfChildren = 0;

	public UserPreferencesDto() {
	}

	public void setAttractionProximity(int attractionProximity) {
		this.attractionProximity = attractionProximity;
	}

	public int getAttractionProximity() {
		return attractionProximity;
	}

	public int getLowerPricePoint() {
		return lowerPricePoint;
	}

	public void setLowerPricePoint(int lowerPricePoint) {
		this.lowerPricePoint = lowerPricePoint;
	}

	public int getHighPricePoint() {
		return highPricePoint;
	}

	public void setHighPricePoint(int highPricePoint) {
		this.highPricePoint = highPricePoint;
	}

	public int getTripDuration() {
		return tripDuration;
	}

	public void setTripDuration(int tripDuration) {
		this.tripDuration = tripDuration;
	}

	public int getTicketQuantity() {
		return ticketQuantity;
	}

	public void setTicketQuantity(int ticketQuantity) {
		this.ticketQuantity = ticketQuantity;
	}

	public int getNumberOfAdults() {
		return numberOfAdults;
	}

	public void setNumberOfAdults(int numberOfAdults) {
		this.numberOfAdults = numberOfAdults;
	}

	public int getNumberOfChildren() {
		return numberOfChildren;
	}

	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}