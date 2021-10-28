package test;

public class CommonBusiness {
	
	private String busId;
	private double similarity; // The similarity value between the busId and the parameter businessId.
	private double stars; // The rating received by busId by the parameter userId.
	
	public CommonBusiness(String busId, double similarity, double stars)
	{
		this.busId = busId;
		this.similarity = similarity;
		this.stars = stars;
	}

	public String getBusId() {
		return this.busId;
	}

	public void setBusId(String busId) {
		this.busId = busId;
	}

	public double getSimilarity() {
		return this.similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public double getStars() {
		return this.stars;
	}

	public void setStars(double stars) {
		this.stars = stars;
	}
	
	public String toString()
	{
		return "Common Rating " + this.busId + " " + this.similarity + " " + this.stars;
	}

}
