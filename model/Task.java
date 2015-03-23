package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for a Task
 * 
 * @author Kyle
 *
 */

public class Task implements Comparable<Object> {
	private final StringProperty taskName;
	private final StringProperty comment;

	private final ObjectProperty<LocalDate> startDate;
	private final ObjectProperty<LocalTime> startTime;

	private final ObjectProperty<LocalDate> endDate;
	private final ObjectProperty<LocalTime> endTime;

	private final int EARLIER = -1;
	private final int SAME = 0;
	private final int LATER = 1;

	private final String EVENT = "event";
	private final String DATELINE = "dateline";
	private final String FLOAT = "float";

	/**
	 * Default constructor.
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
		this.startDate = new SimpleObjectProperty<LocalDate>(LocalDate.of(1992,
				9, 12));
		this.startTime = new SimpleObjectProperty<LocalTime>(
				LocalTime.of(12, 0));
		this.endDate = new SimpleObjectProperty<LocalDate>(LocalDate.of(1992,
				9, 12));
		this.endTime = new SimpleObjectProperty<LocalTime>(LocalTime.of(12, 0));
	}

	/**
	 * Constructor for setting all values of a task and instantiating it.
	 *
	 * @param taskName
	 *            Name of the task object.
	 * @param taskDescription
	 *            Descriptiont of the task object.
	 * @param startDate
	 *            Starting date of the task.
	 * @param startTime
	 *            Starting time of the task.
	 * @param endDate
	 *            Ending date of the task.
	 * @param endTime
	 *            Ending time of the task.
	 */
	public Task(String taskName, String taskDescription, LocalDate startDate,
			LocalTime startTime, LocalDate endDate, LocalTime endTime) {
		this.taskName = new SimpleStringProperty(taskName);
		this.comment = new SimpleStringProperty(taskDescription);
		this.startDate = new SimpleObjectProperty<LocalDate>(startDate);
		this.startTime = new SimpleObjectProperty<LocalTime>(startTime);
		this.endDate = new SimpleObjectProperty<LocalDate>(endDate);
		this.endTime = new SimpleObjectProperty<LocalTime>(endTime);
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

	public LocalDate getEndDate() {
		return endDate.get();
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate.set(endDate);
	}

	public ObjectProperty<LocalDate> endDateProperty() {
		return endDate;
	}

	public LocalTime getEndTime() {
		return endTime.get();
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime.set(endTime);
	}

	public ObjectProperty<LocalTime> endTimeProperty() {
		return endTime;
	}

	/**
	 * Overrides compareTo method and returns the chronology of the compared
	 * dates with -1 for being earlier, 0 for being the same and 1 for being
	 * later. It currently does not take into account of period overlaps. Thus
	 * endTime is not be accounted for and the method is seemingly a lot
	 * shorter.
	 * 
	 * @param obj
	 *            Task.
	 * @return Chronology CONSTANT.
	 */
	public int compareTo(Object obj) {
		Task task = (Task) obj;
		if (this.getType() == FLOAT && task.getType() == FLOAT) {
			return SAME;
		} else if (this.getType() == FLOAT) {
			return LATER;
		} else if (task.getType() == FLOAT) {
			return EARLIER;
		} else if (this.getLocalDateTime().isBefore(task.getLocalDateTime())) {
			return EARLIER;
		} else if (this.getLocalDateTime().isEqual(task.getLocalDateTime())) {
			return SAME;
		} else {
			return LATER;
		}
	}

	/**
	 * Gets the type of the task
	 *
	 * @return Type of task.
	 */
	private String getType() {
		if (this.endTime.get() != null) {
			return EVENT;
		} else if (this.startTime.get() != null) {
			return DATELINE;
		} else {
			return FLOAT;
		}
	}

	/**
	 * Gets the LocalDateTime from LocalDate and LocalTime of task
	 *
	 * @return LocalDateTime of task
	 */
	private LocalDateTime getLocalDateTime() {
		return LocalDateTime.of(startDate.get(), startTime.get());
	}
}
