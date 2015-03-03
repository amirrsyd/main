package com.commando.storage;

import com.commando.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import java.nio.charset.Charset;

/**
 * Storage class
 *
 * @author Qiyuan
 */
public class Storage {
	private final Charset CHAR_SET = Charset.forName("US-ASCII");
	private final int ZERO = 0;
	private final int FIELD_NUMBER = 6;

	private ObservableList<Task> list;
	private Path storePath;

	public Storage(String filePath) {
		storePath = Paths.get(filePath).toAbsolutePath();
		openFile(storePath);
	}

	protected void openFile(Path storePath) {
		list = new ObservableList<Task>();
		try (BufferedReader reader = Files.newBufferedReader(filePath_, 
		                             CHAR_SET)) {

		    String line = reader.readLine();
		    while (line != null) {
		    	Task task = new Task(line);
		    	Vector<String> taskLines = new Vector<String>();
		    	while (line != "") {
		    		line = reader.readLine();
		    		taskLines.add(line);
		    	}
		    	if (taskLines.size() < FIELD_NUMBER) {
		    		taskLines.add(null);
		    	}
		    	task.setComment(taskLines.remove(ZERO));
		    	task.setStartDate(taskLines.remove(ZERO));
		    	task.setStartTime(taskLines.remove(ZERO));
		    	task.setEndDate(taskLines.remove(ZERO));
		    	task.setEndTime(taskLines.remove(ZERO));
		    	storeTask(task);
		    	line = reader.readLine();
		    }
		} catch (IOException error) {
			System.err.format("%s%n", error);
		}
	}

	public Task getTask(String taskName) {
		Task task = search(taskName);
		return task;
	}

	protected boolean storeTask(Task newTask) {
		list.add(newTask);
		FXCollections.sort(taskList);
		return true;
	}

	public boolean deleteTask(String taskName, TrashStorage trash) {
		Task task = search(taskName);
		trash.storeTask(task);
		list.remove(task);
	}

	public ObservableList<Task> getList() {
		return list;
	}

	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(storePath, 
			                         CHAR_SET, StandardOpenOption.CREATE, 
			                         StandardOpenOption.WRITE, 
			                         StandardOpenOption.SYNC)) {
			while (!list.isEmpty()) {
				Task task = list.remove(ZERO);
				writer.write(task.getTaskName());
				if (task.getComment != null) {
					writer.write(task.getComment());
				}
				if (task.getStartDate() != null) {
					writer.write(task.getStartDate().toString());
					writer.write(task.getStartTime().toString());
				}
				if (task.getEndDate() != null) {
					writer.write(task.getEndDate().toString());
					writer.write(task.getEndTime().toString());
				}
				writer.newLine();
			}
		} catch (IOException error) {
			System.err.format("%s%n", error);
		}
	}

	private Task search(String taskName) {
		//search task
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getTaskName() == taskName) {
				return list.get(i);
			}
		}
		return null;
	}
}