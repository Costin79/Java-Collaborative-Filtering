package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemsAdjSimMatrix {
	
	private Map<String, Map<String,Double>> matrix;
	
	public ItemsAdjSimMatrix(ItemUtilityMatrix itemUtilMatrix, UserUtilityMatrix userUtilMatrix ){
		
		setMatrix(itemUtilMatrix, userUtilMatrix);
	}
	
	
	private void setMatrix(ItemUtilityMatrix itemUtilMatrix, UserUtilityMatrix userUtilMatrix){
		
		//System.out.println("Similarity matrix calculation started");
		
		// Put the utilityMatrix list into an array.
		String [] data =
				itemUtilMatrix.getBusinessIds()
							 .toArray(new String[itemUtilMatrix.getBusinessIds().size()]);
		
		// Create a two dimensional array of doubles which will temporarily hold the similarities values.
		// Each row and column represents the index location of a cell in the String[] data array created above.
		double[][] m = new double[data.length][data.length];
		
		//Get the user averages out of the userUtilMatrix instance.
		Map<String, Double> userAverages = userUtilMatrix.getAveragesMap();
		double similarity = 0;
		
		for(int i = 0; i < m.length; i++)
    	{
			// Get the ratings map for the itemId String at position 'i',
			// from the itemUtilityMatrix object passed in the constructor.
			Map<String,Double> vectorAti = itemUtilMatrix.getUtilMatrix().get(data[i]);
    		
    		for(int j = i; j < m[i].length; j++)
    		{
    			if(i == j) {m[i][j] = 1;}
    			else
    			{
    				// Get the vector of ratings map for the <LocationData> object at position 'j',
    				// from the UtilityMatrix object passed in the constructor.
    				Map<String,Double> vectorAtj = itemUtilMatrix.getUtilMatrix()
    															.get(data[j]);
    	    		
    				similarity = adjCosSimilarity(vectorAti, vectorAtj, userAverages);
    				
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
				//****************************************************
				//Only the positive values are stored into the similarities map, 
				//because of better performance and no out of range predictions. 
				if(i != j && m[i][j] > 0) {
					neighbors.put(data[j], m[i][j]);
				//*****************************************************
				}
			}// End of j loop.
			this.matrix.put(busId, neighbors);
		}// End of i loop.
		
		System.out.println("$$$$$$$$$$$$$$$$");
		System.out.println("Similarity matrix calculations ended");
	}// End of setSimMatrix.
	
	private double adjCosSimilarity(Map<String, Double>vec1, Map<String, Double>vec2, Map<String,Double>userAverages) {
		
		//Check for zero so there won't be a division by zero.
		if(vec1.size() == 0 || vec2.size() == 0) {
			return 0;
		}
		return  dividend(vec1, vec2, userAverages) / divisor(vec1, vec2, userAverages);
		
	}
	
	private double dividend(Map<String,Double> vec1, Map<String, Double> vec2, Map<String,Double>userAverages) {
		
		double dotProduct = 0;
		
		// Do the intersection between the key sets of the maps in order to isolate the common userIds.
		Set<String> intersection = new HashSet<>(vec1.keySet());
		intersection.retainAll(vec2.keySet());
		
		// There needs to be more than 2 common users. If only one common user the result is always 1.
		if(intersection.size() < 2)
		{
			return 0;
		}
		
		// Calculate the dot product of the common userId.
		dotProduct = intersection.parallelStream()
										.map(s -> (vec1.get(s)-userAverages.get(s)) * (vec2.get(s)-userAverages.get(s)))
													.reduce(0.0, (Double a, Double b) -> a+b);
		
		return dotProduct;
	}
	
	private double divisor(Map<String, Double>vec1, Map<String, Double>vec2, Map<String,Double>userAverages){
		
		double sumOfSquares1 = vec1.keySet().stream().map(s -> (vec1.get(s)-userAverages.get(s)) *
																(vec1.get(s)-userAverages.get(s)))
														.reduce(0.0, (Double a, Double b) -> a+b);
		
		double sumOfSquares2 = vec2.keySet().stream().map(s -> (vec2.get(s)-userAverages.get(s)) *
																(vec2.get(s)-userAverages.get(s)))
														.reduce(0.0, (Double a, Double b) -> a+b);
		
		return Math.sqrt(sumOfSquares1) * Math.sqrt(sumOfSquares2);
		
	}

	
	// End of setMatrix helper methods.
	
	public Map<String, Map<String,Double>> getMatrix(){
		return this.matrix;
	}

}
