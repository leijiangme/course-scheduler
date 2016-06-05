package edu.gatech.cs6310.projectOne;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestFilesReader {

    List<StudentDemand> getStudentDemands(String csvFile)
        throws FileNotFoundException, IOException {
			String csvSplitBy = ",";
			String line = "";
			BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
			
			List<StudentDemand> studentDemands = new ArrayList<StudentDemand>();	
			
			while ((line = bufferedReader.readLine()) != null) {
			    String[] strings = line.split(csvSplitBy);
				int[] numbers = new int[strings.length];

				for (int i = 0; i < numbers.length; i++) {
				    numbers[i] = Integer.parseInt(strings[i]);
				}

				studentDemands.add(new StudentDemand(numbers[0], numbers[1]));
			}
			bufferedReader.close();
			return studentDemands;
    }
}
