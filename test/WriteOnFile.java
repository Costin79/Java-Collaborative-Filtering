package test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WriteOnFile {
	
	private static final String CSV_HEADER = "BusinessId,UserId,ReceivedRating,PredictedRating";
	
	public static void write(List<PredictedRating>predictedSet) {
		
		FileWriter fw = null;
		
		try {
			fw = new FileWriter("C:\\Users\\costp\\Desktop\\data500firstout.csv");
			fw.append(CSV_HEADER);
			fw.append('\n');
			
			for(PredictedRating p: predictedSet) {
				fw.append(p.getBusinessId());
				fw.append(",");
				fw.append(p.getUserId());
				fw.append(",");
				fw.append(String.valueOf(p.getReceivedRating()));
				fw.append(",");
				fw.append(String.valueOf(p.getPredictedRating()));
				fw.append('\n');
			}
		}
		catch(Exception e)
		{
			System.out.println("errror at the write to file!");
			e.printStackTrace();
		}
		finally
		{
			try {
				fw.flush();
				fw.close();
			}
			catch(IOException e)
			{
				System.out.println("Flushing/Closing error!");
				e.printStackTrace();
			}
		}
	}

}














