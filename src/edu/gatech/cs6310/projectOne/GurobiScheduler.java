package edu.gatech.cs6310.projectOne;

import java.util.List;

import gurobi.*;

public class GurobiScheduler extends Scheduler {

	protected double result;
	protected List<StudentDemand> studentDemands;
	
	protected CourseAvailability courseAvail = new CourseAvailability();
	protected CoursePrerequisites coursePrereq = new CoursePrerequisites();
	protected TestFilesReader tfr = new TestFilesReader();
	protected GeneralConstraints gc = new GeneralConstraints();

	protected int numCourses = gc.getNumCourses();
	protected int numSemesters = gc.getNumSemesters();
	protected int numStudents = gc.getNumStudents();
	protected int numMaxCourse = gc.getNumMaxCourse();

	protected GRBEnv env;
	protected GRBModel model;
	protected GRBVar x;
	protected GRBVar[][][] yijk;

	@Override
	protected void generateConstraints() throws GRBException {
		generateClassSizeConstraints();
		generateMaxCourseConstraint();
		generatePrerequisiteConstraint();
		generateStudentDemandConstraint();
	}
	
	public double calculateSchedule(String csvFile) {
		try {
			env = new GRBEnv("p1grb.log");
			env.set(GRB.IntParam.LogToConsole, 0);
			model = new GRBModel(env);

			studentDemands = tfr.getStudentDemands(csvFile);

			initializeYijk();
			initializeX();
			setObjective();
			generateConstraints();
			
			model.optimize();
			result = model.get(GRB.DoubleAttr.ObjVal);

		} catch (GRBException grbE) {
			grbE.printStackTrace();
		}
		return result;
	}


	private void initializeYijk() throws GRBException {
		yijk = new GRBVar[numStudents ][numCourses][numSemesters];

		for (int student = 0; student < numStudents; student++) {
			for (int course = 0; course < numCourses; course++) {
				for (int semester = 0; semester < numSemesters; semester++) {
					yijk[student][course][semester] = model.addVar(0, 1, 0.0, GRB.BINARY, "yijk");
				}
			}
		}
		model.update();
	}
	

	protected void initializeX() throws GRBException {
		x = model.addVar(0, numStudents, 0.0, GRB.INTEGER, "classSize");
		model.update();
	}

	private void setObjective() throws GRBException {
		GRBLinExpr objective = new GRBLinExpr();
		objective.addTerm(1, x);
		model.setObjective(objective, GRB.MINIMIZE);
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
