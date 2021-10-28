package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class UserUtilityMatrix {
	
	private Map<String, Map<String, Double>> usersUtilMatrix;
	private List<Rating> ratingsSet;
	private List<String> userIds;
	private Map<String, Double> averagesMap;
	private double average;
	
	
	// Only one constructor will be used in this class. For one more possible constructor see 
	public UserUtilityMatrix(List<String> userIds, List<Rating> ratingsSet) {
		
		this.userIds = userIds;
		this.ratingsSet = ratingsSet;
		
		setUsersUtilMatrix();
		setAverage();
		setAveragesMap();
	}

	
	private void setUsersUtilMatrix() {
		
		this.usersUtilMatrix = new HashMap<>();
		

		// Group the ratingsSet by userId.
		Map<String, List<Rating>> groupedUsersIdMap = this.ratingsSet.parallelStream()
														.collect(Collectors.groupingBy(Rating::getUserId));
		
		Set<String> usIds = new HashSet<>(userIds);
		
		usIds.removeAll(groupedUsersIdMap.keySet());
		
		Map<String, List<Rating>>noRatings = new HashMap<>();
		if(!usIds.isEmpty())
		{
			noRatings = usIds.parallelStream()
					.collect(Collectors.toMap(usId -> usId, usId -> new ArrayList<Rating>()));
		}
		// Add noRatings map to the groupedBusIdMap.
		 groupedUsersIdMap.putAll(noRatings);
		 
		 // Fill up this.itemsUtilMatrix.
		//Set<Map.Entry<String, List<Rating>>> s = groupedBusIdMap.entrySet(); 
		
		this.usersUtilMatrix 
				= groupedUsersIdMap.entrySet().stream()
						.collect(Collectors.toMap(entry -> entry.getKey(),entry -> entry.getValue()
											.stream()
						.collect(Collectors.toMap(rating -> rating.getBusinessId(), rating -> rating.getStars()))));
	}
	
	private void setAverage(){
		
		double sum = this.ratingsSet.stream().map(Rating::getStars).reduce(0.0,(Double a, Double b) -> a+b);
		this.average = sum / this.ratingsSet.size();
	}
	
	private void setAveragesMap(){
		
		this.averagesMap = new HashMap<String,Double>();
		
		this.userIds.forEach(usId -> {
			
			Map<String,Double> vec = this.usersUtilMatrix.get(usId);
			// If the business does not have any ratings the average is the sample's global average.
			if(vec.size()== 0) {
				this.averagesMap.put(usId, this.average);
			}
			else {
				
				double sum = vec.values().stream().reduce(0.0,(Double a, Double b) -> a+b);
				this.averagesMap.put(usId, sum / vec.size());
			}
		});
	}
	
	
	public Map<String, Map<String, Double>> getUtilMatrix(){
		return this.usersUtilMatrix;
	}
	
	public double getAverage() {
		return this.average;
	}
	
	public Map<String, Double> getAveragesMap(){
		return this.averagesMap;
	}

	
}




















