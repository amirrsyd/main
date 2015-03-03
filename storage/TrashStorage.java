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
 * TrashStorage class
 *
 * @author Qiyuan
 */
public class TrashStorage extends Storage {

	public TrashStorage(String filePath) {
		storePath = Paths.get(filePath + "/trash").toAbsolutePath();
		openFile(storePath);
	}

	public boolean emptyTrash() {
		trash.clear();
		return true;
	}

	public ObservableList<Task> getTrash() {
		return list;
	}
}