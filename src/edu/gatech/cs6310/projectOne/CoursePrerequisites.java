package edu.gatech.cs6310.projectOne;

public class CoursePrerequisites {

	private int beforeCourseID;
	private int afterCourseID;
	
	public int getAfterCourseID() {
		return afterCourseID;
	}
	
	public void setAfterCourseID(int afterCourseID) {
		this.afterCourseID = afterCourseID;
	}
	
	public int getBeforeCourseID() {
		return beforeCourseID;
	}
	
	public void setBeforeCourseID(int beforeCourseID) {
		this.beforeCourseID = beforeCourseID;
	}
}
