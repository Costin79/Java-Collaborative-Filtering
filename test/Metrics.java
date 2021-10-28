package test;

import java.util.List;

public class Metrics {
	
	public static double calculateMae(List<PredictedRating>predictedSet){
		
		if(predictedSet.size() == 0) {
			System.out.println("There are not any elements in the predicted set.");
			return 0;
		}
		
		double sum = predictedSet.stream().map(p->Math.abs(p.getReceivedRating() - p.getPredictedRating()))
											.reduce(0.0,(Double a, Double b)-> a+b);
		return sum / predictedSet.size();
	}
	
	public static double calculateRmse(List<PredictedRating>predictedSet) {
		
		if(predictedSet.size() == 0) {
			System.out.println("There are not any elements in the predicted set.");
			return 0;
		}
		
		double sum = predictedSet.stream().map(p->p.getReceivedRating() - p.getPredictedRating())
											.map(p-> p * p).reduce(0.0,(Double a, Double b)-> a+b);
		
		return Math.sqrt(sum / predictedSet.size());
	}

}
