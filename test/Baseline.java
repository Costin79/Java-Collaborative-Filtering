package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Baseline {
	
	private List<PredictedRating> predictedSet;
	private DataSample sample;
	
	public Baseline(DataSample sample) {
		this.sample = sample;
		setPredictedSet();
	}
	
	private void setPredictedSet(){
		
		// Instantiate the list.
		this.predictedSet = new ArrayList<>();
		
		// Get the trainSet list out of the <DataSample> object.
		List<Rating> trainSet = this.sample.getTrainSet();
	
		// Get the testSet list out of the <DataSample> object.
		List<Rating> testSet = this.sample.getTestSet();
		
		// Get user utility matrix.
		UserUtilityMatrix userUtilMatrix = new UserUtilityMatrix(this.sample.getUserIds(), trainSet);
		
		// Get items utility matrix.
		ItemUtilityMatrix itemUtilMatrix = new ItemUtilityMatrix(this.sample.getBusinessIds(), trainSet);
		
		// Get the average rating of the trainSet.
		double averageRating = itemUtilMatrix.getAverage();
		
		// Calculate the predicted set.
		this.predictedSet = 
				testSet.stream()
				         .map(r -> new PredictedRating(r.getBusinessId(),
				        		 					   r.getUserId(),
				        		 					   r.getStars(),
											           calculateRating(r.getBusinessId(),
													  				   r.getUserId(), 
													  				   averageRating, 
													  				   userUtilMatrix.getAveragesMap(), 
													  				   itemUtilMatrix.getAveragesMap()))).collect(Collectors.toList());
	}	
	
	public List<PredictedRating> getPredictedSet(){
		return this.predictedSet;
	}

	
	private double calculateRating(String businessId, String userId, double averageRating,
									Map<String, Double>usersAverages, Map<String, Double>itemsAverages) {
		
		return averageRating + (usersAverages.get(userId) - averageRating)
						+ (itemsAverages.get(businessId) - averageRating); 
	}
	
	
	// *********************** CROSS VALIDATE.****************************************************
	
		public void crossValidate(int folds) {
			
			//Get the sorted list of businessIds from the <DataSample> object.
			List<String> businessIds = this.sample.getBusinessIds();
			
			//Get the sorted list of userIds from the <DataSample> object.
			List<String> userIds = this.sample.getUserIds();
					
			// Create an array to store the <Rating> objects of each fold.
			List<List<Rating>> foldsList = new ArrayList<>();
			
			// Instantiate an ArrayList of objects <Rating> in folds number of foldsList elements.
			for(int i = 0; i < folds; i++) {
				foldsList.add(new ArrayList<Rating>());
			}
			
			// Add the <Ratings> objects that have a rating value of 5.
			addRatingsCategory(foldsList, this.sample.getRatingsOfFive(), folds);
			
			// Add the <Ratings> objects that have a rating value of 4.
			addRatingsCategory(foldsList, this.sample.getRatingsOfFour(), folds);
			
			// Add the <Ratings> objects that have a rating value of 3.
			addRatingsCategory(foldsList, this.sample.getRatingsOfThree(), folds);
			
			// Add the <Ratings> objects that have a rating value of 2.
			addRatingsCategory(foldsList, this.sample.getRatingsOfTwo(), folds);
			
			// Add the <Ratings> objects that have a rating value of 1.
			addRatingsCategory(foldsList, this.sample.getRatingsOfOne(), folds);
			
			
			// ****************** The cross validation algorithm starts here. *****************************************
			
			// This string stores the result to be printed.
			String result = "";
			
			for(int i = 0; i < foldsList.size(); i++)
			{
				// pick the list at the cell partsArray[i] as testing set.
				List<Rating> foldTestSet = foldsList.get(i); 
				//System.out.println("Test set size is " + foldTestSet.size() + " " + i);
				
				// this list will hold the train set values, which are part of the lists at a position different than partsArray[i].
				List<Rating> foldTrainSet = new ArrayList<>(); 
				for(int j = 0; j < foldsList.size(); j++)
				{
					if(j != i)
					{
						foldTrainSet.addAll(foldsList.get(j));
					}
				}// end of j loop.
				//System.out.println("Here is fold number " + (i+1));
				//System.out.println("Train set size is " + trainSet.size());
				result = result + " Fold number " + (i+1) + " results are "
										+ runFold(foldTestSet, foldTrainSet, businessIds, userIds) + "\n";
				
			}// end of i loop.
			
			System.out.println(result);
		}
		
		private void addRatingsCategory(List<List<Rating>>foldsList, List<Rating>ratingsCategory, int folds) {
			
			// Calculate the number of ratingsCategory elements each fold should contain.
			int num = (int) Math.round((double)ratingsCategory.size() / folds);
			
			int x = 0; // This integer represents the position where the cut of the ratingsCategory is made.
			for(int i = 0; i < folds-1; i++) {
				foldsList.get(i).addAll(ratingsCategory.subList(x, x+num));
				x = x + num;
			}
			// Add the last block of ratingsCategory sub-list to the last element in the foldsList.
			foldsList.get(folds-1).addAll(ratingsCategory.subList(x, ratingsCategory.size()));
		}
		
		private String runFold(List<Rating>foldTestSet, List<Rating>foldTrainSet, List<String>businessIds, List<String>userIds){
			
			//Build the item utility matrix for the fold.
			ItemUtilityMatrix itemUtilMatrix = new ItemUtilityMatrix(businessIds, foldTrainSet);
			
			//Build the user utility matrix for the fold.
			UserUtilityMatrix userUtilMatrix = new UserUtilityMatrix(userIds, foldTrainSet);
			
			//Get the trainSet average rating.
			double average = itemUtilMatrix.getAverage();
			
	    	//Calculate a <PredictedRating> list of objects out of the testSet.
	    	List<PredictedRating> predictedSet = new ArrayList<>();
	    	
	    	predictedSet = foldTestSet.stream().map(r -> new PredictedRating(r.getBusinessId(), r.getUserId(), r.getStars(),
	          calculateRating(r.getBusinessId(),r.getUserId(), average, userUtilMatrix.getAveragesMap(), itemUtilMatrix.getAveragesMap())))
						.collect(Collectors.toList());
			
	    	double mae = Metrics.calculateMae(predictedSet);
	    	double rmse = Metrics.calculateRmse(predictedSet);
	    	
			return "MAE value is " +  mae + " RMSE value is "  +  rmse;
		}
		

}
	
	






