package gui.view;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import gui.MainApp;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class GuiTester {
	
	Throwable rethrownException = null;

	@Before
	public void resetException() {
		rethrownException = null;
	}

	@Test
    public void testPrepareLabelsForHeader() throws Throwable {
		
		Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                new JFXPanel(); // Initializes the JavaFx Platform
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
	                    try {
	                    	
	                    	//Actual test starts here, inside the javafx thread
	                    	System.out.println("Running test 1");
	                    	Label[] results = new Label[7];
	                        TaskOverviewController tester = new TaskOverviewController(); // Create an instance
	                        tester.setMonthHeader(new Label());
	                        tester.prepareLabelsForHeader(results);
	                        String expectedMonthHeader = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)+ " " + String.valueOf( LocalDate.now().getYear());
	                        assertEquals(results[0].getText(), "Sun");
	                        assertEquals(results[1].getText(), "Mon");
	                        assertEquals(results[2].getText(), "Tues");
	                        assertEquals(results[3].getText(), "Wed");
	                        assertEquals(results[4].getText(), "Thurs");
	                        assertEquals(results[5].getText(), "Fri");
	                        assertEquals(results[6].getText(), "Sat");
	                        assertEquals(tester.getMonthHeader().getText(), expectedMonthHeader);
	                        System.out.println("Done test 1");
	                        //End of test
	                        
	                    } catch(AssertionError e) {
	                    	//Exception is propagate to the top
	                    	rethrownException = e;
	                    }
                    }
                });
            }
        });
        thread.start();// Initialize the thread
        Thread.sleep(1000);
        if(rethrownException != null) {
        	throw rethrownException;
        }
	}
	
	@Test
	public void testPrepareLabelsForCalendar() throws Throwable {
		
		Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                new JFXPanel(); // Initializes the JavaFx Platform
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
	                    try {
	                    	//Actual test starts here, inside the javafx thread
	                    	System.out.println("Running test 2");
	                    	TaskOverviewController tester = new TaskOverviewController();
	                    	tester.setDateNumbers(new Label[7][7]);
	                    	int inputYear = LocalDate.now().getYear();
	                    	Month inputMonth = LocalDate.now().getMonth();
	                    	
	                    	//Setting up expected results
	                        LocalDate monthToShow = LocalDate.of(inputYear, inputMonth, 1);
	                    	Label[][] expectedDateNumbers = new Label[7][7];
	                		int offset = 0;
	                		
	                		//If monday we offset by 1 day to fill the calendar and same for tuesday, wednesday, thursday and so on...
	                		if(monthToShow.getDayOfWeek().getValue() > 1) {
	                			offset = monthToShow.getDayOfWeek().getValue();
	                		}
	                		//However, on sunday the offset is 7 so that we show 1 row of the previous month on top
	                		else {
	                			offset = 7;
	                		}
	                		
	                		monthToShow = monthToShow.minusDays(offset);
	                		
	                		for(int i = 0; i < 6; i++) {
	                			for(int j = 0; j < 7; j++) {
	                				expectedDateNumbers[i][j] = new Label(String.valueOf(monthToShow.getDayOfMonth()));
	                				monthToShow = monthToShow.plusDays(1);
	                			}
	                		}
	                		
	                		//Executing the actual setup
	                		tester.prepareLabelsForCalendar(inputMonth, inputYear);
	                		
	                		//Testing if expected and result is same
	                		for(int i = 0; i < 6; i++) {
	                			for(int j = 0; j < 7; j++) {
	                				assertEquals(tester.getDateNumbers()[i][j].getText(), expectedDateNumbers[i][j].getText());
	                			}
	                		}
	                		
	                		System.out.println("Done test 2");
	                        //End of test
	                		
	                    } catch(AssertionError e) {
	                    	//Exception is propagate to the top
	                    	rethrownException = e;
	                    }
                    }
                });
            }
        });
        thread.start();// Initialize the thread
        Thread.sleep(1000);
        if(rethrownException != null) {
        	throw rethrownException;
        }
	}
}
