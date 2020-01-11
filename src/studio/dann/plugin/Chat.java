package studio.dann.plugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides a handler for formatting and communication with Minecraft's in-game chat.
 *
 * @author Dan Negura (contact.dann@icloud.com)
 */
public class Chat {

	/**
	 * Colour codes.
	 */
	public final ChatColor HIGHLIGHT;
	public final ChatColor NORMAL;

	/**
	 * Prefixes.
	 */
	public final String PREFIX;
	public final String EMPTY_PREFIX;

	/**
	 * Maximum horizontal line length.
	 */
	public final int MAX_LINE_LENGTH;

	/**
	 * Character widths.
	 */
	private static Map<Character, Integer> charWidths;

	/**
	 * Creates an instance of this class.
	 * It sets a prefix to be used before every chat message, and a highlight colour as well as a normal colour.
	 * @param prefix goes before each message
	 * @param highlight colour
	 * @param normal colour
	 */
	public Chat(String prefix, ChatColor highlight, ChatColor normal) {
		if (prefix == null || highlight == null || normal == null)
			throw new NullPointerException();
		
		// assign chat colors
		HIGHLIGHT = highlight;
		NORMAL = normal;
		
		// define prefix
		PREFIX = HIGHLIGHT + "[" + prefix + "] " + NORMAL;
		EMPTY_PREFIX = HIGHLIGHT + "| " + NORMAL;
		
		// define max length of line
		MAX_LINE_LENGTH = 155 - getStringWidth(PREFIX);
	}

	/**
	 * Sends a formatted in-game message to a player.
	 *
	 * @param player to send the message to
	 * @param lines of text to format and send
	 */
	public void toPlayer(Player player, String ... lines) {
		if (player == null || lines == null)
			throw new NullPointerException();
		 
		List<String> chatLines = new LinkedList<String>();

		// Format text.
		for (String line : lines) {
			// line is not empty
			if (!line.contentEquals("")
					&& !line.contentEquals(" ")) {
				List<String> split = splitLine(line, MAX_LINE_LENGTH);
				chatLines.addAll(split);
			}
			// line is empty
			else
				chatLines.add(line);
		}
		// Print message.
		for (int i = 0; i < chatLines.size(); i++) {
			if (i == 0)
				player.sendMessage(PREFIX + chatLines.get(i));
			else
				player.sendMessage(EMPTY_PREFIX + chatLines.get(i));
		}
	}

	/**
	 * Splits the line into a list of smaaller lines to fit the chat space.
	 * @param line to split
	 * @param maxLength of a line, value must be greater 0
	 * @return the split lines
	 */
	public List<String> splitLine(String line, int maxLength) {
		if (line == null)
			throw new NullPointerException();
		if (maxLength <= 0)
			throw new IllegalArgumentException();
		
		List<String> lines = new LinkedList<String>();
		String[] words = line.split(" ");
		int lineLength = 0;
		StringBuilder lineBuilder = new StringBuilder();
		
		for (String word : words) {
			int wordWidth = getStringWidth(word) + 1;
			wordWidth -= (countOccurences(word, NORMAL.toString()) * getStringWidth(NORMAL.toString()));
			wordWidth -= (countOccurences(word, HIGHLIGHT.toString()) * getStringWidth(HIGHLIGHT.toString()));
			if (lineLength + wordWidth >= maxLength) {
				lines.add(lineBuilder.toString());
				lineBuilder = new StringBuilder();
				lineLength = 0;
			}
			lineBuilder.append(word);
			lineBuilder.append(" ");
			lineLength += getStringWidth(word) + getCharWidth(' ');
		}
		
		if (lineBuilder.length() != 0)
			lines.add(lineBuilder.toString());
		
		return lines;
	}

	/**
	 * Counts the occurrence of a String inside another string.
	 * @param string to look into
	 * @param snippet to scan for
	 * @return the number of times the snippet was found inside the String
	 */
	public static int countOccurences(String string, String snippet) {
		if (string == null || snippet == null)
			throw new NullPointerException();

		int fromIndex = 0;
		int counter = 0;
        while ((fromIndex = string.indexOf(snippet, fromIndex)) != -1 ){
            counter++;
            fromIndex++;
        }
        return counter;
	}

	/**
	 * Returns the total width in thirds of Minecraft's character width.
	 * @param string to count the eidth of
	 * @return the length of the string
	 */
	public static int getStringWidth(String string) {
		if (string == null)
			throw new NullPointerException();

		int width = 0;
		for (char c : string.toCharArray())
			width += getCharWidth(c);
		return width;
	}

	/**
	 * Returns the width of a character in thirds of a Minecraft character 'a'.
	 * @param c character
	 * @return the width of c
	 */
	public static int getCharWidth(char c) {
		if (charWidths == null) {
			// initialize widths
			charWidths = new HashMap<Character, Integer>();
			charWidths.put('f', 2);
			charWidths.put('i', 1);
			charWidths.put('l', 1);
			charWidths.put('t', 2);
			charWidths.put('|', 1);
			charWidths.put('[', 2);
			charWidths.put(']', 2);
			charWidths.put('\'',1);
			charWidths.put('.', 1);
			charWidths.put(',', 1);
			charWidths.put('(', 2);
			charWidths.put(')', 2);
			charWidths.put('{', 2);
			charWidths.put('}', 2);
			charWidths.put('!', 1);
			charWidths.put('"', 2);
			charWidths.put(' ', 1);
		}
		return charWidths.getOrDefault(c, 3);
	}
}
