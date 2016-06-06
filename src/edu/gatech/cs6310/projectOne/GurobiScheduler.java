package edu.gatech.cs6310.projectOne;

import gurobi.*;

public class GurobiScheduler extends Scheduler {

	private CourseAvailability courseAvail = new CourseAvailability();
	private CoursePrerequisites coursePrereq = new CoursePrerequisites();

	@Override
	protected void generateConstraints() throws GRBException {
		generateClassSizeConstraints();
		generateMaxCourseConstraint();
		generatePrerequisiteConstraint();
		generateStudentDemandConstraint();
	}

	protected void generateClassSizeConstraints() throws GRBException {
		for (int course = 0; course < numCourses; course++) {
			for (int semester = 0; semester < numSemesters; semester++) {
				
				GRBLinExpr classSizeGRB = new GRBLinExpr();
				if (courseAvail.courseAvailability[course][semester] == true) {		
					for (int student = 0; student < numStudents; student++) {
						classSizeGRB.addTerm(1, yijk[student][course][semester]);
					}
					model.addConstr(classSizeGRB, GRB.LESS_EQUAL, x, "Courses offered");
				} else {				
					for (int student = 0; student < numStudents; student++) {
						classSizeGRB.addTerm(1, yijk[student][course][semester]);
					}
					model.addConstr(classSizeGRB, GRB.LESS_EQUAL, 0, "Courses not offered");
				}
			}
		}
	}

	protected void generatePrerequisiteConstraint() throws GRBException {
		for (int cp = 0; cp < coursePrereq.coursePrerequisites.length; cp++) {
			int prereq = coursePrereq.coursePrerequisites[cp][0];
			int postreq = coursePrereq.coursePrerequisites[cp][1];

			for (int student = 0; student < numStudents; student++) {
				for (int semester = 0; semester < numSemesters; semester++) {

					GRBLinExpr coursePrerequisiteGRB = new GRBLinExpr();
					for (int k = 0; k < semester; k++) {
						coursePrerequisiteGRB.addTerm(1, yijk[student][prereq][k]);
					}
					model.addConstr(coursePrerequisiteGRB, 
							GRB.GREATER_EQUAL,
							yijk[student][postreq][semester], 
							"Prerequisite constraint");
				}
			}
		}
	}

	protected void generateStudentDemandConstraint() throws GRBException {
		for (int student = 0; student < numStudents; student++) {
			for (int course = 0; course < numCourses; course++) {
				
				for (StudentDemand sd : studentDemands) {
					GRBLinExpr studentDemandGRB = new GRBLinExpr();
					if ((student + 1) == sd.getStudentID() && (course + 1) == sd.getCourseID()) {
						for (int semester = 0; semester < numSemesters; semester++) {
							studentDemandGRB.addTerm(1, yijk[student][course][semester]);
						}
						model.addConstr(studentDemandGRB, GRB.EQUAL, 1, "Student demand constraint");
					} else {
						for (int semester = 0; semester < numSemesters; semester++) {
							studentDemandGRB.addTerm(1, yijk[student][course][semester]);
						}
						model.addConstr(studentDemandGRB, GRB.EQUAL, 0, "Student demand constraint");
					}
				}
			}
		}
	}
	
	protected void generateMaxCourseConstraint() throws GRBException {
		for (int student = 0; student < numStudents; student++) {
			for (int semester = 0; semester < numSemesters; semester++) {

				GRBLinExpr maxCoursePerSemesterGRB = new GRBLinExpr();
				for (int course = 0; course < numCourses; course++) {
					maxCoursePerSemesterGRB.addTerm(1, yijk[student][course][semester]);
				}
				model.addConstr(maxCoursePerSemesterGRB, 
						        GRB.LESS_EQUAL,
						        2, 
						        "Max course per sememster constraints");
			}
		}
	}

}
