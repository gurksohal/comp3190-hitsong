

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class HitSongPredict {

	static String PATH = "created_dataset/balancedataset.csv";
	static Random rand = new Random();
	static ArrayList<SongData> songData;
	static ArrayList<SongData> testData;
	static ArrayList<SongData> trainData;
	
	public static void main(String[] args) throws IOException, CsvException {
		testData = new ArrayList<SongData>();
		trainData = new ArrayList<SongData>();
		songData = new ArrayList<SongData>();
		loadCSV(PATH);

		double avgAcc = 0.0;
		
		
		//run 5 times to get the avg acc
		for (int x = 0; x < 5; x++) {
			splitData(0.1); //test on 10% of the data
			HoldData train = createMatrices(trainData);
			HoldData test = createMatrices(testData);

			double[][] trainX = train.X;
			int[] trainY = train.Y;

			double[][] testX = test.X;
			int[] testY = test.Y;

			LogisticRegression model = new LogisticRegression(trainX, trainY, testX, testY);
			model.train();
			avgAcc += model.test();
		}
		
		System.out.println("Average Accuracy is: " + avgAcc/5);

	}

	//create a list of all the songs in the dataset
	private static void loadCSV(String path) throws IOException, CsvException {
		Reader reader = Files.newBufferedReader(Paths.get(path));
		CSVReader csvReader = new CSVReader(reader);
		List<String[]> records = csvReader.readAll();
		records.remove(0); // remove header
		for (String[] record : records) {
			SongData song = new SongData(record[0], record[1]);
			song.acousticness = record[2];
			song.danceability = record[3];
			song.energy = record[4];
			song.instrumentalness = record[5];
			song.key = record[6];
			song.liveness = record[7];
			song.loudness = record[8];
			song.mode = record[9];
			song.speechiness = record[10];
			song.tempo = record[11];
			song.valence = record[12];
			song.label = record[13];
			songData.add(song);
		}
		csvReader.close();
	}

	//split the data
	private static void splitData(double d) {
		trainData.addAll(songData);
		int size = trainData.size();
		while (testData.size() < size * d) {
			int index = rand.nextInt(trainData.size());
			testData.add(trainData.remove(index));
		}
	}

	//create training and testing data
	private static HoldData createMatrices(ArrayList<SongData> data) {
		double[][] X = new double[data.size()][11];
		int[] Y = new int[data.size()];
		for (int i = 0; i < data.size(); i++) {
			SongData song = data.get(i);
			double f1 = Double.parseDouble(song.acousticness);
			double f2 = Double.parseDouble(song.danceability);
			double f3 = Double.parseDouble(song.energy);
			double f4 = Double.parseDouble(song.instrumentalness);
			double f5 = Double.parseDouble(song.key);
			double f6 = Double.parseDouble(song.liveness);
			double f7 = Double.parseDouble(song.loudness);
			double f8 = Double.parseDouble(song.mode);
			double f9 = Double.parseDouble(song.speechiness);
			double f10 = Double.parseDouble(song.tempo);
			double f11 = Double.parseDouble(song.valence);
			X[i] = new double[] { f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11 };
			Y[i] = Integer.parseInt(song.label);
		}

		return new HoldData(X, Y);
	}

}

class HoldData {
	double[][] X;
	int[] Y;

	public HoldData(double[][] x, int[] y) {
		X = x;
		Y = y;
	}
}
