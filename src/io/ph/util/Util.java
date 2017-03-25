package io.ph.util;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.tika.Tika;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;

import io.ph.bot.Bot;
import io.ph.bot.model.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

public class Util {
	/**
	 * Resolve a Member from a Message.
	 * <p>
	 * If this Message contains a mention, it returns the first. Otherwise, 
	 * username must be the only parameter in the Message
	 * <p>
	 * First checks nicknames then usernames
	 * @param s String to check
	 * @return Member if found, null if not found
	 */
	public static Member resolveMemberFromMessage(Message msg) {
		if (msg.getMentionedUsers().size() > 0)
			return msg.getGuild().getMember(msg.getMentionedUsers().get(0));
		return resolveMemberFromMessage(getCommandContents(msg.getContent()), msg.getGuild());
	}

	/**
	 * Resolve a user from a String - String must be only their username.
	 * First checks nicknames then usernames
	 * @param s String to check
	 * @return User if found, null if not found
	 */
	public static Member resolveMemberFromMessage(String toCheck, Guild guild) {
		if (toCheck.isEmpty())
			return null;
		for (Member m : guild.getMembers()) {
			if (m.getEffectiveName().toLowerCase().startsWith(toCheck.toLowerCase())
					|| m.getUser().getName().toLowerCase().startsWith(toCheck.toLowerCase())) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Resolve a banned user from their name
	 * @param s String to check
	 * @param guild Guild to check in
	 * @return User if found, null if not found
	 */
	public static User resolveBannedUserFromString(String s, Guild guild) {
		for(User u : guild.getController().getBans().complete()) {
			if(u.getName().toLowerCase().startsWith(s.toLowerCase()))
				return u;
		}
		return null;
	}

	/**
	 * Get the contents of a command, if it has arguments
	 * Returns everything except the command and its prefix (split among a space)
	 * @param msg Message to parse
	 * @return String. Empty if there are no arguments
	 */
	public static String getCommandContents(Message msg) {
		return getCommandContents(msg.getContent());
	}
	/**
	 * Get the contents of a command, if it has arguments
	 * Returns everything except the first element of a split among a space
	 * @param s String to parse
	 * @return String. Empty if there are no arguments
	 */
	public static String getCommandContents(String s) {
		return combineStringArray(removeFirstArrayEntry(s.split(" ")));
	}

	/**
	 * Combine a string array into a single String
	 * @param arr String array
	 * @return String of combination
	 */
	public static String combineStringArray(String[] arr) {
		StringBuilder sb = new StringBuilder();
		for(String s : arr) {
			sb.append(s+" ");
		}
		return sb.toString().trim();
	}
	/**
	 * Remove the first item from a String array
	 * @param arr String array to manipulate
	 * @return Array without first element
	 */
	public static String[] removeFirstArrayEntry(String[] arr) {
		String[] toReturn = new String[arr.length - 1];
		for(int i = 1; i < arr.length; i++) {
			toReturn[i - 1] = arr[i];
		}
		return toReturn;
	}

	/**
	 * Remove the first item from a String array
	 * @param arr String array to manipulate
	 * @return Array without first element
	 */
	public static String[] removeLastArrayEntry(String[] arr) {
		String[] toReturn = new String[arr.length - 1];
		for(int i = 0; i < arr.length - 1; i++) {
			toReturn[i] = arr[i];
		}
		return toReturn;
	}

	/**
	 * Return if a member has permission
	 * @param member Member to check
	 * @param permission Permission to check
	 * @return True if they have permission, false if not
	 */
	public static boolean memberHasPermission(Member member, Permission permission) {
		if (permission.getJdaPerm() == null 
				&& member.getUser().getId().equals(Bot.getInstance().getConfig().getBotOwnerId())) {
			return true;
		}
		if (permission.getJdaPerm() != null && member.hasPermission(permission.getJdaPerm())) {
			return true;
		}
		return false;
	}

	/**
	 * Get the first parameter of a command based on a space split
	 * @param msg {@link Message} to parse
	 * @return String of first parameter based on a space split
	 */
	public static String getParam(Message msg) {
		return getParam(msg.getContent());
	}
	/**
	 * Get the first parameter of a command based on a space split
	 * @param str String to parse
	 * @return String of first parameter based on a space split
	 */
	public static String getParam(String str) {
		return removeFirstArrayEntry(str.split(" "))[0];
	}

	/**
	 * Returns json value for given String url
	 * @param url url to connect to
	 * @return
	 */
	public static JsonValue jsonFromUrl(String url) throws IOException {
		return Json.parse(stringFromUrl(url));
	}

	/**
	 * Returns string for given String url
	 * @param url url to connect to
	 * @return String of the body contents
	 */
	public static String stringFromUrl(String url) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader rd;
		URL link = new URL(url);
		if(link.getProtocol().equals("https")) {
			HttpsURLConnection conn = (HttpsURLConnection) link.openConnection();
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.connect();
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		} else {
			HttpURLConnection conn = (HttpURLConnection) link.openConnection();
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
			conn.connect();
			rd = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
		}
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	/**
	 * Convert milliseconds to mm:ss format
	 * @param milli long of milliseconds (not expecting Long values)
	 * @return min:sec
	 */
	public static String formatTime(long milli) {
		long sec = (milli / 1000) % 60;
		long min = (milli / 1000) / 60;
		if ((sec+"").length() == 1)
			return min+":0"+sec;
		return min+":"+sec;
	}

	/**
	 * Check if String input is a valid integer through {@link Integer#parseInt(String)}
	 * @param input String input
	 * @return True if int, false if not
	 */
	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	/**
	 * Check if String input is a valid double through {@link Double#parseInt(String)}
	 * @param input String input
	 * @return True if double, false if not
	 */
	public static boolean isDouble(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	/**
	 * Check if String input is a valid URL
	 * @param input String input
	 * @return True if URL, false if not
	 */
	public static boolean isValidUrl(String input) {
		try {
			new URL(input);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/**
	 * Similar implementation to JavaScript setTimeout
	 * @param runnable Runnable to run
	 * @param delay Delay in milliseconds
	 * @param async Run in async or blocking
	 */
	public static void setTimeout(Runnable runnable, int delay, boolean async){
		if (async) {
			new Thread(() -> {
				try {
					Thread.sleep(delay);
					runnable.run();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}).start();
		} else {
			try {
				Thread.sleep(delay);
				runnable.run();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	/**
	 * Save a URL to a file with a user-agent header
	 * @param url URL of file
	 * @param destinationFile File to download to
	 * @throws IOException 
	 */
	public static void saveFile(URL url, File destinationFile) throws IOException {
		InputStream in = null;
		FileOutputStream out = null;
		try {
			if(url.getProtocol().equals("https")) {
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			} else {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			}
			out = new FileOutputStream(destinationFile);
			int c;
			byte[] b = new byte[1024];
			while ((c = in.read(b)) != -1)
				out.write(b, 0, c);
			out.flush();
		} finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
		}
	}

	/**
	 * Get a MIME type from a URL
	 * @param url URL to check
	 * @return String of MIME type
	 */
	public static String getMimeFromUrl(URL url) {
		InputStream in;
		try {
			if (url.getProtocol().equals("https")) {
				HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			} else {
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("User-Agent",
						"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
				conn.connect();
				in = conn.getInputStream();
			}
			return new Tika().detect(in);
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Use Apache Tika to detect MIME type
	 * @param f File to detect
	 * @return String of MIME type
	 * @throws IOException Bad file
	 */
	public static String getMimeFromFile(File f) throws IOException {
		final Tika tika = new Tika();
		return tika.detect(f);
	}

	/**
	 * Return ordinal number (number + st, nd, rd, th)
	 * @param i Number to ordinalize (is that a word?)
	 * @return Ordinal String
	 */
	public static String ordinal(int i) {
		int mod100 = i % 100;
		int mod10 = i % 10;
		if(mod10 == 1 && mod100 != 11) {
			return i + "st";
		} else if(mod10 == 2 && mod100 != 12) {
			return i + "nd";
		} else if(mod10 == 3 && mod100 != 13) {
			return i + "rd";
		} else {
			return i + "th";
		}
	}

	/**
	 * Return a future instant from a string formatted #w#d#h#m
	 * @param string String to resolve from
	 * @return Instant in the future
	 */
	public static Instant resolveInstantFromString(String string) {
		Matcher matcher = Pattern.compile("\\d+|[wdhmWDHM]+").matcher(string);
		Instant now = Instant.now();
		LocalDateTime nowLDT = LocalDateTime.ofInstant(now, ZoneId.systemDefault());
		int previous = 0;
		while(matcher.find()) {
			String s = matcher.group().toLowerCase();
			if (Util.isInteger(s)) {
				previous = Integer.parseInt(s);
				continue;
			}
			switch(s) {
			case "w":
				nowLDT = nowLDT.plus(previous, ChronoUnit.WEEKS);
				break;
			case "d":
				nowLDT = nowLDT.plus(previous, ChronoUnit.DAYS);
				break;
			case "h":
				nowLDT = nowLDT.plus(previous, ChronoUnit.HOURS);
				break;
			case "m":
				nowLDT = nowLDT.plus(previous, ChronoUnit.MINUTES);
				break;
			default:
				break;
			}
		}
		return nowLDT.atZone(ZoneId.systemDefault()).toInstant();
	}
	
	/**
	 * Resolve a user's color with a default fallback
	 * @param member Member to check
	 * @param fallback Default color to fallback to
	 * @return Color or fallback
	 */
	public static Color resolveColor(Member member, Color fallback) {
		return member.getColor() == null ? fallback : member.getColor();
	}
	
	/**
	 * Resolve a member from a message
	 * @param msg Message to resolve from
	 * @return Member of this guild
	 */
	public static Member memberFromMessage(Message msg) {
		return msg.getGuild().getMember(msg.getAuthor());
	}
}
