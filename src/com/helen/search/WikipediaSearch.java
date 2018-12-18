package com.helen.search;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.helen.database.Configs;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class WikipediaSearch {

	private final static String NOT_FOUND = "No results found.";
	final static Logger logger = Logger.getLogger(WikipediaSearch.class);
	
	public static int getPage(String searchTerm) throws IOException {
		int page = -1;
		// https://en.wikipedia.org/w/api.php?format=json&formatversion=2&action=query&list=search&srlimit=1&srprop=&srsearch=
		URL url = new URL(Configs.getSingleProperty("wikipediaSearchUrl").getValue() + searchTerm);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		JsonParser json = new JsonParser();
		JsonElement jsonTree = json.parse(br);
		if(jsonTree != null && jsonTree.isJsonObject()){
			JsonElement query = jsonTree.getAsJsonObject().get("query");
			if(query != null && query.isJsonObject()) {
				JsonElement search = query.getAsJsonObject().get("search");
				if (search != null && search.isJsonArray()) {
					JsonArray results = search.getAsJsonArray();
					if (results != null && results.size() > 0) {
						JsonElement result = results.get(0);
						if (result != null && result.isJsonObject()) {
							JsonElement pageid = result.getAsJsonObject().get("pageid");
							if (pageid != null && pageid.isJsonPrimitive()) {
								page = pageid.getAsInt();
							}
						}
					}
				}
			}
		}

		conn.disconnect();
		return page;
	}

	public static String cleanContent(String content) {
		content = content.replaceAll("\\s*\\([^()]+\\)", "").substring(0, 300);
		int lastPeriod = content.lastIndexOf('.');
		if(lastPeriod == -1)
			return content;
		else
			return content.substring(0, lastPeriod + 1);
	}

	public static String search(String searchTerm) throws IOException {
		searchTerm = URLEncoder.encode(searchTerm.substring(searchTerm.indexOf(' ') + 1), "UTF-8");
		int page = getPage(searchTerm);
		if(page == -1)
			return NOT_FOUND;
		String pageString = "" + page;
		String content = null;
		// https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&pageids=
		URL url = new URL(Configs.getSingleProperty("wikipediaEntryUrl").getValue() + pageString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

		JsonParser json = new JsonParser();
		JsonElement jsonTree = json.parse(br);
		if(jsonTree != null && jsonTree.isJsonObject()) {
			JsonElement query = jsonTree.getAsJsonObject().get("query");
			if (query != null && query.isJsonObject()) {
				JsonElement search = query.getAsJsonObject().get("pages");
				if (search != null && search.isJsonObject()) {
					JsonElement result = search.getAsJsonObject().get(pageString);
					if (result != null && result.isJsonObject()) {
						JsonElement extract = result.getAsJsonObject().get("extract");
						if (extract != null && extract.isJsonPrimitive()) {
							content = extract.getAsString();
						}
					}
				}
			}
		}

		conn.disconnect();

		if(content == null)
			return NOT_FOUND;

		return cleanContent(content);
	}
}
