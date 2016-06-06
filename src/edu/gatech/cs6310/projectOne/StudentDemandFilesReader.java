package edu.gatech.cs6310.projectOne;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StudentDemandFilesReader {

	List<StudentDemand> studentDemands = new ArrayList<StudentDemand>();
	GeneralConstraints generalConstraints = new GeneralConstraints();

	public List<StudentDemand> getStudentDemands(String csvFile) {

		String csvDelimiter = ",";
		BufferedReader br = null;
		String line = "";

		try {
			br = new BufferedReader(new FileReader(csvFile));
			br.readLine();

			while ((line = br.readLine()) != null) {
				String[] strings = line.split(csvDelimiter);
				StudentDemand sd = new StudentDemand();
				sd.setStudentID(Integer.parseInt(strings[0]));
				sd.setCourseID(Integer.parseInt(strings[1]));
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
		return studentDemands;
	}
}
