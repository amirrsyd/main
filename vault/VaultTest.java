package vault;

import static org.junit.Assert.*;

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
	
	@Before
	public void setUpTestEnv() throws IOException {
		vaultPath = System.getProperty("user.dir");
		taskVault = new TaskVault(vaultPath);
		completedTaskVault = new CompletedTaskVault(vaultPath);
		historyVault = new HistoryVault(vaultPath);
		trashVault = new TrashVault(vaultPath);
		taskVault.createTask("", "", null, null, null, null);
	}
	@Test
	public void testCreateTask() {
		assertTrue(taskVault.createTask("1", "", null, null, null, null));
		assertNotNull(taskVault.getTask("1"));
	}
	
	@Test
	public void testCompleteTask() {
		assertTrue(taskVault.completeTask("", completedTaskVault));
		assertNotNull(completedTaskVault.getTask(""));
	}
	
	@Test
	public void testGetNextTask() {
		assertNotNull(taskVault.getNextTask());
	}
	
	@Test
	public void testEmptyTrash() {
		assertTrue(trashVault.emptyTrash());
	}
	
	@Test
	public void testStoreTask() {
		assertTrue(historyVault.storeTask(new Task()));
	}
	
	@Test
	public void testCompletedTaskVault() {
		assertNotNull(completedTaskVault);
	}
	
	@Test
	public void testGetList() {
		assertNotNull(taskVault.getList());
		assertNotNull(completedTaskVault.getList());
		assertNotNull(trashVault.getList());
		assertNotNull(historyVault.getList());
	}
	
	@Test
	public void testGetTask() {
		assertNotNull(taskVault.getTask(""));
	}
	
	@Test
	public void testDeleteTask() {
		assertTrue(taskVault.deleteTask("", trashVault));
	}

}
