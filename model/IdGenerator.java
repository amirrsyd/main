package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map.Entry;
import java.util.TreeMap;

public class IdGenerator {
	
	private static final Charset CHAR_SET = Charset.forName("US-ASCII");
	
	private int base36Id;
	private TreeMap<Integer, String> idMap;
	private Path filePath;
	
	public IdGenerator() {
		String dirPath = System.getProperty("user.dir");
		filePath = Paths.get(dirPath + "/Id.txt").toAbsolutePath();
		File idFile = filePath.toFile(); 
		if (idFile.exists() && idFile.length() > 0) {
			try {
				BufferedReader reader = Files.newBufferedReader(filePath);
				String line = reader.readLine();
				base36Id = Integer.parseInt(line.substring(1), 36);
				idMap = new TreeMap<Integer, String>();
				line = reader.readLine();
				while (line != null) {
					String[] splitLine = line.split(" ");
					int key = Integer.parseInt(splitLine[0].substring(1), 36);
					idMap.put(key, line.substring(splitLine[0].length() + 1));
					line = reader.readLine();
				}
				reader.close();
			} catch (IOException error) {
				System.err.println(error);
			}
		}
		else {
			base36Id = 0;
			idMap = new TreeMap<Integer, String>();
			save();
		}
	}

	public void addId(int id, String name) {
		idMap.put(id, name);
		save();
	}
	
	public void removeId(String idString) {
		idMap.remove(Integer.parseInt(idString.substring(1), 36));
		save();
	}
	
	public String generateId(String taskName) {
		boolean flag = false;
		while (isExistingId(base36Id)) {
			base36Id++; // If Id already exists, increment to generate new Id
			if (base36Id >= 2147483647 && flag == true) {
				System.out.println("maximum task limit exceeded");
				return null;
			}
			if (base36Id >= 2147483647) {
				base36Id = 0;
				flag = true;
			}
		}
		int id = base36Id;
		addId(id, taskName);
		base36Id++;
		save();
		return ("@").concat(Integer.toString(id, 36));
	}
	
	public boolean isExistingId(int id) {
		return idMap.containsKey(id);
	}
	
	public void save() {
		try (BufferedWriter writer = Files.newBufferedWriter(filePath, 
                CHAR_SET, StandardOpenOption.CREATE, 
                StandardOpenOption.WRITE, 
                StandardOpenOption.SYNC,
                StandardOpenOption.TRUNCATE_EXISTING)) {
			writer.write("@" + Integer.toString(base36Id, 36));
			writer.newLine();
			for (Entry<Integer, String> entry : idMap.entrySet()) {
			    writer.write("@" + Integer.toString(entry.getKey(), 36) + " " + entry.getValue());
			    writer.newLine();
			}
		} catch (IOException error) {
			System.err.println(error);
		}
	}
}
