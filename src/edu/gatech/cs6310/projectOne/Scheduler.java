package edu.gatech.cs6310.projectOne;

import java.util.List;

import gurobi.*;

public abstract class Scheduler {

	protected double result;
	protected List<StudentDemand> studentDemands;

	TestFilesReader tfr = new TestFilesReader();
	GeneralConstraints gc = new GeneralConstraints();

	protected int numCourses = gc.getNumCourses();
	protected int numSemesters = gc.getNumSemesters();
	protected int numStudents = gc.getNumStudents();
	protected int numMaxCourse = gc.getNumMaxCourse();

	protected GRBEnv env;
	protected GRBModel model;
	protected GRBVar x;
	protected GRBVar[][][] yijk;

	protected abstract void generateConstraints() throws GRBException;

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
}
