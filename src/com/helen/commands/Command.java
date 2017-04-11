package com.helen.commands;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import com.helen.database.Config;
import com.helen.database.Configs;
import com.helen.database.DatabaseObject;
import com.helen.database.Pages;
import com.helen.database.Pronouns;
import com.helen.database.Queries;
import com.helen.database.Roll;
import com.helen.database.Rolls;
import com.helen.database.Tell;
import com.helen.database.Tells;
import com.helen.database.Users;
import com.helen.search.WebSearch;
import com.helen.search.YouTubeSearch;

public class Command {
	private static final Logger logger = Logger.getLogger(Command.class);

	private PircBot helen;

	private boolean adminMode = false;

	private static HashMap<String, Method> hashableCommandList = new HashMap<String, Method>();
	private static HashMap<String, Method> slowCommands = new HashMap<String, Method>();
	private static HashMap<String, Method> regexCommands = new HashMap<String, Method>();
	public Command() {

	}

	public Command(PircBot ircBot) {
		helen = ircBot;
	}

	static {
		for (Method m : Command.class.getDeclaredMethods()) {
			if (m.isAnnotationPresent(IRCCommand.class)) {
				if (m.getAnnotation(IRCCommand.class).startOfLine() && !m.getAnnotation(IRCCommand.class).reg()) {
					for(String s: ((IRCCommand) m.getAnnotation(IRCCommand.class)).command()){
						hashableCommandList.put(s.toLowerCase(), m);
					}
				} else if (!m.getAnnotation(IRCCommand.class).reg()) {
					for(String s: ((IRCCommand) m.getAnnotation(IRCCommand.class)).command()){
						slowCommands.put(s.toLowerCase(), m);
					}
				} else {
					for(String s: ((IRCCommand) m.getAnnotation(IRCCommand.class)).regex()){
						regexCommands.put(s, m);
					}
				}

				logger.info(((IRCCommand) m.getAnnotation(IRCCommand.class)).command());
			}
		}
		logger.info("Finished Initializing commandList.");
	}

	private void checkTells(CommandData data) {
		ArrayList<Tell> tells = Tells.getTells(data.getSender());
		
		if(tells.size() > 0){
			helen.sendNotice(data.getSender(), "You have " + tells.size() + " pending tell(s).");
		}
		for (Tell tell : tells) {
			Tells.clearTells(tell.getTarget());
			helen.sendMessage(tell.getTarget(), tell.toString());

		}
	}

	public void dispatchTable(CommandData data) {
			
		checkTells(data);

		boolean jarvisInChannel = false;
		if(!(data.getChannel() == null || data.getChannel().isEmpty())){
			User[] list = helen.getUsers(data.getChannel());
			for(User u: list){
				logger.info(u.getNick());
				if(u.getNick().equalsIgnoreCase("jarvis")){
					jarvisInChannel = true;
				}
			}
		}else{
			logger.info("Channel was empty");
		}
		
		logger.info("Entering dispatch table with command: \"" + data.getCommand() + "\"");
		if (hashableCommandList.containsKey(data.getCommand().toLowerCase())) {
			try {
				
				Method m = hashableCommandList.get(data.getCommand().toLowerCase());
				if(m.getAnnotation(IRCCommand.class).coexistWithJarvis() || !jarvisInChannel){
					m.invoke(this, data);
				}
				
				
				
				
			} catch (Exception e) {
				logger.error("Exception invoking start-of-line command: " + data.getCommand(), e);
			}
		} else {
			for (String command : slowCommands.keySet()) {
				if (data.getMessage().toLowerCase().contains(command.toLowerCase())) {
					try {
						Method m = slowCommands.get(command);
						if(m.getAnnotation(IRCCommand.class).coexistWithJarvis() || !jarvisInChannel){
							m.invoke(this, data);
						}
					} catch (Exception e) {
						logger.error("Exception invoking command: " + command, e);
					}
				}
			}
			for (String regex : regexCommands.keySet()){
				Pattern r = Pattern.compile(regex);
				
				if(!(data.getSplitMessage().length > 1)){
					Matcher match = r.matcher(data.getSplitMessage()[0]);
					if(match.matches()){
						try {
							Method m = regexCommands.get(regex);
							if(m.getAnnotation(IRCCommand.class).coexistWithJarvis() || !jarvisInChannel){
								m.invoke(this,data);
							}
						} catch (Exception e) {
							logger.error("Exception invoking command: " + regexCommands.get(regex).getAnnotation(IRCCommand.class).command(), e);
						}
					}
				}
			}
		}
	}

	// Relateively unregulated commands (anyone can try these)
	@IRCCommand(command = {".HelenBot"}, startOfLine = false, coexistWithJarvis = true)
	public void versionResponse(CommandData data) {
		if (data.getChannel().isEmpty()) {
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": Greetings, I am HelenBot v"
					+ Configs.getSingleProperty("version").getValue());
		}
		helen.sendMessage(data.getChannel(),
				data.getSender() + ": Greetings, I am HelenBot v" + Configs.getSingleProperty("version").getValue());
	}

	@IRCCommand(command = {".modeToggle"}, startOfLine = true, coexistWithJarvis = true)
	public void toggleMode(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			adminMode = !adminMode;
		}
	}
	
	@IRCCommand(command = {".ch",".choose"}, startOfLine = true)
	public void choose(CommandData data){
		if(data.isAuthenticatedUser(adminMode, true)){
			String[] choices = data.getMessage().substring(data.getMessage().indexOf(" ")).split(",");
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " 
						+ choices[((int) (Math.random() * (choices.length - 1)) + 1)]);
		}
	}

	@IRCCommand(command = {".mode"}, startOfLine = true, coexistWithJarvis = true)
	public void displayMode(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			helen.sendMessage(data.getResponseTarget(),
					data.getSender() + ": I am currently in " + (adminMode ? "Admin" : "Any User") + " mode.");
		}
	}

	@IRCCommand(command = {".msg"}, startOfLine = true)
	public void sendMessage(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			String target = data.getTarget();
			String payload = data.getPayload();

			helen.sendMessage(target, data.getSender() + " said:" + payload);

		}
	}

	@IRCCommand(command = {".roll"}, startOfLine = true)
	public void roll(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			Roll roll = new Roll(data.getMessage(), data.getSender());
			Rolls.insertRoll(roll);
			helen.sendMessage(data.getChannel(), data.getSender() + ": " + roll.toString());
		}
	}

	@IRCCommand(command = {".myRolls", ".myrolls"}, startOfLine = true)
	public void getRolls(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			ArrayList<Roll> rolls = Rolls.getRolls(data.getSender());
			if (rolls.size() > 0) {
				helen.sendMessage(data.getResponseTarget(), buildResponse(rolls));
			} else {
				helen.sendMessage(data.getResponseTarget(),
						data.getSender() + ": Apologies, I do not have any saved rolls for you at this time.");
			}

		}
	}
	
	@IRCCommand(command = {".average",".avg"}, startOfLine = true)
	public void getAverage(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			String average = Rolls.getAverage(data.getSplitMessage()[1], data.getSender());
			if(average != null){
				helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " +  average);
			}

		}
	}

	@IRCCommand(command = {".g",".google"}, startOfLine = true)
	public void webSearch(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			try {
				helen.sendMessage(data.getResponseTarget(),
						data.getSender() + ": " + WebSearch.search(data.getMessage()).toString());
			} catch (IOException e) {
				logger.error("Exception during web search", e);
			}
		}

	}

	@IRCCommand(command = {".y",".yt",".youtube"}, startOfLine = true)
	public void youtubeSearch(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			helen.sendMessage(data.getResponseTarget(),
					data.getSender() + ": " + YouTubeSearch.youtubeSearch(data.getMessage()).toString());
		}

	}

	@IRCCommand(command = ".seen", startOfLine = true)
	public void seen(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			helen.sendMessage(data.getResponseTarget(), Users.seen(data));
		}
	}
	
	@IRCCommand(command = "SCP", startOfLine = true, reg = true, regex = {"(scp|SCP)-([0-9]+)"})
	public void scpSearch(CommandData data){
		if(data.isAuthenticatedUser(adminMode, true)){
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + Pages.getPageInfo(data.getCommand()));
		}
	}
	
	@IRCCommand(command = ".tagLoad", startOfLine = true, coexistWithJarvis = true)
	public void updateTags(CommandData data){
		if(data.isAuthenticatedUser(adminMode, false)){
			Pages.getTags();
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": Tags have been updated in my database." );
		}
	}
	
	@IRCCommand(command = {".pronouns",".pronoun"}, startOfLine = true, coexistWithJarvis = true)
	public void getPronouns(CommandData data){
		if(data.isAuthenticatedUser(adminMode, true)){
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + Pronouns.getPronouns(data.getTarget()));
		}
	}
	
	@IRCCommand(command = ".myPronouns", startOfLine = true, coexistWithJarvis = true)
	public void myPronouns(CommandData data){
		if(data.isAuthenticatedUser(adminMode, true)){
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + Pronouns.getPronouns(data.getSender()));
		}
	}
	
	@IRCCommand(command = ".setPronouns", startOfLine = true, coexistWithJarvis = true)
	public void setPronouns(CommandData data){
		if(data.isAuthenticatedUser(adminMode, true)){
			String response = Pronouns.insertPronouns(data);
			if(response.contains("banned term" )){
				Tells.sendTell("DrMagnus", "Secretary_Helen", "User " + data.getSender() + " attempted to add a banned term:" + response, true);
			}
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + response);
		}
	}
	
	@IRCCommand(command = ".clearPronouns", startOfLine = true, coexistWithJarvis = true)
	public void clearPronouns(CommandData data){
		if(data.isAuthenticatedUser(adminMode, true)){
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + Pronouns.clearPronouns(data.getSender()));
		}
	}
	
	@IRCCommand(command = ".removePronouns", startOfLine = true, coexistWithJarvis = true)
	public void removePronouns(CommandData data){
		if(data.isAuthenticatedUser(adminMode, false)){
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + Pronouns.clearPronouns(data.getTarget()));
		}
	}

	// Authentication Required Commands
	@IRCCommand(command = ".join", startOfLine = true, coexistWithJarvis = true)
	public void enterChannel(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true))
			helen.joinChannel(data.getTarget());

	}

	@IRCCommand(command = ".leave", startOfLine = true, coexistWithJarvis = true)
	public void leaveChannel(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true))
			helen.partChannel(data.getTarget());

	}

	@IRCCommand(command = ".tell", startOfLine = true)
	public void tell(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			String str = Tells.sendTell(data.getTarget(), data.getSender(), data.getTellMessage(),
					(data.getChannel().isEmpty() ? true : false));
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + str);
		}
	}

	@IRCCommand(command = ".exit", startOfLine = true, coexistWithJarvis = true)
	public void exitBot(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, true)) {
			for(String channel : helen.getChannels()){
				helen.partChannel(channel,"Stay out of the revolver's sights...");
			}
			try{
				Thread.sleep(5000);
			}catch(Exception e){
				
			}
			helen.disconnect();
			System.exit(0);

		}
	}

	@IRCCommand(command = ".allProperties", startOfLine = true)
	public void getAllProperties(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			ArrayList<Config> properties = Configs.getConfiguredProperties(true);
			helen.sendMessage(data.getResponseTarget(),
					data.getSender() + ": Configured properties: " + buildResponse(properties));
		}
	}

	@IRCCommand(command = ".property", startOfLine = true)
	public void getProperty(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			ArrayList<Config> properties = Configs.getProperty(data.getTarget());
			helen.sendMessage(data.getResponseTarget(),
					data.getSender() + ": Configured properties: " + buildResponse(properties));
		}
	}

	@IRCCommand(command = ".setProperty", startOfLine = true)
	public void setProperty(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			String properties = Configs.setProperty(data.getSplitMessage()[1], data.getSplitMessage()[2],
					data.getSplitMessage()[3]);
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + properties);
		}
	}

	@IRCCommand(command = ".updateProperty", startOfLine = true)
	public void updateProperty(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			String properties = Configs.updateSingle(data.getSplitMessage()[1], data.getSplitMessage()[2],
					data.getSplitMessage()[3]);
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + properties);
		}
	}

	@IRCCommand(command = ".deleteProperty", startOfLine = true)
	public void deleteProperty(CommandData data) {
		if (data.isAuthenticatedUser(adminMode, false)) {
			String properties = Configs.removeProperty(data.getSplitMessage()[1], data.getSplitMessage()[2]);
			helen.sendMessage(data.getResponseTarget(), data.getSender() + ": " + properties);
		}
	}
	
	@IRCCommand(command = ".clearCache", startOfLine = true)
	public void clearCache(CommandData data){
		if(data.isAuthenticatedUser(adminMode, false)){
			Queries.clear();
			Configs.clear();
		}
	}

	private String buildResponse(ArrayList<? extends DatabaseObject> dbo) {
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(int i = 0; i < dbo.size(); i++){
			if(i != 0){
				str.append(dbo.get(i).getDelimiter());
			}
			str.append(dbo.get(i).toString());
		}
		str.append("}");
		return str.toString();
	}
}
