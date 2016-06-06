package edu.gatech.cs6310.projectOne;

public class GeneralConstraints {
	
	private int numCourses = 18;	
	private int numSemesters = 12;
	private int numMaxCourse = 2;
	private int numStudents;
	
	public int getNumCourses() {
		return numCourses;
	}

	public int getNumSemesters() {
		return numSemesters;
	}

	public int getNumStudents() {
		return numStudents;
	}

	public void setNumStudents(int numStudents) {
		this.numStudents = numStudents;
	}

	public int getNumMaxCourse() {
		return numMaxCourse;
	}
}
