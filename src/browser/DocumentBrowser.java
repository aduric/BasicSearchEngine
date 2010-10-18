package browser;

import index.GenerateIndex;
import index.InvertedIndex;

import javax.swing.*;

import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import scanner.*;
import search.LanguageModel;
import search.QueryResultComparator;
import search.Search;
import search.QueryResult;

/**
 * <p>
 * This is the Main class of the program. It shows a GUI interface to the user.
 * </p>
 * <p>
 * The main functionality of this is to give the user the ability to load .txt files,
 * parse the files into a intermediary representation format and create and load an
 * inverted index with specified files and be able to do search on it.
 * </p>
 * <p>
 * Buttons:
 * </p>
 * <p>
 * Load - Loads a .txt file, converts it to the intermidiary representation format and displays the file in the text area. Note that the file should not be huge
 * as this will create memory issues. documents.txt should definitely NOT be loaded here.
 * </p>
 * <p>
 * Save - Once a file is loaded into the text area, the user can save it as a .OUT file
 * </p>
 * <p>
 * Generate Index From Loaded Text - This will generate index files from the text that is loaded into the text area
 * </p>
 * <p>
 * Generate Index From .OUT File - This will allow the user to select a .OUT file to generate the index. When dealing with large files, this operation could 
 * take a while (anywhere from a few minutes to a few hours, depending on the file size and memory restrictions). A message will be displayed in the console
 * when the operation is finished.
 * </p>
 * <p>
 * Load Index - This will actually read the 3 index files (dictionary.txt, postings.txt and docids.txt) into memory and create an index. The index can only then
 * be searched. Please note that the 3 files specified MUST exist. This operation might take a few minutes.
 * </p>
 * <p>
 * Search - Searches the loaded index for the keywords specified in the text field. This could take a few minutes if searching for many terms.
 * </p>
 * 
 * TODO: Make sure put tokens in dictionary as lowercase
 * TODO: Make sure to compare tokens without case
 * TODO: Sanitize user input (ie, get rid of whitespace a around tokens in query, check for invalid characters)
 * 
 * @author adnan
 *
 */
public class DocumentBrowser extends JFrame implements ActionListener{

	
	JTextArea txtArea;
	JPanel contentPane;
	final JFileChooser fc;
	JLabel stats;
	JProgressBar progress;
	Timer timer;
	JButton genIndex, genOutIndex, ldIndex, btnSearch, btnDown, btnUp;
	JTextField queryField;
	JRadioButton rbtnVectorSearch, rbtnLangSearch;
	ButtonGroup rbtnSearchType;
	
	InvertedIndex ii;
	int pagerCurrentIndex = 0;
	ArrayList<QueryResult> ranking = null;
	
	public DocumentBrowser() {
		super("Adnan NLP Document Browser");				//provides text for the title bar
		
		//Add components here
		contentPane = new JPanel();
		fc = new JFileChooser();
		
		contentPane.setLayout(new FlowLayout());
		
		//Our top query box
		queryField = new JTextField(25);
		
		//Search type radio button
		rbtnVectorSearch = new JRadioButton("Vector Space Search", true);
		rbtnLangSearch = new JRadioButton("Language Model Search");
		rbtnSearchType = new ButtonGroup();
		rbtnSearchType.add(rbtnVectorSearch);
		rbtnSearchType.add(rbtnLangSearch);
		
		//The 'search' button
		btnSearch = new JButton("Search");
		btnSearch.setEnabled(false);
		btnSearch.addActionListener(this);
		
		//When we have search results in the text area, we want to give the user 10 results at a time
		//and the user can cycle with these two buttons
		btnDown = new JButton("<");
		btnUp = new JButton(">");
		btnDown.setEnabled(false);
		btnUp.setEnabled(false);
		btnUp.addActionListener(this);
		btnDown.addActionListener(this);
		
		//The load document button
		JButton ldDoc = new JButton("Load");
		ldDoc.addActionListener(this);
		
		//Save the document
		JButton saveDoc = new JButton("Save");
		saveDoc.addActionListener(this);
		
		//Generate Index files from the current txtArea - for offline processing
		genIndex = new JButton("Generate Index From Loaded Text");
		genIndex.addActionListener(this);
		genIndex.setEnabled(false);
		
		//Generate Index files from a file - for offline processing
		genOutIndex = new JButton("Generate Index From .OUT File");
		genOutIndex.addActionListener(this);
		
		
		//Load an index that already exists - must be prompted for 3 files (dictionary, postings, docids)
		ldIndex = new JButton("Load Index");
		ldIndex.addActionListener(this);
		
		//Label containing some statistics - when file is being loaded and read mostly
		stats = new JLabel("Progress");
		
		//JProgressBar
		progress = new JProgressBar(0, 100);

		
		//Our document brwoser area is a huge TextArea
		txtArea = new JTextArea(50, 80);
		
		JScrollPane scroll = new JScrollPane();
		scroll.getViewport().add(txtArea);

		
		contentPane.add(ldDoc);
		contentPane.add(saveDoc);
		contentPane.add(queryField);
		contentPane.add(btnSearch);
		contentPane.add(rbtnVectorSearch);
		contentPane.add(rbtnLangSearch);
		contentPane.add(genIndex);
		contentPane.add(genOutIndex);
		contentPane.add(ldIndex);
		contentPane.add(btnDown);
		contentPane.add(btnUp);
		contentPane.add(scroll);
		
		setContentPane(contentPane);
		
	}
	
	//handle the action events
	public void actionPerformed(ActionEvent evt) {
		if(evt.getSource() instanceof JButton) {
			if((((JButton)evt.getSource()).getText()).equals("Load")) {
				
				
				//Load the document
				
				File loadfile = null;
				
				int retVal = fc.showOpenDialog(this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
		            loadfile = fc.getSelectedFile();
		            //This is where a real application would open the file.
		            System.out.println("Opening: " + fc.getSelectedFile());
		            genIndex.setEnabled(true);
		        } else {
		        	System.out.println("Open command cancelled by user.");
		        }

				
				genIndex.setEnabled(true);
				//Create a tokenizer and append the lines into the TextArea
				//However, if the file is too big, we don't want to load it into the text area
				if((double)loadfile.length() < Math.pow(2.0, 24.0))
					loadText(loadfile);
				else 
					loadBigText(loadfile, new File("documents.out"));
					
				System.out.println("Done loading file");
				
			}
			else if((((JButton)evt.getSource()).getText()).equals("Save")) {
				//Load the document
				
				File savefile = null;
				
				int retVal = fc.showSaveDialog(this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
		            savefile = fc.getSelectedFile();
		            //This is where a real application would open the file.
		            System.out.println("Saving: " + savefile.getName());
		        } else {
		        	System.out.println("Open command cancelled by user.");
		        }
				
				//save the file to the specified file, if it doesn't exist, create one
				saveFile(savefile);
				
				System.out.println("File saved to "+savefile);
	
			}
			else if((((JButton)evt.getSource()).getText()).equals("Search")) {
				//Search the document for the query
				System.out.println("Search document");
				
				pagerCurrentIndex = 0;

				String myQuery = queryField.getText();
				
				//Find out which search we use by querying the radiobutton selected
					if(rbtnVectorSearch.isSelected()) {
						//Get the ranking by doing a vector space search
						System.out.println("Vector Space Model Search");
						this.ranking = Search.performSearch(myQuery, ii);
					}
					else if(rbtnLangSearch.isSelected()) {
						//Get the ranking by doing a language model search
						System.out.println("Language Model Search");
						this.ranking = LanguageModel.performSearch(myQuery, ii, 0.8, LanguageModel.searchType.JelinekMercer);
					}
					else
						System.out.println("Please select a search type.");
				
				
				//Sort the results by the similarity ranking in descending order
				Collections.sort(ranking, new QueryResultComparator("simScore"));
				
				//enable cycle buttons if there are more than 10 results
				if(ranking.size() > 10) {
					btnUp.setEnabled(true);
					btnDown.setEnabled(true);
				}

				//print first 10 results
				pager(pagerCurrentIndex);
				
				//int initStart = 0, initEnd = 10;
				
				/*for(int r = initStart; r < initEnd; r++) {
					txtArea.append(String.format("%d: %s\n", r, ranking.get(r).toString()));
				}
				
				int rcount = 0;
				for(QueryResult s: ranking) {
					System.out.println(s);
					txtArea.append(String.format("%d: %s\n", rcount, s.toString()));
					rcount++;
				}*/
				
			}
			else if((((JButton)evt.getSource()).getText()).equals(">")) {
				pagerCurrentIndex+=10;
				pager(pagerCurrentIndex);
			}
			else if((((JButton)evt.getSource()).getText()).equals("<")) {
				pagerCurrentIndex-=10;
				pager(pagerCurrentIndex);
			}
			else if((((JButton)evt.getSource()).getText()).equals("Generate Index From Loaded Text")) {
				//generate index files for offline processing from the text in txtArea
				System.out.println("Generate Index From Loaded Text");
				
	            //Need to generate InvertedIndex from the text currently in txtArea (create BufferedReader)
	            GenerateIndex.createIndex(TokenizerFactory.createTokenizer(TokenizerFactory.TokenizerType.DictionaryTokenizer, new StringReader(txtArea.getText())));
	            
	            //Load the index into memory
	            ii = GenerateIndex.loadIndex("dictionary.txt", "postings.txt", "docids.txt");
	            
				
				
			}
			else if((((JButton)evt.getSource()).getText()).equals("Generate Index From .OUT File")) {
				//generate index files for offline processing from another file
				System.out.println("Generate Index From .OUT File");
				
				try {
				
				File genFile = null;
				int retVal = fc.showOpenDialog(this);
				if (retVal == JFileChooser.APPROVE_OPTION) {
		            genFile = fc.getSelectedFile();
		            //This is where a real application would open the file.
		            System.out.println("Opening: " + fc.getSelectedFile());
		            
		            //Need to generate Inverted Index from 'genFile'
		            GenerateIndex.createIndex(TokenizerFactory.createTokenizer(TokenizerFactory.TokenizerType.DictionaryTokenizer, new BufferedReader(new InputStreamReader(new FileInputStream(genFile)))));
		            
		        } else {
		        	System.out.println("Open command cancelled by user.");
		        }
				
				}catch(IOException e) {
					System.out.println("Error: " + e);
				}
				
			}
			else if((((JButton)evt.getSource()).getText()).equals("Load Index")) {
				//load index into memory from inverted index files dictionary.txt, postings.txt, docids.txt
				System.out.println("Load Index");
				
	            //Load the index into memory
	            ii = GenerateIndex.loadIndex("dictionary.txt", "postings.txt", "docids.txt");
	            
	            btnSearch.setEnabled(true);
			}
			
			
		}
			//frame.update(this);
	}
	
	private void saveFile(File filename) {
		
		try {
		
		//Check if the file exists and it is writable
		if(filename.exists()) {
			if(!filename.canWrite()) {
		
				throw new IOException();
				
			}
		}
		else {//DNE, create a file
			filename.createNewFile();
		}
		
		//Save the file by creating it and dumping out all the contents from txtArea
		PrintWriter pwrite = new PrintWriter(filename);
	
		txtArea.write(pwrite);
		
		//This might be a bad idea...but it works
		//pwrite.print(txtArea.getText());
		
		} catch(IOException e) {
			System.out.println("File did not save. Could not create the file.");
		}
		
	}
	
	private void loadBigText(File filename, File tofile) {
		
		System.out.println("File is too big for the text area. The file will be automatically written out to: " + tofile.getPath());
		
		//Create our toknizer for this file	
		AbstractTokenizer nlTok = null;
		FileWriter pw = null;
		try {
		
			nlTok = TokenizerFactory.createTokenizer(TokenizerFactory.TokenizerType.SGMLTokenizer, new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
			//Create printwriter to write to a file
			pw = new FileWriter(tofile, true);
		
			System.out.println("Reading file: "+filename);
			
			stats.setText("Reading file...");
			
			System.out.println();
			System.out.print("Working");
			int oldValue = 0;
			while(nlTok.hasNext()) {
					//String p = (String)nlTok.next();
					String p = ((SGMLTokenizer)nlTok).BufferedNext(65536); //2 ^ 16 = 65536
					if(p!=null) {
						
						//Write this directly to a file
						
						pw.append(p);
						pw.flush();
						
						//stats.setText(String.format("Reading file: (%d / %d) bytes read.", nlTok.dataRead(), nlTok.totalData()));
						//System.out.println(stats.getText());
						int tmpval = ((int)((double)((double)nlTok.dataRead() / (double)filename.length()) * 100));
						//stats.setText(String.format("%d",tmpval));
						if(tmpval!=oldValue) {
							System.out.print(".");
							oldValue = tmpval;
						}
					}
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.print("Done.");
		System.out.println();			
	}
	
	
	private void loadText(File filename) {
		
		//Create our toknizer for this file	
		AbstractTokenizer nlTok = null;
		try {
		
			nlTok = TokenizerFactory.createTokenizer(TokenizerFactory.TokenizerType.SGMLTokenizer, new BufferedReader(new InputStreamReader(new FileInputStream(filename))));
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Reading file: "+filename);
		
		stats.setText("Reading file...");
		
		System.out.println();
		System.out.print("Working...");
		int oldValue = 0;
		while(nlTok.hasNext()) {
				//String p = (String)nlTok.next();
				String p = ((SGMLTokenizer)nlTok).BufferedNext(65536); //2 ^ 16 = 65536
				if(p!=null) {
					txtArea.append(p);
					//System.out.print(p);
					//stats.setText(String.format("Reading file: (%d / %d) bytes read.", nlTok.dataRead(), nlTok.totalData()));
					int tmpval = ((int)((double)((double)nlTok.dataRead() / (double)filename.length()) * 100));
					//stats.setText(String.format("%d",tmpval));
					if(tmpval!=oldValue) {
						System.out.print(".");
						oldValue = tmpval;
					}
				}
		}
		System.out.print("Done.");
		System.out.println();			
	}
	
	/**
	 * 
	 * Print the results from a certain start position
	 * 
	 */
	private void pager(int start) {
		//print out the results
		txtArea.setText(null);
		for(int r = start; r < start+10; r++) {
			if(r >= ranking.size() || r < 0) break;
			txtArea.append(String.format("%d: %s\n", r, this.ranking.get(r).toString()));
		}
		
	}
	
	public static void main(String args[]) {
		JFrame frame = new DocumentBrowser();				//The framework() constructor is used to create a new instance
									//of the JFrame class. This is the application's main window
		
		WindowListener l = new WindowAdapter() {		//Even-handling code to close the frame on exit		
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		};
		frame.addWindowListener(l);				//Puts the WindowListener is 'listen' mode in the frame
		
		frame.setSize(800, 800);
		
		//frame.pack();						//The frame is resized to the smallest possible surface area
		frame.setVisible(true);					//Makes the frame visible
		
	}
	
}


