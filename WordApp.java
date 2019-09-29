import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Scanner;
import java.util.concurrent.*;
//model is separate from the view.

public class WordApp {
//shared variables
	static int noWords = 4;
	static int totalWords;

	static int frameX = 1000;
	static int frameY = 600;
	static int yLimit = 480;

	static WordDictionary dict = new WordDictionary(); // use default dictionary, to read from file eventually

	static WordRecord[] words;
	static volatile boolean done = true; // must be volatile
	static volatile boolean started = false; // must be volatile
	static Score score = new Score();

	static WordPanel w;

	public static void setupGUI(int frameX, int frameY, int yLimit) {
		// Frame init and dimensions
		JFrame frame = new JFrame("WordGame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(frameX, frameY);

		JPanel g = new JPanel();
		g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS));
		g.setSize(frameX, frameY);

		w = new WordPanel(words, yLimit);
		w.setSize(frameX, yLimit + 100);
		g.add(w);

		JPanel txt = new JPanel();
		txt.setLayout(new BoxLayout(txt, BoxLayout.LINE_AXIS));
		JLabel caught = new JLabel("Caught: " + score.getCaught() + "    ");
		JLabel missed = new JLabel("Missed:" + score.getMissed() + "    ");
		JLabel scr = new JLabel("Score:" + score.getScore() + "    ");
		txt.add(caught);
		txt.add(missed);
		txt.add(scr);

		// [snip]

		final JTextField textEntry = new JTextField("", 20);
		textEntry.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String text = textEntry.getText();
				for (int i = 0; i < noWords; i++) {
					if (words[i].matchWord(text)) {
						score.caughtWord(text.length());
					}
				}
				textEntry.setText("");
				textEntry.requestFocus();
			}
		});

		txt.add(textEntry);
		txt.setMaximumSize(txt.getPreferredSize());
		g.add(txt);

		// New Thread to keep checking the caught, missed and score
		(new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					caught.setText("Caught: " + score.getCaught() + "    ");
					missed.setText("Missed:" + score.getMissed() + "    ");
					scr.setText("Score:" + score.getScore() + "    ");
				}
			}
		})).start();

		JPanel b = new JPanel();
		b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));

		JButton pauseB = new JButton("Pause");

		// add the listener to the jbutton to handle the "pressed" event
		pauseB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (started) {
					if (!done) {
						done = true;
						pauseB.setText("Unpause");
						textEntry.setEditable(false);
					} else {
						done = false;
						// w.run();
						pauseB.setText("Pause");
						textEntry.setEditable(true);

					}
				}
			}
		});

		JButton startB = new JButton("Start");
		// add the listener to the jbutton to handle the "pressed" event
		startB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (done) {
					done = false;
					started = true;
					pauseB.setText("Pause");
					textEntry.requestFocus(); // return focus to the text entry field
					textEntry.setEditable(true);
					gameOn();
				} else {
					pauseB.setText("Pause");
					textEntry.requestFocus(); // return focus to the text entry field
					textEntry.setEditable(true);
					gameOn();

				}
			}
		});

		JButton quitB = new JButton("Quit");

		quitB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		JButton endB = new JButton("End");

		endB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				done = true;
				started = false;
				// In case game is ended while paused
				pauseB.setText("Pause");
				textEntry.setEditable(true);
				gameOn();
			}
		});

		b.add(startB);
		b.add(endB);
		b.add(pauseB);
		b.add(quitB);
		g.add(b);

		frame.setLocationRelativeTo(null); // Center window on screen.
		frame.add(g); // add contents to window
		frame.setContentPane(g);
		// frame.pack(); // don't do this - packs it into small space
		frame.setVisible(true);

	}

	public static String[] getDictFromFile(String filename) {
		String[] dictStr = null;
		try {
			Scanner dictReader = new Scanner(new FileInputStream(filename));
			int dictLength = dictReader.nextInt();
			// System.out.println("read '" + dictLength+"'");

			dictStr = new String[dictLength];
			for (int i = 0; i < dictLength; i++) {
				dictStr[i] = new String(dictReader.next());
				// System.out.println(i+ " read '" + dictStr[i]+"'"); //for checking
			}
			dictReader.close();
		} catch (IOException e) {
			System.err.println("Problem reading file " + filename + " default dictionary will be used");
		}
		return dictStr;
	}

	public static void main(String[] args) {
		// deal with command line arguments
		totalWords = Integer.parseInt(args[0]); // total words to fall
		noWords = Integer.parseInt(args[1]); // total words falling at any point
		assert (totalWords >= noWords) : " Not enough words in dictionary"; // this could be done more neatly
		String[] tmpDict = getDictFromFile(args[2]); // file of words
		if (tmpDict != null)
			dict = new WordDictionary(tmpDict);

		WordRecord.dict = dict; // set the class dictionary for the words.

		words = new WordRecord[noWords]; // shared array of current words

		// [snip]

		setupGUI(frameX, frameY, yLimit);
		// Start WordPanel thread - for redrawing animation
		gameOn();
		JOptionPane.showMessageDialog(w,
				"Press Start to start/restart words falling\nPress Pause/Unpause to pause/unpause game\nPress End to end game\nPress Quit to close GUI");
		JOptionPane.showMessageDialog(w,
				"Type the words once they leave the blue and before they hit the red\nScore is based on length of words caught\n"
						+ totalWords + " words will fall\nTry not to miss any");

		// new Thread to keep running through the game
		(new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) { // Keep running forever
					if (score.getTotal() >= 10 && !done) { // Show end game message
						JOptionPane.showMessageDialog(w, "Your score is: " + score.getScore() + "\nYou missed: "
								+ score.getMissed() + " words\nYou Caught: " + score.getCaught() + " words");
						done = true;
						gameOn();
					}
					if (!done) {
						// new Thread to animate
						(new Thread(new Runnable() {
							@Override
							public void run() {
								for (int i = 0; i < noWords; i++) {
									words[i].drop();
									if (words[i].dropped()) {
										score.missedWord();
										words[i].resetWord();
									}
								}
							}
						})).start();
						w.run();
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		})).start();
	}

	// Must be called when End is pressed and at the beginning of the game in main
	public static void gameOn() {
		score.resetScore();
		w.run();
		int x_inc = (int) frameX / noWords;
		// initialize shared array of current words

		for (int i = 0; i < noWords; i++) {
			words[i] = new WordRecord(dict.getNewWord(), i * x_inc, yLimit);
		}
	}

}
