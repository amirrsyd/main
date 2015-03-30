package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import logic.CdLogic;

/**
 * Testing integration of Logic and Vault components.
 * 
 * @author qiyuan
 */
public class IntegrationTest {
	private CdLogic logic;
	@Before
	public void setUpTestEnv() throws IOException {
		logic = new CdLogic();
	}
	
	@After
	public void clearOutputFiles() {
		logic.clear();
	}
	@Test
	public void testAdd() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
	}
	
	@Test
	public void testComplete() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("\"task\" completed successfully", logic.executeCommand("complete task"));
	}
	
	@Test
	public void testDelete() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("\"task\" deleted successfully", logic.executeCommand("delete task"));
	}
	
	@Test
	public void testList() throws IOException {
		assertEquals("All tasks displayed", logic.executeCommand("list"));
	}
	
	@Test
	public void testEdit() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("edit complete", logic.executeCommand("edit task startdate 23/03/2015"));
	}
	
	@Test
	public void testUndoAdd() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("Undo: \"task\" removed from tasks", logic.executeCommand("undo"));
	}
	
	@Test
	public void testUndoEdit() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("edit complete", logic.executeCommand("edit task startdate 23/03/2015"));
		assertEquals("Undo: \"task\" has been restored to its original", logic.executeCommand("undo"));
	}
	
	@Test
	public void testUndoDelete() throws IOException {
		assertEquals("Task \"task\" successfully added", logic.executeCommand("add task 12/03/2015 12:00"));
		assertEquals("\"task\" deleted successfully", logic.executeCommand("delete task"));
		assertEquals("Undo: \"task\" moved back from trash to tasks", logic.executeCommand("undo"));
	}
}
