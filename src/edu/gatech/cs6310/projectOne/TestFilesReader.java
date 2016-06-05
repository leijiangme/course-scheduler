package edu.gatech.cs6310.projectOne;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestFilesReader {

	List<StudentDemand> getStudentDemands(String csvFile) {
		
		List<StudentDemand> studentDemands = new ArrayList<StudentDemand>();
		GeneralConstraints generalConstraints = new GeneralConstraints();

		String csvDelimiter = ",";
		BufferedReader br = null;
		String line = "";
		int lineNumbers = 0;

		try {
			StudentDemand sd = new StudentDemand();
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();
			while ((line = br.readLine()) != null) {
				String[] strings = line.split(csvDelimiter);
				sd.setStudentID(Integer.parseInt(strings[0]));
				sd.setCourseID(Integer.parseInt(strings[1]));
				lineNumbers++;
				studentDemands.add(sd);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
        generalConstraints.setNumStudents(lineNumbers);
		return studentDemands;
	}
}
