

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.CSVWriter;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import io.jhdf.HdfFile;
import io.jhdf.api.Dataset;

public class SpotifyData {

	static ArrayList<SongData> songData;
	static SpotifyApi spotifyApi;
	static int count = 0;

	public static void getData() throws SpotifyWebApiException, IOException, URISyntaxException, InterruptedException {
		songData = new ArrayList<SongData>();
		setup();
		System.out.println("starting");
		walkPath("data");
		System.out.println("writing to file");
		writeCSVFile("created_dataset/dataset.csv");
	}

	//parse the .h5 files
	private static void walkPath(String path) throws SpotifyWebApiException, IOException, InterruptedException {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isDirectory()) {
					walkPath(f.getAbsolutePath());
				} else {
					songData.add(setAudioFeatures(createData(f)));
					System.out.println(count);
				}
			}
		} else {
			songData.add(setAudioFeatures(createData(file)));
			System.out.println(count);
		}
	}

	private static void writeCSVFile(String path) throws IOException {
		Writer writer = Files.newBufferedWriter(Paths.get(path));
		CSVWriter csvWrite = new CSVWriter(writer);
		String[] header = { "artist", "title", "acousticness", "danceability", "energy", "instrumentalness", "key",
				"liveness", "loudness", "mode", "speechiness", "tempo", "valence" };
		csvWrite.writeNext(header);
		for (SongData data : songData) {
			if (data != null) {
				String[] row = { data.artist, data.title, data.acousticness, data.danceability, data.energy,
						data.instrumentalness, data.key, data.liveness, data.loudness, data.mode, data.speechiness,
						data.tempo, data.valence };
				csvWrite.writeNext(row);
			}
		}
		csvWrite.close();
	}

	private static SongData createData(File file) throws SpotifyWebApiException, IOException {
		count++;
		if (count % 2000 == 0 && count != 0) { //get new token since it expires after around ~2.5k requests
			System.out.println("Getting a new access token.");
			setup();
		}

		HdfFile hdfFile = new HdfFile(file);
		Dataset dataset = hdfFile.getDatasetByPath("metadata/songs");
		Object data = dataset.getData();
		if (data instanceof HashMap) {
			HashMap map = (HashMap) data;
			String[] name = (String[]) map.get("artist_name");
			String[] title = (String[]) map.get("title");
			hdfFile.close();
			return new SongData(name[0], title[0]);
		}
		hdfFile.close();
		return null;
	}

	private static SongData setAudioFeatures(SongData s)
			throws SpotifyWebApiException, IOException, InterruptedException {
		String artist = s.artist;
		String name = s.title;
		Paging<Track> trackPaging = spotifyApi.searchTracks(artist + " " + name).build().execute(); //search spotify
		if (trackPaging.getItems().length > 0) { //get the first item from search
			String id = trackPaging.getItems()[0].getId();
			AudioFeatures list = spotifyApi.getAudioFeaturesForTrack(id).build().execute();
			SongData data = new SongData(artist, name);
			data.acousticness = list.getAcousticness().toString();
			data.danceability = list.getDanceability().toString();
			data.energy = list.getEnergy().toString();
			data.instrumentalness = list.getInstrumentalness().toString();
			data.key = list.getKey().toString();
			data.liveness = list.getLiveness().toString();
			data.loudness = list.getLoudness().toString();
			data.mode = String.valueOf(list.getMode().getType());
			data.speechiness = list.getSpeechiness().toString();
			data.tempo = list.getTempo().toString();
			data.valence = list.getValence().toString();
			Thread.sleep(850);
			return data;
		}
		return null;
	}

	//build connection to spotify api
	private static void setup() throws SpotifyWebApiException, IOException {
		SpotifyApi api = new SpotifyApi.Builder().setClientId("66c39ada3ea04784b8b8a7492299d5f4")
				.setClientSecret("82095a23461446beb341e28de361d3e3").build();
		ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();
		ClientCredentials clientCredentials = clientCredentialsRequest.execute();
		api.setAccessToken(clientCredentials.getAccessToken());
		spotifyApi = api;
	}
}
