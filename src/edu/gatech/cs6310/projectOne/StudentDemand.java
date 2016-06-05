package edu.gatech.cs6310.projectOne;

public class StudentDemand {
	
	private int studentID;
	private int courseID;
	
	public StudentDemand(int studentID, int courseID){
		this.courseID = courseID;
		this.studentID = studentID;
	}
	
	public int getStudentID() {
		return studentID;
	}
	

	public int getCourseID() {
		return courseID;
	}
}
