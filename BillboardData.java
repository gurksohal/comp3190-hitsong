
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;

//load list of songs from csv
//search the song on billbord to determine lable
//write to file
public class BillboardData {
	
	static ArrayList<SongData> songData;
	
	public static void main(String[] args) throws IOException, CsvException, SpotifyWebApiException, URISyntaxException, InterruptedException {
		SpotifyData.getData();
		songData = new ArrayList<SongData>();
		String path = "created_dataset/dataset.csv";
		loadCSV(path);
		writeCSVFile(path);
	}

	private static int isHit(SongData song) throws UnsupportedEncodingException, IOException {
		URL url = new URL(getUrl(song.artist, song.title));
		System.setProperty("http.agent", "Chrome");
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		String line = reader.readLine();
		System.out.println(songData.size() + 1 + ":" + line.substring(0,line.indexOf(',')));
		int value = 0;
		if(line.contains("chartcode\":\"HSI")) { //if song has been on hot 100 chart in its lifetime
			value = 1;
		}
		return value;
	}
	
	private static void loadCSV(String path) throws IOException, CsvException {
		Reader reader = Files.newBufferedReader(Paths.get(path));
		CSVReader csvReader = new CSVReader(reader);
		List<String[]> records = csvReader.readAll();
		records.remove(0); //remove header
		for(String[] record : records) {
			SongData song = new SongData(record[0], record[1]);
			song.label = String.valueOf(isHit(song));
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
			songData.add(song);
		}
		csvReader.close();
	}
	
	private static void writeCSVFile(String path) throws IOException {
		Writer writer = Files.newBufferedWriter(Paths.get(path));
		CSVWriter csvWrite = new CSVWriter(writer);
		String[] header = { "artist", "title", "acousticness", "danceability", "energy", "instrumentalness", "key",
				"liveness", "loudness", "mode", "speechiness", "tempo", "valence", "label" };
		csvWrite.writeNext(header);
		for (SongData data : songData) {
			if (data != null) {
				String[] row = { data.artist, data.title, data.acousticness, data.danceability, data.energy,
						data.instrumentalness, data.key, data.liveness, data.loudness, data.mode, data.speechiness,
						data.tempo, data.valence, data.label};
				csvWrite.writeNext(row);
			}
		}
		csvWrite.close();
	}
	
	//url encode the arist and song names and append to url
	private static String getUrl(String artist, String title) throws UnsupportedEncodingException {
		artist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString());
		title = URLEncoder.encode(title, StandardCharsets.UTF_8.toString());
		return "https://www.billboard.com/fe-ajax/charts/search?page=0&artist=" + artist + "&title=" + title;
	}
}

