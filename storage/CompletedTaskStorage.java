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
 * CompletedTaskStorage class
 *
 * @author Qiyuan
 */
public class CompletedTaskStorage extends Storage {

	public CompletedTaskStorage(String filePath) {
		storePath = Paths.get(filePath + "/completed").toAbsolutePath();
		openFile(storePath);
	}

	public ObservableList<Task> getCompletedTasks() {
		return list;
	}
}