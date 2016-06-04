package edu.gatech.cs6310.projectOne;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GurobiScheduler {
	
    /**
     * the list of student demands
     */
    private List<StudentDemand> studentDemands;

    @Override
    public void calculateSchedule(String dataFolder) {
        GRBEnv env;

        try {
            env = new GRBEnv("grb.log");
            env.set(GRB.IntParam.LogToConsole, 0);

            GRBModel model = new GRBModel(env);

            studentDemands = parseStudentDemandFile(dataFolder);

            studCourseSemBooleanVars = createStudCourseSemVariables(model);
            GRBVar courseSemClassSizeVar = createCourseSizeLimitVariable(model);

            addConstraints(model, courseSemClassSizeVar);
            setObjective(model, courseSemClassSizeVar);

            model.optimize();
            objectiveValue = model.get(GRB.DoubleAttr.ObjVal);

        } catch (IOException ioE) {
            ioE.printStackTrace();
        } catch (GRBException grbE) {
            grbE.printStackTrace();
        }

    }

    public double getObjectiveValue() {
        return objectiveValue;
    }

   
    private void addConstraints(GRBModel model, GRBVar courseSemClassSizeVar)
                    throws GRBException {
        addFullLoadConstraintsToModel(model);
        addStudentOnlyTakeCourseOnceToModel(model);
        addCourseCapacityConstraintsToModel(courseSemClassSizeVar, model);
        addCoursePrerequisiteConstraintsToModel(model);
        addStudentDemandConstraintsToModel(studentDemands, model);
    }

}
