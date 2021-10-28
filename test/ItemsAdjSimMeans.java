package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemsAdjSimMeans {
	
	private List<PredictedRating> predictedSet;
	private DataSample sample;
	
	public ItemsAdjSimMeans(DataSample sample) {
		this.sample = sample;
		setPredictedSet();
	}
	
	public List<PredictedRating> getPredictedSet(){
		return this.predictedSet;
	}
	
	private void setPredictedSet() {
		
		// Instantiate the list.
		this.predictedSet = new ArrayList<>();
		
		// Get the trainSet list out of the <DataSample> object.
		List<Rating> trainSet = this.sample.getTrainSet();
	
		// Get the testSet list out of the <DataSample> object.
		List<Rating> testSet = this.sample.getTestSet();
		
		//Get the sorted list of businessIds from the <DataSample> object.
		List<String> businessIds = this.sample.getBusinessIds();
		
		//Get the sorted list of userIds from the <DataSample> object.
		List<String> userIds = this.sample.getUserIds();
		
		// Create a ItemUtilityMatrix object out of the trainSet, the businessIds list.
		// The businessIds list is used along with the train set to ensure that every
		// businessId is included in the ItemUtilityMatrix object.
		ItemUtilityMatrix itemUtilMatrix = new ItemUtilityMatrix(businessIds,trainSet);
		
		// Create a ItemUtilityMatrix object out of the trainSet, the businessIds list.
		// The businessIds list is used along with the train set to ensure that every
		// businessId is included in the ItemUtilityMatrix object.
		UserUtilityMatrix userUtilMatrix = new UserUtilityMatrix(userIds, trainSet);
		
		// Create a <ItemCosSimMatrix> object.
    	ItemsAdjSimMatrix similarityMatrix = new ItemsAdjSimMatrix(itemUtilMatrix, userUtilMatrix);
		//Map<String, List<Neighbor>> matrix = similarityMatrix.getMatrix();
		
		
		this.predictedSet = testSet.stream()
							.map(r -> new PredictedRating(r.getBusinessId(), r.getUserId(), r.getStars(),
										calculateRating(r.getBusinessId(),r.getUserId(), similarityMatrix,
										itemUtilMatrix)))
							.collect(Collectors.toList());
		
	
	}
	
	private double calculateRating (String businessId, String userId, 
									ItemsAdjSimMatrix similarityMatrix, ItemUtilityMatrix utilityMatrix){
		
	// Find businesses that have a similarity with the businessId, and were rated by the userId, put them in a 
	// <CommonBusiness> list. Use 10 elements in the list that have the highest similarity value with the businessId
	// to calculate a predicted raring.
		
		double predictedRat;
		
		// Get the neighbors of the businessId.
		Map<String, Double> similarities = similarityMatrix.getMatrix().get(businessId);
		
		// Return the average if there is no similarity between the businessId and any other businessId.
		if(similarities.size() == 0) {
			return utilityMatrix.getAveragesMap().get(businessId);
		}
		
		// This list stores the <CommonBusiness> objects used to calculate the predicted rating.
		List<CommonBusiness> commonBusinesses = getComBus(similarities, userId, utilityMatrix.getUtilMatrix());
		
		if(commonBusinesses.size() == 0) {
			
			return utilityMatrix.getAveragesMap().get(businessId);
		}
		else if(commonBusinesses.size() <= 50) {
			return predictRating(commonBusinesses, utilityMatrix, businessId);
		}
		else {
			Collections.sort(commonBusinesses,Comparator.comparing(CommonBusiness::getSimilarity).reversed());
			predictedRat = predictRating(commonBusinesses.subList(0, 50), utilityMatrix, businessId);
		}
		return predictedRat;
	}
	
	private List<CommonBusiness> getComBus(Map<String,Double>similarities, String userId, 
												Map<String,Map<String,Double>> utilMatrix ){
		
		List<CommonBusiness> comBus = new ArrayList<>();
		
		
		for(String neighbor: similarities.keySet()) {
			
			
			Map<String, Double> vec = utilMatrix.get(neighbor);
		    
		    if(vec.keySet().contains(userId)) {
		    	comBus.add(new CommonBusiness(neighbor, similarities.get(neighbor), vec.get(userId)));
		    }
					
		}//);
		
		return comBus;
	}// End getComBus.
	
	private double predictRating(List<CommonBusiness>list, ItemUtilityMatrix utilityMatrix, String businessId) {
		
		double dividend = list.stream().map(b->((b.getStars() - utilityMatrix.getAveragesMap().get(b.getBusId()))
												* b.getSimilarity()))
										.reduce(0.0,(Double a, Double b)-> a+b);
		
		double divisor = list.stream().map(CommonBusiness::getSimilarity).reduce(0.0, (Double a, Double b)-> a+b);
		
		double result = utilityMatrix.getAveragesMap().get(businessId) + (dividend/divisor);
		
		if(result < 1) {return 1;}
		else if(result > 5) {return 5;}
		else {return result;}
		//System.out.println(utilityMatrix.getAveragesMap().get(businessId) + (dividend/divisor));
		//return utilityMatrix.getAveragesMap().get(businessId) + (dividend/divisor);
		
	}
	
	// *********************** CROSS VALIDATE.****************************************************
	
	public void crossValidate(int folds) {
		
		//Get the sorted list of businessIds from the DataSample object.
		List<String> businessIds = this.sample.getBusinessIds();
		
		//Get the sorted list of userIds from the DataSample object.
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
			List<Rating> testSet = foldsList.get(i); 
			System.out.println("Test set size is " + testSet.size());
			
			// this list will hold the train set values, which are part of the lists at a position different than partsArray[i].
			List<Rating> trainSet = new ArrayList<>(); 
			for(int j = 0; j < foldsList.size(); j++)
			{
				if(j != i)
				{
					trainSet.addAll(foldsList.get(j));
				}
			}// end of j loop.
			System.out.println("Here is fold number " + (i+1));
			System.out.println("Train set size is " + trainSet.size());
			
			result = result + " Fold number " + (i+1) + " results are "
									+ runFold(testSet, trainSet, businessIds, userIds) + "\n";
			
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
	
	private String runFold(List<Rating>testSet, List<Rating>trainSet, List<String>businessIds, List<String>userIds){
		
		//Build the items utility matrix for the fold.
		ItemUtilityMatrix itemUtilMatrix = new ItemUtilityMatrix(businessIds, trainSet);
		
		//Build the users utility matrix for the fold.
		UserUtilityMatrix userUtilMatrix = new UserUtilityMatrix(userIds, trainSet);
		
		// Create a <ItemCosSimMatrix> object.
    	ItemsAdjSimMatrix similarityMatrix = new ItemsAdjSimMatrix(itemUtilMatrix, userUtilMatrix);
    	
    	//Calculate a <PredictedRating> list of objects out of the testSet.
    	List<PredictedRating> predictedSet = new ArrayList<>();
    	predictedSet = testSet.stream().map(r -> new PredictedRating(r.getBusinessId(), r.getUserId(), r.getStars(),
					calculateRating(r.getBusinessId(),r.getUserId(), similarityMatrix, itemUtilMatrix)))
					.collect(Collectors.toList());
		
    	double mae = Metrics.calculateMae(predictedSet);
    	double rmse = Metrics.calculateRmse(predictedSet);
    	
		return "MAE value is " +  mae + " RMSE value is "  +  rmse;
	}
	
	

	

}
