package com.irc.helen;

import java.text.ParseException;

import org.apache.xmlrpc.XmlRpcException;

public class TestMain {

	
	

	
	public static void main(String args[]) throws XmlRpcException, ParseException {
		String nounData = "he/him/his, they/them/their, he/his/him";
		String[] nouns = nounData.replace(","," ").replace("/"," ").replace("\\"," ").trim().replaceAll(" +", " ").split(" ");
		for(String str: nouns){
			System.out.println(str);
		}
	}
	
	
	/*
	public static void main(String args[]) throws XmlRpcException, ParseException {
		String pagename = "SCP-106".toLowerCase();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("site", "scp-wiki");
		String[] target = new String[]{pagename.toLowerCase()};
		params.put("pages",target);
		ArrayList<String> keyswewant = new ArrayList<String>();
		keyswewant.add("title_shown");
		keyswewant.add("rating");
		keyswewant.add("created_at");
		keyswewant.add("title");
		keyswewant.add("created_by");
		keyswewant.add("tags");
		
			HashMap<String, HashMap<String, Object>> result = (HashMap<String, HashMap<String, Object>>) pushToAPI(
					"pages.get_meta", params);
			
			StringBuilder returnString = new StringBuilder();
			returnString.append(result.get(pagename).get("title_shown"));
			returnString.append(": ");
			returnString.append("Scp-106");
			returnString.append("(Rating: ");
			returnString.append(result.get(pagename).get("rating"));
			returnString.append(". By: )");
			returnString.append(result.get(pagename).get("created_by"));
			returnString.append(" - ");
			returnString.append("http://www.scp-wiki.net/");
			returnString.append(pagename.toLowerCase());
			returnString.append(" ");
			Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse((String) result.get(pagename).get("created_at"));
			Object[] tags = (Object[]) result.get(pagename).get("tags");
			for(Object tag: tags){
				System.out.println(tag.toString());
			}
			returnString.append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse((String) result.get(pagename).get("created_at")));
			System.out.println( returnString.toString());
			
		
	
	}
*/
}
