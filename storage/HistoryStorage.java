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
 * HistoryStorage class
 *
 * @author Qiyuan
 */
public class HistoryStorage extends Storage {
	public HistoryStorage(String filePath) {
		storePath = Paths.get(filePath + "/history").toAbsolutePath();
		openFile(storePath);
	}

	public ObservableList<Task> getHistory() {
		return list;
	}

	// Overriding method to remove sorting
	protected boolean storeTask(Task newTask) {
		list.add(newTask);
		return true;
	}
}