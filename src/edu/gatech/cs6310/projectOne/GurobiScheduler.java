package edu.gatech.cs6310.projectOne;

import java.io.IOException;
import java.util.List;

import gurobi.*;

public class GurobiScheduler {
	
    double resClassSize;
    private List<StudentDemand> studentDemands;
    GurobiConstraints grbc = new GurobiConstraints();
    TestFilesReader tfr = new TestFilesReader();
    GeneralConstraints gc = new GeneralConstraints();
	
    private int numCourses = gc.getNumCourses();	
	private int numSemesters = gc.getNumSemesters();
	private int numStudents;

    public double calculateSchedule(String csvFile) {
        GRBEnv env;
        GRBVar csVar;
        GRBVar[][][] yijk;
        
        try {
            env = new GRBEnv("grb.log");
            env.set(GRB.IntParam.LogToConsole, 0);

            GRBModel model = new GRBModel(env);

            studentDemands = tfr.getStudentDemands(csvFile);

            yijk = createYijk(model);
            csVar = addCourseSizeLimit(model);

            addConstraints(model, csVar);
            setObjective(model, csVar);

            model.optimize();
            resClassSize = model.get(GRB.DoubleAttr.ObjVal);
            return resClassSize;

        } catch (IOException ioE) {
            ioE.printStackTrace();
        } catch (GRBException grbE) {
            grbE.printStackTrace();
        }

    }

   
    private GRBVar addCourseSizeLimit(GRBModel model) throws GRBException {
            GRBVar ret = model.addVar(0, numStudents, 0.0, GRB.INTEGER, "classSize");
            model.update();
            return ret;
        }

    
    private GRBVar[][][] createYijk(GRBModel model) throws GRBException {
            GRBVar[][][] yStCoSe = new GRBVar[numStudents + 1][numCourses + 1][numSemesters + 1];
          
            for (int student = 1; student <= numStudents; student++) {
                for (int course = 1; course <= numCourses; course++) {
                    for (int semester = 1; semester <= numSemesters; semester++) {
                        yStCoSe[student][course][semester] = model.addVar(0, 1, 0.0, GRB.BINARY);
                    }
                }
            }
            model.update();
            return yStCoSe;
        }

   
    private void setObjective(GRBModel model, GRBVar objVar) throws GRBException {
            GRBLinExpr objective = new GRBLinExpr();
            objective.addTerm(1, objVar);

            model.setObjective(objective, GRB.MINIMIZE);
        }
    	
   
    private void addConstraints(GRBModel model, GRBVar csVar) throws GRBException {
    	grbc.generateMaxCoursePerSemesterConstraint(model);
        grbc.generateCourseTakenTimeConstraint(model);
        grbc.generateClassSizeConstraints(csVar, model);
        grbc.generateCoursePrerequisiteConstraint(model);
        grbc.generateStudentDemandConstraint(studentDemands, model);
    }

}
