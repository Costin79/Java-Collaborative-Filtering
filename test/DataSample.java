package test;

import java.util.List;
import java.lang.String;
import java.util.stream.*;
import java.util.*;
import java.nio.file.*;
import java.io.IOException;

// This class creates lists of <Rating> objects out of the .csv path file string passed in its constructor.

public class DataSample{
	
	private String file;
	private List<Rating> sampleData;
	private List<Rating> trainSet;
	private List<Rating> testSet;
	private List<Rating> ratingsOfOne;
	private List<Rating> ratingsOfTwo;
	private List<Rating> ratingsOfThree;
	private List<Rating> ratingsOfFour;
	private List<Rating> ratingsOfFive;
	private List<String> businessIds;
	private List<String> userIds;
	private List<Rating> rawData;
	private double sampleAverage;

	public DataSample(String file) {
		
		this.file = file;
		// Build the sampleData <Rating> list out of the .csv file.
		try {
			
			Stream<String> rows = Files.lines(Paths.get(this.file));
		//Build a list that contains all the ratings, including duplicates out of the .csv file.	
			this.rawData = rows.skip(1)
								  .map(x->x.split(","))
								  .filter(x->x.length==3)
								  .map(x->{ 
									  return new Rating(x[0],x[1], Double.parseDouble(x[2]));
									 })
								  .collect(Collectors.toList());
			rows.close();
			// Filter the rawData lists of <Rating> is such a way that it will result in a list which will not contain any
			// ratings made by a userId to the same businessId.
			filterRawData();
		}
		catch(IOException e) {
			System.out.println("File path problem in DataSample constructor.");
		}
		
		
		//Build the rating category lists.
		this.ratingsOfOne = this.sampleData.parallelStream().filter(x->x.getStars()==1).collect(Collectors.toList());
		this.ratingsOfTwo = this.sampleData.parallelStream().filter(x->x.getStars()==2).collect(Collectors.toList());
		this.ratingsOfThree = this.sampleData.parallelStream().filter(x->x.getStars()==3).collect(Collectors.toList());
		this.ratingsOfFour = this.sampleData.parallelStream().filter(x->x.getStars()==4).collect(Collectors.toList());
		this.ratingsOfFive = this.sampleData.parallelStream().filter(x->x.getStars()==5).collect(Collectors.toList());
		
		// Shuffle each list.
		Collections.shuffle(this.sampleData);
		Collections.shuffle(this.ratingsOfOne);
		Collections.shuffle(this.ratingsOfTwo);
		Collections.shuffle(this.ratingsOfThree);
		Collections.shuffle(this.ratingsOfFour);
		Collections.shuffle(this.ratingsOfFive);
		
		// Build a sorted list of strings that contains the unique businessIds in the sampleData list.
		setBusinessIds();
		
		// Build a sorted list of strings that contains the unique userIds in the sampleData list.
		setUserIds();
		
		// Calculate the sampleData average rating.
		setSampleAverage();
		
	}// End of constructor
	
	// Create getters for all the lists.
	public List<Rating> getSampleData(){
		return this.sampleData;
	}
	public List<Rating> getRatingsOfOne(){
		return this.ratingsOfOne;
	}
	public List<Rating> getRatingsOfTwo(){
		return this.ratingsOfTwo;
	}
	public List<Rating> getRatingsOfThree(){
		return this.ratingsOfThree;
	}
	public List<Rating> getRatingsOfFour(){
		return this.ratingsOfFour;
	}
	public List<Rating> getRatingsOfFive(){
		return this.ratingsOfFive;
	}
	public List<Rating> getTrainSet(){
		return this.trainSet;
	}
	public List<Rating> getTestSet(){
		return this.testSet;
	}
	public List<String> getBusinessIds() {
		return this.businessIds;
	}
	public List<String> getUserIds() {
		return this.userIds;
	}
	public List<Rating> getRawData(){
		return this.rawData;
	}
	public double getSampleAverage() {
		return this.sampleAverage;
	}
	
	// Setters
	private void setSampleAverage(){
		
		// if/else so a division by zero won't be possible.
		if(this.sampleData.size() > 0) 
		{
			double sumOfRatings = this.sampleData.stream().map(Rating::getStars)
																	.reduce(0.0,(Double a, Double b)-> a + b);
			this.sampleAverage = sumOfRatings / this.sampleData.size();
		}
		else 
		{
				this.sampleAverage = 0;
		}
	}
	
	private void setBusinessIds(){
		this.businessIds = this.sampleData.stream()
				  .map(rating -> rating.getBusinessId())
				  .distinct()
				  .sorted()
				  .collect(Collectors.toList());
	}
	
	private void setUserIds() {
		this.userIds = this.sampleData.stream()
									  .map(Rating::getUserId)
									  .distinct()
									  .sorted()
									  .collect(Collectors.toList());
	}
	
	
	// The split method which builds the trainSet and testSet list of <Rating> objects.
	public void split(double ratio) {
		
		// Initialize the testSet and trainSet lists.
		this.testSet = new ArrayList<>();
		this.trainSet = new ArrayList<>();
		
		if(ratio <= 0 || ratio >= 1)
		{
			throw new InvalidInputException("Invalid input. The value needs to be between 0 and 1.");
		}
		
		// Calculate the size of the testSet list.
		int sizeOfTestSet = (int)(ratio * this.sampleData.size());
		
		//Add each ratings category list to the testSet and trainSet lists in such a way that
		// the testSet lists contains ratios of each category lists the same as the sampleData lists.
		addRatingsCategory(sizeOfTestSet, this.ratingsOfFive);
		addRatingsCategory(sizeOfTestSet, this.ratingsOfFour);
		addRatingsCategory(sizeOfTestSet, this.ratingsOfThree);
		addRatingsCategory(sizeOfTestSet, this.ratingsOfTwo);
		addRatingsCategory(sizeOfTestSet, this.ratingsOfOne);
		
	}
	
	private void addRatingsCategory(int sizeOfTestSet, List<Rating> ratingsCategory) {
		
		// Calculate the number of <Rating> objects with rating values of five the testSet list should contain.
		double n = (double)ratingsCategory.size() / (double)this.sampleData.size(); 
		int num = (int) Math.round(n * sizeOfTestSet);
				
		// Add an n number of <Rating> objects from the ratingsOfFive list to the testSet list, and add the remaining  
		// <Rating> objects to the trainSet list.
		
		for(int i = 0; i < num; i++) {
			this.testSet.add(new Rating(ratingsCategory.get(i).getUserId(),
										ratingsCategory.get(i).getBusinessId(),
										ratingsCategory.get(i).getStars()));
		// Creates new rating objects instead of just passing the reference of the objects in the ratingsOfCategory list.
		}
		
		//Add the rest of the ratingsOfCategory list to the trainSet list.
		for(int i = num; i < ratingsCategory.size(); i++) {
			this.trainSet.add(new Rating(ratingsCategory.get(i).getUserId(),
										 ratingsCategory.get(i).getBusinessId(),
										 ratingsCategory.get(i).getStars()));
		}
	}
	
	private void filterRawData(){
		
		// Create a HashSet.
		Set<Rating>ratingsSet = new HashSet<>();
		
		// Add every rating in the raw data to the set.
		ratingsSet = this.rawData.parallelStream().collect(Collectors.toSet());
		
		// Put the elements of the ratingsSet in this.sampleData List.
		this.sampleData = new ArrayList<>(ratingsSet);
	}
}























