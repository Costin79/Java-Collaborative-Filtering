package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ItemsCosSim {
	
	private List<PredictedRating> predictedSet;
	private DataSample sample;
	
	public ItemsCosSim(DataSample sample) {
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
		//List<String> userIds = sample.getUserIds();
		
		// Create a <UtilityMatrix> object out of the trainSet, the businessIds list, and the userIds list.
		// The businessIds list is used along with the train set to ensure that every
		// businessId is included in the <UtilityMatrix> object.
		ItemUtilityMatrix utilityMatrix = new ItemUtilityMatrix(businessIds,trainSet);
		
		
		// Create a <ItemCosSimMatrix> object.
    	ItemsSimMatrix2 similarityMatrix = new ItemsSimMatrix2(utilityMatrix);
		//Map<String, List<Neighbor>> matrix = similarityMatrix.getMatrix();
		
		// Get the average of all the ratings in the sample.
		//double sampleAverage = this.sample.getSampleAverage();
		
		this.predictedSet = testSet.stream()
							.map(r -> new PredictedRating(r.getBusinessId(), r.getUserId(), r.getStars(),
								calculateRating(r.getBusinessId(),r.getUserId(), similarityMatrix, utilityMatrix)))
							.collect(Collectors.toList());
		
		
	}
	
	private double calculateRating (String businessId, String userId, 
									ItemsSimMatrix2 similarityMatrix, ItemUtilityMatrix utilityMatrix){
		
	// Find businesses that have a similarity with the businessId, and were rated by the userId, put them in a 
	// <CommonBusiness> list. Use 10 elements in the list that have the highest similarity value with the businessId
	// to calculate a predicted raring.
		
		double predictedRat;
		
		// Get the neighbors of the businessId.
		Map<String, Double> neighbors = similarityMatrix.getMatrix().get(businessId);
		
		// Return the average if there is no similarity between the businessId and any other businessId.
		if(neighbors.size() == 0) {
			//return sampleAverage;
			return utilityMatrix.getAveragesMap().get(businessId);
		}
		
		// This list stores the <CommonBusiness> objects used to calculate the predicted rating.
		List<CommonBusiness> commonBusinesses = getComBus(neighbors, userId, utilityMatrix.getUtilMatrix());
		
		if(commonBusinesses.size() == 0) {
			
			//return sampleAverage;
			return utilityMatrix.getAveragesMap().get(businessId);
		}
		else if(commonBusinesses.size() <= 10) {
			return predictRating(commonBusinesses);
		}
		else {
			Collections.sort(commonBusinesses,Comparator.comparing(CommonBusiness::getSimilarity).reversed());
			predictedRat = predictRating(commonBusinesses.subList(0, 10));
		}
		return predictedRat;
	}
	
	private List<CommonBusiness> getComBus(Map<String,Double>neighbors, String userId, 
												Map<String,Map<String,Double>> utilMatrix ){
		
		List<CommonBusiness> comBus = new ArrayList<>();
		
		// Collect the <CommonBusiness> objects.
		
		for(String neighbor: neighbors.keySet()) {
			
			Map<String, Double> vec = utilMatrix.get(neighbor);
		    
		    if(vec.keySet().contains(userId)) {
		    	comBus.add(new CommonBusiness(neighbor, neighbors.get(neighbor), vec.get(userId)));
		    }
					
		}//);
		
		return comBus;
	}// End getComBus.
	
	private double predictRating(List<CommonBusiness>list) {
		
		double dividend = list.stream().map(b->(b.getStars()*b.getSimilarity()))
										.reduce(0.0,(Double a, Double b)-> a+b);
		double divisor = list.stream().map(CommonBusiness::getSimilarity).reduce(0.0, (Double a, Double b)-> a+b);
		return dividend/divisor;
		
	}
	
	// *********************** CROSS VALIDATE.****************************************************
	
	public void crossValidate(int folds) {
		
		//Get the sorted list of businessIds from the <DataSample> object.
		List<String> businessIds = this.sample.getBusinessIds();
		
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
			//System.out.println("Test set size is " + testSet.size());
			
			// this list will hold the train set values, which are part of the lists at a position different than partsArray[i].
			List<Rating> trainSet = new ArrayList<>(); 
			for(int j = 0; j < foldsList.size(); j++)
			{
				if(j != i)
				{
					trainSet.addAll(foldsList.get(j));
				}
			}// end of j loop.
			//System.out.println("Here is fold number " + (i+1));
			//System.out.println("Train set size is " + trainSet.size());
			
			result = result + " Fold number " + (i+1) + " results are "
									+ runFold(testSet, trainSet, businessIds) + "\n";
			
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
	
	private String runFold(List<Rating>testSet, List<Rating>trainSet, List<String>businessIds){
		
		//Build the utility matrix for the fold.
		ItemUtilityMatrix utilityMatrix = new ItemUtilityMatrix(businessIds, trainSet);
		
		// Create a <ItemCosSimMatrix> object.
    	ItemsSimMatrix2 similarityMatrix = new ItemsSimMatrix2(utilityMatrix);
    	
    	//Calculate a <PredictedRating> list of objects out of the testSet.
    	List<PredictedRating> predictedSet = new ArrayList<>();
    	
    	predictedSet = testSet.stream().map(r -> new PredictedRating(r.getBusinessId(), r.getUserId(), r.getStars(),
					calculateRating(r.getBusinessId(),r.getUserId(), similarityMatrix, utilityMatrix)))
					.collect(Collectors.toList());
		
    	double mae = Metrics.calculateMae(predictedSet);
    	double rmse = Metrics.calculateRmse(predictedSet);
    	
		return "MAE value is " +  mae + " RMSE value is "  +  rmse;
	}
	
}




























