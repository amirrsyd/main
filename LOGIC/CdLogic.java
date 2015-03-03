import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;
import java.io.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CdLogic{
    private static final String DATE_REGEX = "([1-9]|[012][0-9]|3[01])[-/]\\s*(0[1-9]|1[012])[-/]\\s*((19|20)?[0-9]{2})";
    private static final String TIME_REGEX = "([01]?[0-9]|2[0-3]):[0-5][0-9]";

    private static final String MESSAGE_INVALID_FORMAT = "invalid command "
            + "format :%1$s";
    
    private static Storage storage;
    private static TaskStorage taskStorage;
    private static TrashStorage trashStorge;
    private static HistoryStorage historyStorage;
    private static CompletedTaskStorage completedTaskStorage;
    
    private static ObservableList<Task> toDisplay;
    private static ObservableList<Task> tasks;
    
    enum COMMAND_TYPE {
        ADD, DELETE, LIST, EMPTY, SEARCH, COMPLETE, EDIT, INVALID, EXIT, CHANGEDIR, UNDO
    };
    
    enum TaskType{
    	FLOAT, EVENT, DATELINE
    }
    
    
    public static void main(String[] args){
    	storage = new Storage("");
    	taskStorage  = new TaskStorage("");
    	trashStorage = new TrashStorage("");
    	historyStorage = new HistoryStorage("");
    	completedTaskStorage = new CompletedTaskStorage("");
    	tasks = taskStorage.getTasks();
    	toDisplay = FXCollections.observableList(taskStorage.getTasks());
    }
    
    public ObservableList<Task> getTaskList(){
    	return tasks;
    }
    
    public ObservableList<Task> getDisplayList(){
    	return toDisplay;
    }


    public String executeCommand(String userCommand) throws IOException {
        if (userCommand.trim().equals(""))
            return String.format(MESSAGE_INVALID_FORMAT, userCommand);

        
        String commandTypeString = getFirstWord(userCommand);
        COMMAND_TYPE commandType = determineCommandType(commandTypeString);
        userCommand = removeFirstWord(userCommand);


        switch (commandType) {
        case ADD:
            return add(userCommand);
        case DELETE:
            return delete(userCommand);
        case LIST:
            return list(userCommand);
        case EMPTY:
            return empty();
        case SEARCH:
            return search(userCommand);
        case COMPLETE:
            return complete(userCommand);
        case EDIT:
            return edit(userCommand);
        case INVALID:
            return String.format(MESSAGE_INVALID_FORMAT, userCommand);
        case EXIT:
            System.exit(0);
        default:
            // throw an error if the command is not recognized
            throw new Error("Unrecognized command type");
        }
    }
   
    private String edit(String userCommand) {
		return null;
	}

	private String complete(String userCommand) {
		if(taskStorage.completeTask(userCommand, completedTaskStorage)){
			return "\"" + userCommand + "\"" + " completed successfully";
		}else{
			return "\"" + userCommand + "\"" + "couldn't be completed";
		}
		return null;
	}

	private String search(String userCommand) {
		// TODO Auto-generated method stub
		return null;
	}

	private String empty() {
		if(trashStorage.emptyTrash()){
			return "trash emptied successfully";
		} else {
			return "trash can't be emptied";
		}
	}

	private String list(String userCommand) {
		// TODO Auto-generated method stub 
		String[] listArguments = parseList(userCommand);
		toDisplay.clear();

		
		if(listArguments[0] == null){
			updateDisplay();
			return null;
		}else if(listArguments[1]==null){
			LocalDate date1 = toLocalDate(listArguments[0]);
			for(int i=0; i<tasks.size(); i++){
				Task currTask = tasks.get(i);
				if(currTask.getEndDate()!=null){
					if(currTask.getStartDate().equals(date1)
							||currTask.getEndDate().equals(date1)){
						toDisplay.add(currTask);
					}else if(currTask.getStartDate().isBefore(date1) 
							&& currTask.getEndDate().isAfter(date1)){
						toDisplay.add(currTask);
					}
				}else if(currTask.getStartDate()!=null){
					if(currTask.getStartDate().equals(date1)){
						toDisplay.add(currTask);
					}
				}
			}
		}else if(listArguments[2]==null){
			LocalDate date1 = toLocalDate(listArguments[0]);
			LocalTime time1 = toLocalTime(listArguments[1]);
			LocalDateTime dateTime1 = LocalDateTime.of(date1, time1);

			for(int i = 0; i<tasks.size(); i++){
				Task currTask = tasks.get(i);
				if(currTask.getEndDate()!=null){
					if(getStartLDT(currTask).equals(dateTime1)
							||getEndLDT(currTask).equals(dateTime1)){
						toDisplay.add(currTask);
					}else if(getStartLDT(currTask).isBefore(dateTime1) 
							&& getStartLDT(currTask).isAfter(dateTime1)){
						toDisplay.add(currTask);
					}
				}else if(currTask.getStartDate()!=null){
					if(getStartLDT(currTask).equals(dateTime1)){
						toDisplay.add(currTask);					}
				}
			}
		}else{
			LocalDate date1 = toLocalDate(listArguments[0]);
			LocalTime time1 = toLocalTime(listArguments[1]);
			LocalDate date2 = toLocalDate(listArguments[2]);
			LocalTime time2 = toLocalTime(listArguments[3]);
			LocalDateTime dateTime1 = LocalDateTime.of(date1, time1);
			LocalDateTime dateTime2 = LocalDateTime.of(date2, time2);
			
			for (int i=0; i<tasks.size(); i++){
				Task currTask = tasks.get(i);
				if(currTask.getStartDate()!=null){
					if((getStartLDT(currTask).compareTo(dateTime1)>=0)&&
							(getStartLDT(currTask).compareTo(dateTime2)<=0)){
						toDisplay.add(currTask);
					}else if(currTask.getEndDate()!=null){
						if((getEndLDT(currTask).compareTo(dateTime1)>=0)&&
							(getEndLDT(currTask).compareTo(dateTime2)<=0)){
							toDisplay.add(currTask);
						}
					}
				}
			}
		}
		
		return null;
	}

	/**
	 * 
	 * 
	 * @param userCommand
	 * @return
	 */
	private String delete(String userCommand) {
		// TODO Auto-generated method stub
		String taskName = removeFirstWord(userCommand);
		if(storage.deleteTask(taskName)){
			updateDisplay();
			return "\"" +taskName + "\"" + " deleted successfully";
		}
		
		return "delete not successful";
	}

	private void updateDisplay() {
		toDisplay = FXCollections.observableList(taskStorage.getTasks());
	}

	private String add(String userCommand) {
		// TODO Auto-generated method stub
		String[] addArguments = parseAdd(userCommand);
	
		
		LocalDate startDate = toLocalDate(addArguments[2]);
		LocalTime startTime = toLocalTime(addArguments[3]);
		LocalDate endDate = toLocalDate(addArguments[4]);
		LocalTime endTime = toLocalTime(addArguments[5]);
		
		if(storage.createTask(addArguments[0], addArguments[1], startDate, 
				startTime, endDate, endTime)){
			return "\"" + addArguments[0] + "\"" + " successfully added";
		}
		
		return null;
	}

	private COMMAND_TYPE determineCommandType(String commandTypeString) {
        if (commandTypeString == null) {
            throw new Error("command type string cannot be null!");
        }
        if (commandTypeString.equalsIgnoreCase("add")) {
            return COMMAND_TYPE.ADD;
        }
        if (commandTypeString.equalsIgnoreCase("delete")) {
            return COMMAND_TYPE.DELETE;
        }
        if (commandTypeString.equalsIgnoreCase("list")) {
            return COMMAND_TYPE.LIST;
        }
        if (commandTypeString.equalsIgnoreCase("empty")) {
            return COMMAND_TYPE.EMPTY;
        }
        if (commandTypeString.equalsIgnoreCase("search")) {
            return COMMAND_TYPE.SEARCH;
        }
        if (commandTypeString.equalsIgnoreCase("complete")) {
            return COMMAND_TYPE.COMPLETE;
        }
        if (commandTypeString.equalsIgnoreCase("edit")) {
            return COMMAND_TYPE.EDIT;
        }

        if (commandTypeString.equalsIgnoreCase("exit")) {
            return COMMAND_TYPE.EXIT;
        }

        return COMMAND_TYPE.INVALID;
    }

	// PARSE METHODS
	
    public String[] parseAdd(String command){
        String[] arguments = new String[5];
        arguments[0] = getTaskName(command); 
        arguments[1] = getDescription(command);
        String[] dates = extractDates(command);
        String[] times = extractTimes(command);

        arguments[2] = dates[0];
        arguments[4] = dates[1];
        arguments[3] = times[0];
        arguments[5] = times[1];

        return arguments;
    }
  
    public String[] parseList(String data){
        String[] arguments = new String[4];
        String[] dates = extractDates(data);
        String[] times = extractTimes(data);

        arguments[0] = dates[0];
        arguments[2] = dates[1];
        arguments[1] = times[0];
        arguments[3] = times[1];

        return arguments;    
    }

    public String[] parseSearch(String data){
        return data.trim().split("\\s+");
    }
    
    // STRING MANIPULATION METHODS
    
    public String getTaskName(String data){
        String task = data.split(DATE_REGEX, 2)[0];
        task.trim();
        
        return task;
    }
    
    public String getDescription(String data){
        String[] split = data.split(TIME_REGEX);
        String description = split[split.length -1].trim();

        return description;
    }

    public String[] extractDates(String data){
        Pattern datePattern = Pattern.compile(DATE_REGEX);
        Matcher dateMatcher = datePattern.matcher(data);
        String[] result = new String[2];
        int index = 0;
        while (dateMatcher.find()){
            result[index++] = dateMatcher.group();
        }  
        return result;
    }

    public String[] extractTimes(String data){
        Pattern timePattern = Pattern.compile(TIME_REGEX);
        Matcher timeMatcher = timePattern.matcher(data);
        String[] result = new String[2];
        int index = 0;
        while (timeMatcher.find()){
            result[index++] = timeMatcher.group();
        }  
        return result;
    }

    private String getFirstWord(String userCommand) {
        return userCommand.trim().split("\\s+")[0];
    }

    private String removeFirstWord(String userCommand) {
        return userCommand.replace(getFirstWord(userCommand), "").trim();
    }

    private LocalDate toLocalDate(String dateString){
    	DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    	LocalDate date = LocalDate.parse(dateString, dateFormatter);
    	
    	return date;
    }
    
    private LocalTime toLocalTime(String timeString){
    	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    	LocalTime time = LocalTime.parse(timeString, timeFormatter);
    	
    	return time;
    }
    
	public LocalDateTime getStartLDT(Task task) {
		return LocalDateTime.of(task.getStartDate(), task.getStartTime());
	}
	
	public LocalDateTime getEndLDT(Task task){
		return LocalDateTime.of(task.getEndDate(), task.getEndTime());
	}
}