package edu.gatech.cs6310.projectOne;

import java.io.IOException;
import java.util.List;

import gurobi.*;

public class GurobiScheduler {
	
    double result;
    private List<StudentDemand> studentDemands;
    GurobiConstraints grbc = new GurobiConstraints();
    TestFilesReader tfr = new TestFilesReader();
    GeneralConstraints gc = new GeneralConstraints();
	
    private int numCourses = gc.getNumCourses();	
	private int numSemesters = gc.getNumSemesters();
	private int numStudents = gc.getNumStudents();

    public double calculateSchedule(String csvFile) {
        GRBEnv env;
        GRBVar csvar;
        GRBVar[][][] yijk;
        
        try {
            env = new GRBEnv("grb.log");
            env.set(GRB.IntParam.LogToConsole, 0);

            GRBModel model = new GRBModel(env);

            studentDemands = tfr.getStudentDemands(csvFile);

            yijk = createYijk(model);
            csvar = addCourseSizeLimit(model);

            grbc.generateMaxCoursePerSemesterConstraint(model);
            grbc.generateCourseTakenTimeConstraint(model);
            grbc.generateClassSizeConstraints(csvar, model);
            grbc.generateCoursePrerequisiteConstraint(model);
            grbc.generateStudentDemandConstraint(studentDemands, model);
            
            setObjective(model, csvar);

            model.optimize();
            result = model.get(GRB.DoubleAttr.ObjVal);
            return result;

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

}
