package edu.gatech.cs6310.projectOne;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gurobi.*;

public class GurobiConstraints {
	
    private int numStudents;
    private Double objectiveValue = null;

    private final GeneralConstraints generalConstraints = new GeneralConstraints();
    private GRBVar[][][] yijk;  //i: student; j = course; k = semester
    

    private void addCourseCapacityConstraintsToModel(GRBVar classSizeVar, GRBModel model)
    		throws GRBException {

         for (int course = 1; course <= generalConstraints.getNumCourses(); ++course) {
            for (int semester = 1; semester <= generalConstraints.getNumSemesters(); ++semester) {
                GRBLinExpr classSize = new GRBLinExpr();
                for (int student = 1; student <= numStudents; ++student) {
                    classSize.addTerm(1, yijk[student][course][semester]);
                }

                if (generalConstraints.isCourseOffered(course, semester)) {
                    String name = String.format("ClassCapacity_Co%d_Se%d",
                                    course, semester);
                    model.addConstr(classSize, GRB.LESS_EQUAL, classSizeVar,
                                    name);
                } else {
                    String name = String.format(
                                    "ClassCapacity_CourseNotOffered_Co%d_Se%d",
                                    course, semester);
                    model.addConstr(classSize, GRB.LESS_EQUAL, 0, name);
                }

            }
        }
    }
    

    private void addCoursePrerequisiteConstraintsToModel(GRBModel model)
                    throws GRBException {
        for (CoursePrerequisites p : generalConstraints.getPrerequisites()) {
            addCoursePrerequisiteConstraintToModel(p.getPrereq(),
                            p.getPostreq(), model);
        }
    }

  
    private void addCoursePrerequisiteConstraintToModel(int prereq,
                    int postreq, GRBModel model) throws GRBException {

        for (int student = 1; student <= numStudents; ++student) {
            for (int semester = 1; semester <= generalConstraints
                            .getNumSemesters(); ++semester) {
                GRBLinExpr coursePrereqLHS = new GRBLinExpr();
                for (int k = 1; k < semester; ++k) {
                    coursePrereqLHS.addTerm(1, yijk[student][prereq][k]);
                }
                String name = String.format("Prereq_St%d_Co%d_%d", student,
                                prereq, postreq);
                model.addConstr(coursePrereqLHS,
                                GRB.GREATER_EQUAL,
                                yijk[student][postreq][semester],
                                name);
            }
        }

    }

    private void addFullLoadConstraintsToModel(GRBModel model)
                    throws GRBException {
        for (int student = 1; student <= numStudents; ++student) {
            for (int semester = 1; semester <= generalConstraints
                            .getNumSemesters(); ++semester) {
                GRBLinExpr courseLoad = new GRBLinExpr();
                for (int course = 1; course <= generalConstraints
                                .getNumCourses(); ++course) {
                    courseLoad.addTerm(
                                    1,
                                    yijk[student][course][semester]);
                }

                String name = String.format("FullLoad_St%d_Se%d", student,
                                semester);

                model.addConstr(courseLoad, GRB.LESS_EQUAL,
                                generalConstraints.getFullLoad(), name);
            }
        }
    }

    
    private void addStudentDemandConstraintsToModel(
                    List<StudentDemand> studentDemands, GRBModel model)
                    throws GRBException {
        // Students must take each course that they're requesting
        for (StudentDemand sd : studentDemands) {
            int student = sd.getStudentID();
            int course = sd.getCourseID();

            GRBLinExpr studentMustTakeCourse = new GRBLinExpr();
            for (int semester = 1; semester <= generalConstraints
                            .getNumSemesters(); ++semester) {
                studentMustTakeCourse
                                .addTerm(1,
                                                yijk[student][course][semester]);
            }

            String name = String.format("St%d_must_take_Co%d", student, course);
            model.addConstr(studentMustTakeCourse, GRB.EQUAL, 1, name);
        }
    }

    private void addStudentOnlyTakeCourseOnceToModel(GRBModel model)
                    throws GRBException {

        for (int student = 1; student <= numStudents; ++student) {
            for (int course = 1; course <= generalConstraints.getNumCourses(); ++course) {
                GRBLinExpr le = new GRBLinExpr();

                for (int semester = 1; semester <= generalConstraints
                                .getNumSemesters(); ++semester) {
                    le.addTerm(1,
                                    yijk[student][course][semester]);

                }
                String name = String.format("St%d_take_Co%d_only_once",
                                student, course);

                model.addConstr(le, GRB.LESS_EQUAL, 1, name);
            }
        }
    }

    private GRBVar createCourseSizeLimitVariable(GRBModel model)
                    throws GRBException {

        GRBVar ret = model
                        .addVar(0, numStudents, 0.0, GRB.INTEGER, "ClassSize");

        model.update();
        return ret;
    }

    private GRBVar[][][] createStudCourseSemVariables(GRBModel model)
                    throws GRBException {
        GRBVar[][][] yStudCourseSem = new GRBVar[numStudents + 1][generalConstraints
                        .getNumCourses() + 1][generalConstraints
                        .getNumSemesters() + 1];

        String format = "St%0"
                        + String.valueOf(numStudents).length()
                        + "d_Co%0"
                        + String.valueOf(generalConstraints.getNumCourses())
                                        .length()
                        + "d_Se%0"
                        + String.valueOf(generalConstraints.getNumSemesters())
                                        .length() + "d";

        for (int student = 1; student <= numStudents; ++student) {
            for (int course = 1; course <= generalConstraints.getNumCourses(); ++course) {
                for (int semester = 1; semester <= generalConstraints
                                .getNumSemesters(); ++semester) {

                    yStudCourseSem[student][course][semester] = model.addVar(0,
                                    1, 0.0, GRB.BINARY, String.format(format,
                                                    student, course, semester));

                }
            }
        }
        model.update();
        return yStudCourseSem;
    }

    private List<StudentDemand> parseStudentDemandFile(String dataFolder)
                    throws FileNotFoundException, IOException {
        final String csvSplitBy = ",";

        List<StudentDemand> studentDemands = new ArrayList<StudentDemand>();

        BufferedReader bufferedReader = new BufferedReader(new FileReader(
                        dataFolder));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            try {
                String[] strings = line.split(csvSplitBy);
                int[] numbers = new int[strings.length];

                for (int i = 0; i < numbers.length; i++) {
                    numbers[i] = Integer.parseInt(strings[i]);
                }

                if (numbers[0] > numStudents) {
                    numStudents = numbers[0];
                }

                studentDemands.add(new StudentDemand(numbers[0], numbers[1],
                                numbers[2]));
            } catch (NumberFormatException nfE) {
                // a line in the file is malformed. ignore it
            }
        }
        bufferedReader.close();
        return studentDemands;
    }

    private void setObjective(GRBModel model, GRBVar courseSemClassSizeVar)
                    throws GRBException {
        GRBLinExpr objective = new GRBLinExpr();
        objective.addTerm(1, courseSemClassSizeVar);

        model.setObjective(objective, GRB.MINIMIZE);
    }
	
}
