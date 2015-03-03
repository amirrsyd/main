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
 * FileStorage class
 *
 * @author Qiyuan
 */
public class TaskStorage extends Storage {
	
	public TaskStorage(String filePath) {
		storePath = Paths.get(filePath + "/taskList").toAbsolutePath();
		openFile(storePath);
	}

	public boolean createTask(String taskName, String taskDescription, 
		                      LocalDate startDate, LocalTime startTime, 
	                          LocalDate endDate, LocalTime endTime) {
		Task task = new Task(taskName, taskDescription, startDate, startTime, 
			                 endDate, endTime);
		storeTask(task);
		return true;
	}

	public Task getNextTask() {
		return list.get(ZERO);
	}

	public boolean completeTask(String taskName, CompletedTaskStorage completedTasks) {
		Task task = search(taskName);
		list.remove(task);
		completedTasks.storeTask(task);
		return true;
	}

	public ObservableList<Task> getTasks() {
		return list;
	}
}