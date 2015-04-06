package logic;

import static org.junit.Assert.*;
import logic.CdLogic;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CdLogicTest {

	private static CdLogic logic ;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logic = new CdLogic() ;
	}

	@Test (expected = NullPointerException.class)
	//tests for invalid command entered 
	public void testInvalidExecCommand(String userCommand)throws IOException{
		if(userCommand.trim().equals("")){
			assertEqauls("Should return invalid command", logic.executeCommand("invalid command "
					+ "format :%1$s", userCommand)) ;

		}

	}
	@Test
	//tests if task can be added successfully
	//equivalence partitioning
	public void addTask() throws IOException { 
		testAddCommand("add first task ","Task \"mum's birthday party\" successfully added",
				"add mum's birthday party 13/03/2015 12:00 13/03/2015 18:00") ;
	}
	public void testAddCommand(String taskDet, String outcome, String userCommand) throws IOException{
		logic.CdLogic logic;
		//CdLogic cdLogic = null;
		assertEquals(taskDet, "Should add command successfully", logic.executeCommand(userCommand));
	}

	@Test
	//tests if program prompts invalid command if no task description is given
	//boundary value analysis
	public void addEmptyTask() throws IOException{
		invalidCommand("invalid command ", "add") ;
	}
	public void invalidCommand(String outcome, String userCommand) throws IOException{
		Object logic;
		assertEquals(outcome, logic.executeCommand(userCommand)) ;
	}

	@Test
	//tests delete command
	//equivalence partitioning
	public void deleteTask() throws IOException{
		testDelCommand("delete task",  "Task\" do laundry\" deleted successfully", "delete do laundry 25/03/2018 13:00 25/03/2015 14:00") ;
	}
	public void testDelCommand(String taskDet, String outcome, String userCommand) throws IOException{
		CdLogic cdLogic = null ;
		assertEquals(taskDet, outcome, cdLogic.executeCommand(userCommand)) ;
	}
	
	@Test
	public void testComplete() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("\"task\" completed successfully", logic.executeCommand("complete task"));
	}
	

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		logic.clear() ;
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	
}
