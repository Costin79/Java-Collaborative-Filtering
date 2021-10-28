package test;

public class PredictedRating {
	
	private String businessId;
	private String userId;
	private double receivedRating;
	private double predictedRating;
	
	public PredictedRating(String businessId, String userId, double receivedRating, double predictedRating) {
		this.businessId = businessId;
		this.userId = userId;
		this.receivedRating = receivedRating;
		this.predictedRating = predictedRating;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public double getReceivedRating() {
		return receivedRating;
	}

	public void setReceivedRating(double receivedRating) {
		this.receivedRating = receivedRating;
	}

	public double getPredictedRating() {
		return predictedRating;
	}

	public void setPredictedRating(double predictedRating) {
		this.predictedRating = predictedRating;
	}

	@Override
	public String toString() {
		return "PredictedRating [businessId=" + businessId + ", userId=" + userId + ", receivedRating=" + receivedRating
				+ ", predictedRating=" + predictedRating + "]";
	}
	
	

}
