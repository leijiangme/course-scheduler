package edu.gatech.cs6310.projectOne;

public class GeneralConstraints {
	
	private int numCourses = 18;	
	private int numSemesters = 12;
	private int maxCoursePerSemester = 2;
	private int timePerCourseTaken = 1;
	private int numStudents;
	
	public int getNumCourses() {
		return numCourses;
	}

	public int getNumSemesters() {
		return numSemesters;
	}

	public int getMaxCoursePerSemester() {
		return maxCoursePerSemester;
	}

	public int getTimePerCourseTaken() {
		return timePerCourseTaken;
	}

	public int getNumStudents() {
		return numStudents;
	}

	public void setNumStudents(int numStudents) {
		this.numStudents = numStudents;
	}

}
