package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemUtilityMatrix {
	
	private Map<String, Map<String, Double>> itemsUtilMatrix;
	private List<Rating> ratingsSet;
	private List<String> businessIds;
	private Map<String, Double> averagesMap;
	private double average;
	
	
	// Only one constructor will be used in this class. For one more possible constructor see 
	public ItemUtilityMatrix(List<String> businessIds, List<Rating> ratingsSet) {
		
		this.businessIds = businessIds;
		this.ratingsSet = ratingsSet;
		
		setItemsUtilMatrix();
		setAverage();
		setAveragesMap();
	}

	
	private void setItemsUtilMatrix() {
		
		this.itemsUtilMatrix = new HashMap<>();
		

		// Group the ratingsSet by businessId.
		Map<String, List<Rating>> groupedBusIdMap = this.ratingsSet.parallelStream()
														.collect(Collectors.groupingBy(Rating::getBusinessId));
		
		// The utility matrix needs to contain all the businessIds in the data sample.
		// The ratingsSet list of <Rating> objects is a train set, so it might not contain all the businessId
		// values present in the entire data set. If there are any elements in the businessIds list, that are not
		// part of the resulted grouped map, the missing businessId strings need to be added to the map as keys,
		// with a value of an empty list of <Rating> objects.
		
		// ** First check if there are any string elements in the businessIds list which are not part of the 
		// groupedBusIdMap keySet.
		// This set contains all the elements in the businessIds list.
		Set<String> busIds = new HashSet<>(businessIds);
		// Compute the set difference between busIds set and the groupedBusIdMap keySet, so only the elements that 
		// are not part of the groupedBusIdmap keySet remain in the busIds set.
		busIds.removeAll(groupedBusIdMap.keySet());
		//If busIds set contains any elements after the difference operation, add to the grouped map the elements in 
		// busIds as keys with an empty <Rating> list as values.
		Map<String, List<Rating>>noRatings = new HashMap<>();
		if(!busIds.isEmpty())
		{
			noRatings = busIds.parallelStream()
					.collect(Collectors.toMap(busId -> busId, busId -> new ArrayList<Rating>()));
		}
		// Add noRatings map to the groupedBusIdMap.
		 groupedBusIdMap.putAll(noRatings);
		 
		 // Fill up this.itemsUtilMatrix.
		//Set<Map.Entry<String, List<Rating>>> s = groupedBusIdMap.entrySet(); 
		
		this.itemsUtilMatrix 
				= groupedBusIdMap.entrySet().stream()
						.collect(Collectors.toMap(entry -> entry.getKey(),entry -> entry.getValue()
											.stream()
						.collect(Collectors.toMap(rating -> rating.getUserId(), rating -> rating.getStars()))));
	}
	
	private void setAverage(){
		
		double sum = this.ratingsSet.stream().map(Rating::getStars).reduce(0.0,(Double a, Double b) -> a+b);
		this.average = sum / this.ratingsSet.size();
	}
	
	private void setAveragesMap(){
		
		this.averagesMap = new HashMap<String,Double>();
		
		this.businessIds.forEach(busId -> {
			
			Map<String,Double> vec = this.itemsUtilMatrix.get(busId);
			// If the business does not have any ratings the average is the sample's global average.
			if(vec.size()== 0) {
				this.averagesMap.put(busId, this.average);
			}
			else {
				
				double sum = vec.values().stream().reduce(0.0,(Double a, Double b) -> a+b);
				this.averagesMap.put(busId, sum / vec.size());
			}
		});
	}
	
	public List<String> getBusinessIds(){
		return this.businessIds;
	}
	
	public Map<String, Map<String, Double>> getUtilMatrix(){
		return this.itemsUtilMatrix;
	}
	
	public double getAverage() {
		return this.average;
	}
	
	public Map<String, Double> getAveragesMap(){
		return this.averagesMap;
	}
}
