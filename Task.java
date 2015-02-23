package com.commando.model;

import java.time.LocalDate;
import java.time.LocalTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for a Task
 * 
 * @author Kyle
 *
 */

public class Task {
	private final StringProperty taskName;
	private final StringProperty comment;
	
	private final ObjectProperty<LocalDate> startDate;
	private final ObjectProperty<LocalTime> startTime;
	
	private final ObjectProperty<LocalDate> dueDate;
	private final ObjectProperty<LocalTime> dueTime;
	
	/**
	 *  Default constructor.
	 */
	public Task() {
		this(null);
	}
	
	/**
	 * Constructor with some initial data.
	 * 
	 * @param taskName
	 */
	public Task(String taskName) {
		this.taskName = new SimpleStringProperty(taskName);
		
		// Some dummy data
		this.comment = new SimpleStringProperty("-");
		this.startDate = new SimpleObjectProperty<LocalDate>(LocalDate.of(1992, 9, 12));
		this.startTime = new SimpleObjectProperty<LocalTime>(LocalTime.of(12,0));
		this.dueDate = new SimpleObjectProperty<LocalDate>(LocalDate.of(1992, 9, 12));
		this.dueTime = new SimpleObjectProperty<LocalTime>(LocalTime.of(12,0));
	}
	
	public String getTaskName() {
		return taskName.get();
	}
	
	public void setTaskName(String taskName) {
		this.taskName.set(taskName);
	}
	
	public StringProperty taskNameProperty() {
		return taskName;
	}
	
	public String getComment() {
		return comment.get();
	}
	
	public void setComment(String comment) {
		this.comment.set(comment);
	}
	
	public StringProperty commentProperty() {
		return comment;
	}
	
	public LocalDate getStartDate() {
		return startDate.get();
	}
	
	public void setStartDate(LocalDate startDate) {
		this.startDate.set(startDate);
	}
	
	public ObjectProperty<LocalDate> startDateProperty() {
		return startDate;
	}
	
	public LocalTime getStartTime() {
		return startTime.get();
	}
	
	public void setStartTime(LocalTime startTime) {
		this.startTime.set(startTime);
	}
	
	public ObjectProperty<LocalTime> startTimeProperty() {
		return startTime;
	}
	
	public LocalDate getDueDate() {
		return dueDate.get();
	}
	
	public void setDueDate(LocalDate dueDate) {
		this.dueDate.set(dueDate);
	}
	
	public ObjectProperty<LocalDate> dueDateProperty() {
		return dueDate;
	}
	
	public LocalTime getDueTime() {
		return dueTime.get();
	}
	
	public void setDueTime(LocalTime dueTime) {
		this.dueTime.set(dueTime);
	}
	
	public ObjectProperty<LocalTime> dueTimeProperty() {
		return dueTime;
	}
}
