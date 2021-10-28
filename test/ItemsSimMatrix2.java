package test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class ItemsSimMatrix2 {

	private Map<String, Map<String,Double>> matrix;
	
	public ItemsSimMatrix2(ItemUtilityMatrix utilityMatrix){
		
		setMatrix(utilityMatrix);
	}
	
	
	private void setMatrix(ItemUtilityMatrix utilityMatrix){
		
		//System.out.println("Similarity matrix calculation started");
		
		// Put the utilityMatrix list into an array.
		String [] data =
				utilityMatrix.getBusinessIds()
							 .toArray(new String[utilityMatrix.getBusinessIds().size()]);
		
		// Create a two dimensional array of doubles which will temporarily hold the similarities values.
		// Each row and column represents the index of a <LocationData> object in the utilMatrix array.
		double[][] m = new double[data.length][data.length];
		double similarity = 0;
		
		for(int i = 0; i < m.length; i++)
    	{
			// Get the vector of ratings map for the <LocationData> object at position 'i',
			// from the UtilityMatrix object passed in the constructor.
			Map<String,Double> vectorAti = utilityMatrix.getUtilMatrix().get(data[i]);
    		
    		for(int j = i; j < m[i].length; j++)
    		{
    			if(i == j) {m[i][j] = 1;}
    			else
    			{
    				// Get the vector of ratings map for the <LocationData> object at position 'j',
    				// from the UtilityMatrix object passed in the constructor.
    				Map<String,Double> vectorAtj = utilityMatrix.getUtilMatrix()
    															.get(data[j]);
    	    		
    				similarity = cosineSimilarity(vectorAti, vectorAtj);
    				
    				//System.out.println(i + " " + j + " " + similarity);
    				m[i][j] = similarity;
    				m[j][i] = similarity;
    			}
    		}// End of j loop.
    	}// End of i loop.
		
    	//System.out.println("The matrix calculations are over.");
		
    	// Instantiate the matrix map.
		this.matrix = new HashMap<>();
		
		// Put the values from the two dimensional array into the matrix map. 
		for(int i = 0; i < m.length; i++) {
			
			// The row businessId. 
			String busId = data[i];
			// Create a map to store the busId neighbors with their similarities values.
			Map<String, Double> neighbors = new HashMap<>();
			
			for(int j = 0; j < m[i].length; j++) {
				
				if(i != j && m[i][j] > 0) {
					neighbors.put(data[j], m[i][j]);
				}
			}// End of j loop.
			this.matrix.put(busId, neighbors);
		}// End of i loop.
		
		//System.out.println("$$$$$$$$$$$$$$$$");
		//System.out.println("Similarity matrix calculations ended");
	}// End of setSimMatrix.
	
	private double cosineSimilarity(Map<String, Double>vec1, Map<String, Double>vec2) {
		
		//Check for zero so there won't be a division by zero.
		if(vec1.size() == 0 || vec2.size() == 0) {
			return 0;
		}
		return dividend(vec1, vec2) / divisor(vec1, vec2);
	}
	
	private double dividend(Map<String,Double> vec1, Map<String, Double> vec2) {
		
		double dotProduct = 0;
		
		// Do the intersection between the key sets of the maps in order to isolate the common userIds.
		Set<String> intersection = new HashSet<>(vec1.keySet());
		intersection.retainAll(vec2.keySet());
		
		// Calculate the dot product of the common userId.
		dotProduct = intersection.parallelStream().map(s -> vec1.get(s) * vec2.get(s))
													.reduce(0.0, (Double a, Double b) -> a+b);
		
		return dotProduct;
	}
	
	private double divisor(Map<String, Double>vec1, Map<String, Double>vec2){
		
		//Extract the values out of each map.
		Collection<Double> v1 = vec1.values();
		Collection<Double> v2 = vec2.values();
		return magnitude(v1) * magnitude(v2);
	}
	
	private double magnitude(Collection<Double>vec) {
		
		double sumOfSquares = vec.stream().map(v -> v * v)
											.reduce(0.0, (Double a, Double b)-> a+b);
		
		return Math.sqrt(sumOfSquares);
	}
	// End of setMatrix helper methods.
	
	public Map<String, Map<String,Double>> getMatrix(){
		return this.matrix;
	}
}
