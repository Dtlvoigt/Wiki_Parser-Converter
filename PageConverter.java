
//Author: Dylan Voigt
//Made for Winter 2020 Capstone

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class PageConverter
{
	public static String unconvertedFiles[];
	public static String title;
	public static String date;
	public static String entry;
	public static String entryAuthor;
	public static String comment;
	public static Vector<String> commentAuthors;
	public static Vector<String> categories;

	public static void main(String[] args)
	{
		try {
			//initialize vectors
			commentAuthors = new Vector<String>();
			categories = new Vector<String>();

			//load list of pages to be converted
			loadFiles();

			//loop through files
			for(int i = 0; i < unconvertedFiles.length; i++)
			{
				//pull needed data from current file
				extractData(unconvertedFiles[i]);

				//create new json file and enter data into it
				writeToJSON(unconvertedFiles[i]);

				//reset class variables
				commentAuthors.clear();
				categories.clear();
			}

		} catch(Exception e) 
		{
			System.out.println(e + " main");
		}
	}

//--------------------------------------------------------------------------

	//load list of file names to be converted
	public static void loadFiles()
	{
		//set path to current directory
		File directory = new File("unconverted_files");
		if(!directory.exists())
			System.exit(1);

		//set array to contain list of files
		unconvertedFiles = directory.list();
	}

//--------------------------------------------------------------------------

	//extract needed objects from current page
	public static void extractData(String currPage)
	{
		try {
			//set title for current page
			title = titleFormat(currPage);
			//title = currPage;

			//convert current file into a json object for parsing
			Object temp = new JSONParser().parse(new FileReader("unconverted_files/" + currPage));
			JSONObject page = (JSONObject) temp;

			//pull date object
			date = (String) page.get("date");

			//create text object of entire page to be parsed
			String pageText = (String) page.get("text");

			//parse text for main entry and comment
			entrySplit(pageText);
			
			//search main entry for author
			entryAuthor = findLastAuthor(entry);

			//search comment block for authors
			commentAuthors = findAuthors(comment);

			//search page for categories
			findCategories(pageText);

		} catch(Exception e)
		{
			System.out.println(e + " extractData()");
		}
	}

//--------------------------------------------------------------------------

	//formats the title
	public static String titleFormat(String pageName)
	{
		//remove file type from page name
		String[] noSpace = pageName.split(".json");
	
		//split title at capital letters. BigData -> Big Data
		/*String[] splitUp = noSpace[0].split("(?<!^)(?=[A-Z])"); 
		String fixedTitle = "";

		//recombine split words to make final title
		for(int i = 0; i < splitUp.length; i++)
		{
			if(i == splitUp.length - 1)
				fixedTitle += splitUp[i];
			else
				fixedTitle += splitUp[i] + " ";
		}
		return fixedTitle;*/
		return noSpace[0];
	}

//--------------------------------------------------------------------------

	//seperates the main entry from the comment block
	public static void entrySplit(String pageText)
	{
		//split entry from comments at first instance of '----'
		String[] sepText = pageText.split("----", 2);
		entry = sepText[0];
	
		//add '----' back to entry unless there is no comment section for this page
		if(sepText.length > 1)
		{
			comment = sepText[1];
			entry += "----";
		}
		else
			comment = "";
	}

//--------------------------------------------------------------------------
	
	//finds the last author in the entered text
	public static String findLastAuthor(String text)
	{
		//gather all authors in text but only keep last one
		Vector<String> authors = findAuthors(text);
		if(!authors.isEmpty())
			return authors.lastElement();
		else
			return "";
	}

//--------------------------------------------------------------------------

	//searches for all possible authors in the text
	public static Vector<String> findAuthors(String text)
	{
		Vector<String> newAuthors = new Vector<String>();

        	//reads through "authors.txt", which is a newline seperated text file, and adds each author to the string TreeSet "authorTree". Relies on .io.FileReader and .io.BufferedReader.
        	SortedSet<String> authorTree = new TreeSet<String>();
        	try {
        		BufferedReader br = new BufferedReader(new FileReader("../authors.txt"));
        		String sCurrentAuthor;
        		while ((sCurrentAuthor = br.readLine()) != null)
        		{
        	    		authorTree.add(sCurrentAuthor);
        	    	}
        	}
        	catch(Exception e) 
		{
			System.out.println(e + " main");
		}

		//search for authors with the format "-- AuthorName"
		String authPattern = "(--) ([a-zA-Z]*)";
		Pattern pattern = Pattern.compile(authPattern);
		Matcher matcher = pattern.matcher(text);

		//if a match is found, add the name to the author list
		while(matcher.find())
		{
			if(matcher.group(2).length() > 2 && !newAuthors.contains(matcher.group(2)) && authorTree.contains(matcher.group(2)))
				newAuthors.add(matcher.group(2));
		}

		//search for authors with the format "--AuthorName"
		String authPattern2 = "(--)([a-zA-Z]*)";
		pattern = Pattern.compile(authPattern2);
		matcher = pattern.matcher(text);

		//if a match is found, add the name to the author list
		while(matcher.find())
		{
			if(matcher.group(2).length() > 2 && !newAuthors.contains(matcher.group(2)) && authorTree.contains(matcher.group(2)))
				newAuthors.add(matcher.group(2));
		}

		return newAuthors;
	}

//--------------------------------------------------------------------------

	//finds all the categories present within the page
	public static void findCategories(String pageText)
	{
		//I will search for matches to Category and anything that follows throughout page
		String catPattern = "(Category.*)";

		//set pattern and matcher
		Pattern pattern = Pattern.compile(catPattern);
		Matcher matcher = pattern.matcher(pageText);		
		if(matcher.find())
			categories.add(matcher.group(1));
		/*{
			//categories are seperated by a ',' so split them apart 
			String[] cats = matcher.group(1).split(", ");

			//look at each category found
			for(int i = 0; i < cats.length; i++)
			{
				//split off the keyword and add it to the vector
				String[] splitCat = cats[i].split("Category");
				if(splitCat.length > 1 && splitCat[1] != "")
					categories.add(splitCat[1]);
			}
		}*/

	}

//--------------------------------------------------------------------------
	
	//puts information into new JSON file
	public static void writeToJSON(String pageName)
	{
		try {
			//create JSON object to store info
			JSONObject newPage = new JSONObject();

			//add objects to JSON object
			newPage.put("title", title);
			newPage.put("date", date);
			newPage.put("entry author", entryAuthor);
			newPage.put("entry", entry);
			newPage.put("comments", comment);
	
			//create JSON array to hold authors
			JSONArray authors = new JSONArray();
			for(int i = 0; i < commentAuthors.size(); i++)
				authors.add(commentAuthors.elementAt(i));
			newPage.put("comment authors", authors);
			
			//create JSON array to hold categories
			JSONArray cats = new JSONArray();
			for(int i = 0; i < categories.size(); i++)
				cats.add(categories.elementAt(i));
			newPage.put("categories", cats);
			
			//open directory
			File directory = new File("converted_files");
			if(!directory.exists())
				System.exit(1);

			//write data to new file
			PrintWriter writer = new PrintWriter(directory + "/" + pageName);
			writer.write(newPage.toJSONString());

			writer.flush();
			writer.close();

		} catch(Exception e) {
			System.out.println(e + "writeToJSON()");
		}
	}

}
