This program was created as part of the backend for our 2020 Capstone team project. 
Full Backend directory: https://github.com/nleong2/100-favorites-preservation

A program that creates new JSON files with organized components

Input files are of the form:

```
{
	"date": ""
	"text": ""
}
```

Output files have the following fields:

```
{
	"title": ""
	"date": ""
	"entry": ""
	"entry author": ""
	"comments": ""
	"comment authors": []
	"categories": []
}
```
##### Note that the order of the fields doesn't matter for JSON files and the converted files may not match this layout

This program loads in one file at a time from /unconverted_files, extracts the needed fields and then outputs the file to /converted_files

Compile with: 
```
javac -cp "json-simple-1.1.jar" PageConverter.java
```

Run with: 
```
java -cp "json-simple-1.1.jar" PageConverter.java
```
