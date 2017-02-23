package net.kwami.tcp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class DeMultiplexor {

	public DeMultiplexor() {
		super();
	}

	public void run(InputStream in) {
		Gson gson = new GsonBuilder().create();
		try (JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));) {
			// List<Message> messages = new ArrayList<Message>();
			reader.beginArray();
			while (reader.hasNext()) {
				Message message = gson.fromJson(reader, Message.class);
				System.out.println(message.toString());
			}
			reader.endArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try (InputStream in = new FileInputStream(
				"/home/chris/git/kwami/general/load-balancer/src/main/java/net/kwami/tcp/input.txt")) {
			DeMultiplexor plexor = new DeMultiplexor();
			plexor.run(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
