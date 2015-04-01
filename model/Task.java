package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for a Task.
 * 
 * @author Kyle
 *
 */

public class Task implements Comparable<Object> {
	protected StringProperty taskName;
	protected StringProperty comment;

	protected ObjectProperty<LocalDate> startDate;
	protected ObjectProperty<LocalTime> startTime;

	protected ObjectProperty<LocalDate> endDate;
	protected ObjectProperty<LocalTime> endTime;
	
	protected boolean isRecurring = false;
	
	protected String idString;

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
	 *            name of the task.
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
	 *            Name of the task.
	 * @param taskDescription
	 *            Description of the task.
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

	/**
	 * Returns the name of the task.
	 * 
	 * @return name of the task.
	 */
	public String getTaskName() {
		return taskName.get();
	}

	/**
	 * Sets the name of the task.
	 * 
	 * @param taskName
	 *            new name of the task.
	 */
	public void setTaskName(String taskName) {
		this.taskName.set(taskName);
	}

	/**
	 * Returns the taskName in a StringProperty instance.
	 * 
	 * @return StringProperty of taskName.
	 */
	public StringProperty taskNameProperty() {
		return taskName;
	}

	/**
	 * Returns the comment as a String object.
	 * 
	 * @return comment as a String object.
	 */
	public String getComment() {
		return comment.get();
	}

	/**
	 * Sets the comment.
	 * 
	 * @param comment
	 *            new comment.
	 */
	public void setComment(String comment) {
		this.comment.set(comment);
	}

	/**
	 * Returns the comment as a StringProperty instance.
	 * 
	 * @return comment as a StringProperty instance.
	 */
	public StringProperty commentProperty() {
		return comment;
	}

	/**
	 * Returns the starting date of the task in a LocalDate instance.
	 * 
	 * @return starting date in a LocalData instance.
	 */
	public LocalDate getStartDate() {
		return startDate.get();
	}

	/**
	 * Sets the starting date of the task.
	 * 
	 * @param startDate
	 *            new starting date of the task in a LocalDate instance.
	 */
	public void setStartDate(LocalDate startDate) {
		this.startDate.set(startDate);
	}

	/**
	 * Returns the starting date of the task in an ObjectProperty instance.
	 * 
	 * @return starting date of the task in an ObjectProperty instance.
	 */
	public ObjectProperty<LocalDate> startDateProperty() {
		return startDate;
	}

	/**
	 * Returns the starting time of the task in a LocalTime instance.
	 * 
	 * @return starting time of the task in a LocalTime instance.
	 */
	public LocalTime getStartTime() {
		return startTime.get();
	}

	/**
	 * Sets the starting time of the task.
	 * 
	 * @param startTime
	 *            new starting time of the task in a LocalTime instance.
	 */
	public void setStartTime(LocalTime startTime) {
		this.startTime.set(startTime);
	}

	/**
	 * Returns the starting time of the task in an ObjectProperty instance.
	 * 
	 * @return starting time of the task in an ObjectProperty instance.
	 */
	public ObjectProperty<LocalTime> startTimeProperty() {
		return startTime;
	}

	/**
	 * Returns the ending date of the task in a LocalDate instance.
	 * 
	 * @return ending date of the task in a LocalDate instance.
	 */
	public LocalDate getEndDate() {
		return endDate.get();
	}

	/**
	 * Sets the ending date of the task.
	 * 
	 * @param endDate
	 *            new ending date of the task in a LocalDate instance.
	 */
	public void setEndDate(LocalDate endDate) {
		this.endDate.set(endDate);
	}

	/**
	 * Returns the ending date of the task in an ObjectProperty instance.
	 * 
	 * @return ending date of the task in an ObjectProperty instance.
	 */
	public ObjectProperty<LocalDate> endDateProperty() {
		return endDate;
	}

	/**
	 * Returns the ending time of the task in a LocalTime instance.
	 * 
	 * @return ending time of the task in a LocalTime instance.
	 */
	public LocalTime getEndTime() {
		return endTime.get();
	}

	/**
	 * Sets the ending time of the task.
	 * 
	 * @param endTime
	 *            new ending time of the task.
	 */
	public void setEndTime(LocalTime endTime) {
		this.endTime.set(endTime);
	}

	/**
	 * Returns the ending time of the task as an ObjectProperty instance.
	 * 
	 * @return ending time of the task as an ObjectProperty instance.
	 */
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
	 *            Task object.
	 * @return EARLIER, SAME or LATER depending on chronology.
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
	 * Returns the type of the task.
	 *
	 * @return Type of task.
	 */
	public String getType() {
		if (this.endTime.get() != null) {
			return EVENT;
		} else if (this.startTime.get() != null) {
			return DATELINE;
		} else {
			return FLOAT;
		}
	}

	/**
	 * Returns the LocalDateTime from LocalDate and LocalTime of task.
	 *
	 * @return task as a LocalDateTime instance.
	 */
	private LocalDateTime getLocalDateTime() {
		return LocalDateTime.of(startDate.get(), startTime.get());
	}
	
	/**
	 * Sets if task is a recurring task
	 * 
	 * @param isRecurring
	 *            whether task is recurring
	 */
	public void setIsRecurring(boolean isRecurring){
		this.isRecurring = isRecurring;
	}
	
	/**
	 * Sets if task is a recurring task
	 * 
	 * @return isRecurring
	 *            whether task is recurring
	 */
	public boolean taskIsRecurring(){
		return this.isRecurring;
	}
	
	public String getId() {
		return idString;
	}
	
	public void setId(String idString) {
		this.idString = idString;
	}
}
