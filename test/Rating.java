package test;

public class Rating {
	
	private String businessId;
	private String userId;
	private double stars;
	
	//public Rating() {}
	
	public Rating (String userId, String businessId, double stars)
	{
		this.setBusinessId(businessId);
		this.setUserId(userId);
		this.setStars(stars);
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

	public double getStars() {
		return stars;
	}

	public void setStars(double stars) {
		this.stars = Math.floor(stars);
	}
	
	public String toString()
	{
		return "userId : " +  this.userId + " businessId:  " + this.businessId + " stars: " + this.stars;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((businessId == null) ? 0 : businessId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Rating other = (Rating) obj;
		if (businessId == null) {
			if (other.businessId != null)
				return false;
		} else if (!businessId.equals(other.businessId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
	

}
