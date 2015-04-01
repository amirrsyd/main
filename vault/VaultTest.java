package vault;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import model.Task;

import org.junit.Before;
import org.junit.Test;

public class VaultTest {
	private String vaultPath;
	private TaskVault taskVault;
	private CompletedTaskVault completedTaskVault;
	private HistoryVault historyVault;
	private TrashVault trashVault;
	private Task testingTask;
	private Task testingTask2;
	private Task testingTask3;
	
	@Before
	public void setUpTestEnv() throws IOException {
		vaultPath = System.getProperty("user.dir");
		taskVault = new TaskVault(vaultPath);
		completedTaskVault = new CompletedTaskVault(vaultPath);
		historyVault = new HistoryVault(vaultPath);
		trashVault = new TrashVault(vaultPath);
		taskVault.createTask("", "first", null, null, null, null);
		testingTask = new Task("", "first", null, null, null, null);
		testingTask2 = testingTask;
		testingTask3 = new Task("", "first", null, null, null, null);
		historyVault.storeTask(testingTask);
		historyVault.storeTask(new Task("", "last", null, null, null, null));
	}
	
	@Test
	public void testAllVaultsExist() {
		assertNotNull(taskVault);
		assertNotNull(completedTaskVault);
		assertNotNull(historyVault);
		assertNotNull(trashVault);
	}
	
	@Test
	public void testCreateTask() {
		assertTrue(taskVault.createTask("1", "", null, null, null, null));
		assertNotNull(taskVault.getTask("1"));
	}
	
	@Test
	public void testCompleteTask() {
		assertFalse(taskVault.completeTask("", null));
		assertFalse(taskVault.completeTask(null, completedTaskVault));
		assertTrue(taskVault.completeTask("", completedTaskVault));
		assertNotNull(completedTaskVault.getTask(""));
	}
	
	@Test
	public void testClear() {
		assertTrue(trashVault.clear());
	}
	
	@Test
	public void testGetTask() {
		assertNotNull(taskVault.getTask(""));
		assertNull(taskVault.getTask(null));
	}
	
	/**
	 * Testing with Equivalence Partitioned values
	 * passing in a new task that has null taskName
	 * passing in null
	 * passing in a new Task that has fields filled exactly like an existing task
	 * passing in a different reference to an already contained task
	 * passing in an already contained task to itself
	 */
	@Test
	public void testStoreTask() {
		assertFalse(taskVault.storeTask(new Task()));
		assertFalse(taskVault.storeTask(null));
		assertTrue(taskVault.storeTask(testingTask));
		assertTrue(taskVault.storeTask(testingTask3));
		assertFalse(taskVault.storeTask(testingTask2));
		assertFalse(taskVault.storeTask(taskVault.getTask("")));
	}
	
	@Test
	public void testGetList() {
		assertNotNull(taskVault.getList());
		assertNotNull(completedTaskVault.getList());
		assertNotNull(trashVault.getList());
		assertNotNull(historyVault.getList());
	}
	
	@Test
	public void testDeleteTask() {
		assertTrue(taskVault.deleteTask("", trashVault));
		assertNull(taskVault.search(""));
		assertNotNull(trashVault.search(""));
		assertFalse(taskVault.deleteTask(null, trashVault));
	}
 
	/**
	 * Testing with boundary values
	 * passing in the name of an existing task'
	 * testing if it is really removed
	 * passing in null
	 * passing in the name of a not existing task
	 */
	@Test
	public void testRemove() {

		assertTrue(taskVault.remove(""));
		assertNull(trashVault.search(""));
		assertFalse(taskVault.remove(null));
		assertFalse(taskVault.remove("1"));
	}
	
	@Test
	public void testSearch() {
		assertNull(taskVault.search(null));
	}
	
	/**
	 * Testing that pop will actually remove the last occurrence of object 
	 */
	@Test
	public void testHistoryPop() {
		assertNotNull(historyVault.pop(""));
		assertTrue(historyVault.search("").getComment().equals("first"));
	}
	
	/**
	 * Testing that storeTask will store an identical object with the 
	 * same reference.
	 * Testing that storeTask will store an identical object with a 
	 * different reference.
	 * Testing that storeTask will store a different object but with identical 
	 * fields.
	 */
	@Test
	public void testHistoryOverriddenStoreTask() {
		assertTrue(historyVault.storeTask(testingTask));
		assertTrue(historyVault.storeTask(testingTask2));
		assertTrue(historyVault.storeTask(testingTask3));
	}
	
	@Test
	public void testPop() {
		assertNotNull(historyVault.pop(""));
		assertNull(historyVault.pop("1"));
	}

}
