

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

//read from file
//balance out the dataset
//write to file
public class FixDataset {

	static ArrayList<SongData> hitSongs;
	static ArrayList<SongData> nonHitSongs;
	public static void main(String[] args) throws IOException, CsvException {
		hitSongs = new ArrayList<SongData>();
		nonHitSongs = new ArrayList<SongData>();
		
		String path = "created_dataset/dataset.csv";
		loadCSV(path);
		System.out.println(hitSongs.size());
		System.out.println(nonHitSongs.size());
		path = "created_dataset/balancedataset.csv";
		Collections.shuffle(nonHitSongs);
		List<SongData> list = nonHitSongs.subList(0, hitSongs.size()*2);
		hitSongs.addAll(list);
		Collections.shuffle(hitSongs);
		writeCSVFile(path);
	}
	
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
			if(song.label.equals("1")) {
				hitSongs.add(song);
			}else{
				nonHitSongs.add(song);
			}

		}
		csvReader.close();
	}
	
	private static void writeCSVFile(String path) throws IOException {
		Writer writer = Files.newBufferedWriter(Paths.get(path));
		CSVWriter csvWrite = new CSVWriter(writer);
		String[] header = { "artist", "title", "acousticness", "danceability", "energy", "instrumentalness", "key",
				"liveness", "loudness", "mode", "speechiness", "tempo", "valence", "label" };
		csvWrite.writeNext(header);
		for (SongData data : hitSongs) {
			if (data != null) {
				String[] row = { data.artist, data.title, data.acousticness, data.danceability, data.energy,
						data.instrumentalness, data.key, data.liveness, data.loudness, data.mode, data.speechiness,
						data.tempo, data.valence, data.label};
				csvWrite.writeNext(row);
			}
		}
		csvWrite.close();
	}
	
	
}
