package edu.gatech.cs6310.projectOne;

public class GeneralConstraints {
	
	private int numCourses = 18;	
	private int numSemesters = 12;
	private int numCoursePerSemester = 2;
	private int numStudents;
	
	public int getNumCourses() {
		return numCourses;
	}

	public int getNumSemesters() {
		return numSemesters;
	}

	public int getNumCoursePerSemester() {
		return numCoursePerSemester;
	}

	public int getNumStudents() {
		return numStudents;
	}

	public void setNumStudents(int numStudents) {
		this.numStudents = numStudents;
	}

}
