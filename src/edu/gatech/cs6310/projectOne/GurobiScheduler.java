package edu.gatech.cs6310.projectOne;

import java.util.List;

import gurobi.*;

public class GurobiScheduler extends Scheduler {

	protected List<StudentDemand> studentDemands;
	
	protected CourseAvailability courseAvail = new CourseAvailability();
	protected CoursePrerequisites coursePrereq = new CoursePrerequisites();
	protected StudentDemandFilesReader tfr = new StudentDemandFilesReader();
	protected GeneralConstraints gc = new GeneralConstraints();

	protected int numCourses;
	protected int numSemesters;
	protected int numStudents;
	protected int numMaxCourse;

	protected GRBEnv env;
	protected GRBModel model;
	protected GRBVar x;
	protected GRBVar[][][] yijk;
	
	protected double result;
	
	//main method for final calculation
	public double calculateSchedule(String csvFile) {
		
		numCourses   = gc.getNumCourses();
		numSemesters = gc.getNumSemesters();
		numMaxCourse = gc.getNumMaxCourse();
		
		studentDemands = tfr.getStudentDemands(csvFile);
		numStudents    = studentDemands.size();
		
		try {
			env = new GRBEnv("p1grb.log");
			env.set(GRB.IntParam.LogToConsole, 0);
			model = new GRBModel(env);

			initializeYijk();
			initializeX();
			generateConstraints();
			setObjective();
			
			model.optimize();
			result = model.get(GRB.DoubleAttr.ObjVal);
		} catch (GRBException grbE) {
			grbE.printStackTrace();
		}
		return result;
	}


	@Override
	protected void generateConstraints() throws GRBException {
		generateClassSizeConstraints();
		generateMaxCourseConstraint();
		generatePrerequisiteConstraint();
		generateStudentDemandConstraint();
	}
	
	@Override
	protected void initializeYijk() throws GRBException {
		yijk = new GRBVar[numStudents][numCourses][numSemesters];
		for (int student = 0; student < numStudents; student++) {
			for (int course = 0; course < numCourses; course++) {
				for (int semester = 0; semester < numSemesters; semester++) {
					yijk[student][course][semester] = model.addVar(0, 1, 0.0, GRB.BINARY, "yijk");
				}
			}
		}
		model.update();
	}
	
	@Override
	protected void initializeX() throws GRBException {
		x = model.addVar(0, numStudents, 0.0, GRB.INTEGER, "classSize");
		model.update();
	}

	@Override
	protected void setObjective() throws GRBException {
		GRBLinExpr objective = new GRBLinExpr();
		objective.addTerm(1, x);
		model.setObjective(objective, GRB.MINIMIZE);
	}

	//////////////////////////////////////////////////////////
	//generate constraints                                  //
	//////////////////////////////////////////////////////////
	
	protected void generateClassSizeConstraints() throws GRBException {
		for (int course = 0; course < numCourses; course++) {
			for (int semester = 0; semester < numSemesters; semester++) {			
				GRBLinExpr classSizeGRB = new GRBLinExpr();
				if (courseAvail.courseAvailability[course][semester] == true) {		
					for (int student = 0; student < numStudents; student++) {
						classSizeGRB.addTerm(1, yijk[student][course][semester]);
					}
					model.addConstr(classSizeGRB, GRB.LESS_EQUAL, x, 
							        "CoursesAvailabilityConstraint_offered");
				} else {				
					for (int student = 0; student < numStudents; student++) {
						classSizeGRB.addTerm(1, yijk[student][course][semester]);
					}
					model.addConstr(classSizeGRB, GRB.LESS_EQUAL, 0, 
							        "CoursesAvailabilityConstraint_not_offered");
				}
			}
		}
	}

	
	protected void generatePrerequisiteConstraint() throws GRBException {
		for (int cp = 0; cp < coursePrereq.coursePrerequisites.length; cp++) {
			int prereq = coursePrereq.coursePrerequisites[cp][0]-1;
			int postreq = coursePrereq.coursePrerequisites[cp][1]-1;

			for (int student = 0; student < numStudents; student++) {
				for (int semester = 0; semester < numSemesters; semester++) {
					GRBLinExpr coursePrerequisiteGRB = new GRBLinExpr();
					for (int k = 0; k < semester; k++) {
						coursePrerequisiteGRB.addTerm(1, yijk[student][prereq][k]);
					}
					model.addConstr(coursePrerequisiteGRB, GRB.GREATER_EQUAL,
							        yijk[student][postreq][semester], 
							        "PrerequisiteConstraint");
				}
			}
		}
	}

	protected void generateStudentDemandConstraint() throws GRBException {
		for (StudentDemand sd : studentDemands) {
			int student = sd.getStudentID() - 1;
			int course = sd.getCourseID() - 1;
			
			GRBLinExpr studentDemandGRB = new GRBLinExpr();
			for (int semester = 0; semester < numSemesters; semester++) {
				studentDemandGRB.addTerm(1, yijk[student][course][semester]);
			}
			model.addConstr(studentDemandGRB, GRB.EQUAL, 1,
					       "StudentDemandConstraint");
		}
	}
	
	protected void generateMaxCourseConstraint() throws GRBException {
		for (int student = 0; student < numStudents; student++) {
			for (int semester = 0; semester < numSemesters; semester++) {
				GRBLinExpr maxCoursePerSemesterGRB = new GRBLinExpr();
				for (int course = 0; course < numCourses; course++) {
					maxCoursePerSemesterGRB.addTerm(1, yijk[student][course][semester]);
				}
				model.addConstr(maxCoursePerSemesterGRB, GRB.LESS_EQUAL, 2, 
						        "MaxCoursePerSememsterConstraint");
			}
		}
	}

}
