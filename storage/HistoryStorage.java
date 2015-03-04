package STORAGE;

import MODEL.Task;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * HistoryStorage class
 *
 * @author Qiyuan
 */
public class HistoryStorage extends Storage {
	
	public HistoryStorage(String filePath) throws IOException {
		Path storePath = Paths.get(filePath + "/history.txt").toAbsolutePath();
		setStorePath(storePath);
		checkAndCreateFile(storePath);
		
		openFile(getStorePath());
	}

	public ObservableList<Task> getHistory(){
		return getList();
	}

	// Overriding method to remove sorting
	public boolean storeTask(Task newTask) {
		getList().add(newTask);
		return true;
	}
}