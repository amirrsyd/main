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

public class Task implements Comparable {
	private final StringProperty taskName;
	private final StringProperty comment;
	
	private final ObjectProperty<LocalDate> startDate;
	private final ObjectProperty<LocalTime> startTime;
	
	private final ObjectProperty<LocalDate> endDate;
	private final ObjectProperty<LocalTime> endTime;

	private final int EARLIER = -1;
	private final int SAME = 0;
	private final int LATER = 1;

	private enum TaskType {
		FLOAT, EVENT, DATELINE
	}
	
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
		this.endDate = new SimpleObjectProperty<LocalDate>(LocalDate.of(1992, 9, 12));
		this.endTime = new SimpleObjectProperty<LocalTime>(LocalTime.of(12,0));
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
	
	public LocalDate getendDate() {
		return endDate.get();
	}
	
	public void setendDate(LocalDate endDate) {
		this.endDate.set(endDate);
	}
	
	public ObjectProperty<LocalDate> endDateProperty() {
		return endDate;
	}
	
	public LocalTime getendTime() {
		return endTime.get();
	}
	
	public void setendTime(LocalTime endTime) {
		this.endTime.set(endTime);
	}
	
	public ObjectProperty<LocalTime> endTimeProperty() {
		return endTime;
	}

	/**
	 * Overrides compareTo method and returns the chronology
	 * of the compared dates with -1 for being earlier, 0 for
	 * being the same and 1 for being later.
	 * It currently does not take into account of period overlaps.
	 * Thus endTime is not be accounted for and the method is 
	 * seemingly a lot shorter.
	 * 
	 * @param obj    Task.
	 * @return       Chronology CONSTANT.
	 */
	public int compareTo(Object obj) {
		Task task = (Task) obj;
		if (this.getType() == FLOAT && task.getType == FLOAT) {
			return SAME;
		}
		else if (this.getType() == FLOAT) {
			return LATER;
		}
		else if (task.getType() == FLOAT) {
			return EARLIER;
		}
		else if (this.getLocalDateTime().isBefore(task.getLocalDateTime)) {
			return EARLIER;
		}
		else if (this.getLocalDateTime().isEqual(task.getLocalDateTime())) {
			return SAME;
		}
		else {
			return LATER;
		}
	}

	/**
	 * Gets the type of the task
	 *
	 * @return    Type of task.
	 */
	private TaskType getType() {
		if (this.startTime != null) {
			return TaskType.EVENT;
		}
		else if (this.endTime != null) {
			return TaskType.DATELINE;
		}
		else {
			return TaskType.FLOAT;
		}
	}

	/**
	 * Gets the LocalDateTime from LocalDate and LocalTime of task
	 *
	 * @return    LocalDateTime of task
	 */
	private LocalDateTime getLocalDateTime() {
		return LocalDateTime.of(startDate, startTime);
	}
}
