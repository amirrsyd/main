package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Model class for a recurring task. Recurrence states the number of times for
 * recurring and day states the day of the week to recur on if recurring weekly.
 * However if the task recurs monthly, then day is set to null.
 * 
 * @author Qiyuan.
 */
public class RecurringTask extends Task {
	private int recurrence;
	private DayOfWeek day;
	private int dayOfMonth;

	public enum Day {
		MON, TUE, WED, THU, FRI, SAT, SUN
	}

	/**
	 * Default constructor.
	 */
	public RecurringTask() {
	}

	/**
	 * Constructor for setting all values of a recurring task and instantiating
	 * it. Assumes recurrence on a date monthly.
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
	 * @param recurrence
	 *            Recurrence of the task.
	 * @param dayOfMonth
	 *            Recurrence of the task.
	 */
	public RecurringTask(String taskName, String taskDescription,
			LocalDate startDate, LocalTime startTime, LocalDate endDate,
			LocalTime endTime, int recurrence, int dayOfMonth) {
		this.taskName = new SimpleStringProperty(taskName);
		this.comment = new SimpleStringProperty(taskDescription);
		this.startDate = new SimpleObjectProperty<LocalDate>(startDate);
		this.startTime = new SimpleObjectProperty<LocalTime>(startTime);
		this.endDate = new SimpleObjectProperty<LocalDate>(endDate);
		this.endTime = new SimpleObjectProperty<LocalTime>(endTime);
		this.recurrence = recurrence;
		this.day = null;
		this.dayOfMonth = dayOfMonth;
		this.isRecurring = true;
	}

	/**
	 * Constructor for setting all values of a recurring task and instantiating
	 * it. Recurrence occur weekly on the specified day.
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
	 * @param recurrence
	 *            Recurrence of the task.
	 * @param day
	 *            day of the week to recur on.
	 */
	public RecurringTask(String taskName, String taskDescription,
			LocalDate startDate, LocalTime startTime, LocalDate endDate,
			LocalTime endTime, int recurrence, DayOfWeek day) {
		this.taskName = new SimpleStringProperty(taskName);
		this.comment = new SimpleStringProperty(taskDescription);
		this.startDate = new SimpleObjectProperty<LocalDate>(startDate);
		this.startTime = new SimpleObjectProperty<LocalTime>(startTime);
		this.endDate = new SimpleObjectProperty<LocalDate>(endDate);
		this.endTime = new SimpleObjectProperty<LocalTime>(endTime);
		this.recurrence = recurrence;
		this.day = day;
		this.dayOfMonth = 0;
		this.isRecurring = true;
	}

	/**
	 * Constructor for constructing a recurring task based on a normal task's
	 * values. Assumes recurrence on a date monthly.
	 * 
	 * @param task
	 *            The normal task.
	 * @param recurrence
	 *            Recurrence of the task.
	 */
	public RecurringTask(Task task, int recurrence, int dayOfMonth) {
		this.taskName = new SimpleStringProperty(task.getTaskName());
		this.comment = new SimpleStringProperty(task.getComment());
		this.startDate = new SimpleObjectProperty<LocalDate>(
				task.getStartDate());
		this.startTime = new SimpleObjectProperty<LocalTime>(
				task.getStartTime());
		this.endDate = new SimpleObjectProperty<LocalDate>(task.getEndDate());
		this.endTime = new SimpleObjectProperty<LocalTime>(task.getEndTime());
		this.recurrence = recurrence;
		this.day = null;
		this.dayOfMonth = dayOfMonth;
		this.isRecurring = true;
	}

	/**
	 * Constructor for constructing a recurring task based on a normal task's
	 * values. Recurrence occurs weekly on the specified day.
	 * 
	 * @param task
	 *            The normal task.
	 * @param recurrence
	 *            Recurrence of the task.
	 * @param day
	 *            day of the week to recur on.
	 */
	public RecurringTask(Task task, int recurrence, DayOfWeek day) {
		this.taskName = new SimpleStringProperty(task.getTaskName());
		this.comment = new SimpleStringProperty(task.getComment());
		this.startDate = new SimpleObjectProperty<LocalDate>(
				task.getStartDate());
		this.startTime = new SimpleObjectProperty<LocalTime>(
				task.getStartTime());
		this.endDate = new SimpleObjectProperty<LocalDate>(task.getEndDate());
		this.endTime = new SimpleObjectProperty<LocalTime>(task.getEndTime());
		this.recurrence = recurrence;
		this.day = day;
		this.dayOfMonth = 0;
		this.isRecurring = true;
	}

	/**
	 * Returns the recurrence of the task.
	 * 
	 * @return Recurrence of the task.
	 */
	public int getRecurrence() {
		return recurrence;
	}

	/**
	 * Sets the recurrence of the task.
	 * 
	 * @param recurrence
	 *            the new recurrence of the task.
	 */
	public void setRecurrence(int recurrence) {
		this.recurrence = recurrence;
	}

	/**
	 * Returns the day of the week to recur on.
	 * 
	 * @return day of the week to recur on.
	 */
	public DayOfWeek getRecurrenceDay() {
		return day;
	}

	/**
	 * Sets the day of the week to recur on.
	 * 
	 * @param day
	 *            day of the week to recur on.
	 */
	public void setRecurrenceDay(DayOfWeek day) {
		this.day = day;
	}

	/**
	 * Sets the day of the week to recur on.
	 * 
	 * @returns
	 *            day of the month to recur on.
	 */
	public int getDayOfMonth(){
		return dayOfMonth;
	}
	
	/**
	 * Sets the day of the week to recur on.
	 * 
	 * @param dayOfMonth
	 *            day of the month to recur on.
	 */
	public void setDayOfMonth(int dayOfMonth){
		this.dayOfMonth = dayOfMonth;
	}
	
}
