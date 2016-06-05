package edu.gatech.cs6310.projectOne;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gurobi.*;

public class GurobiConstraints {
	
	GeneralConstraints generalConstraints = new GeneralConstraints();
	CourseAvailability courseAvail = new CourseAvailability();
	CoursePrerequisites coursePrereq = new CoursePrerequisites();
	
    private int numCourses = generalConstraints.getNumCourses();	
	private int numSemesters = generalConstraints.getNumSemesters();
	private int maxCoursePerSemester = generalConstraints.getMaxCoursePerSemester();
	private int timePerCourseTaken = generalConstraints.getTimePerCourseTaken();
	
    private int numStudents;
    private Double objectiveValue = null;

    private GRBVar[][][] yijk;  //i: student; j = course; k = semester
    

    private void generateClassSizeConstraints(GRBVar classSize, GRBModel model)	
        throws GRBException {
         for (int course = 1; course <= numCourses; course++) {
            for (int semester = 1; semester <= numSemesters; semester++) {
            	
                GRBLinExpr classSizeGRB = new GRBLinExpr();
                
                for (int student = 1; student <= numStudents; student++) {
                    classSizeGRB.addTerm(1, yijk[student][course][semester]);
                }

                if (courseAvail.courseAvailability[course-1][semester-1] == true) {
                    String cname = String.format("ClassCapacity_Co%d_Se%d", course, semester);
                    model.addConstr(classSizeGRB, 
                    		        GRB.LESS_EQUAL, 
                    		        classSize, 
                    		        cname);
                } else {
                    String cname = String.format("ClassCapacity_CourseNotOffered_Co%d_Se%d", course, semester);
                    model.addConstr(classSizeGRB, 
                    		        GRB.LESS_EQUAL, 
                    		        0, 
                    		        cname);
                }
            }
        }
    }
    

    private void generateCoursePrerequisiteConstraint( GRBModel model) 
        throws GRBException {
    	for (int cp = 0; cp < coursePrereq.coursePrerequisites.length; cp++) {
    		int prereq = coursePrereq.coursePrerequisites[cp][0];
    		int postreq = coursePrereq.coursePrerequisites[cp][1];
    		
    		for (int student = 1; student <= numStudents; student++) {
                for (int semester = 1; semester <= numSemesters; semester++) {
                	
                    GRBLinExpr coursePrerequisiteGRB = new GRBLinExpr();
                    
                    for (int k = 1; k < semester; k++) {
                        coursePrerequisiteGRB.addTerm(1, yijk[student][prereq][k]);
                    }
                    String cname = String.format("Prereq_St%d_Co%d_%d", student, prereq, postreq);
                    model.addConstr(coursePrerequisiteGRB,
                                    GRB.GREATER_EQUAL,
                                    yijk[student][postreq][semester],
                                    cname);
                }
    		}
    	}
    }

    
    private void generateStudentDemandConstraint(List<StudentDemand> studentDemands, GRBModel model)
        throws GRBException {
    	for (StudentDemand sd : studentDemands) {
    	       int student = sd.getStudentID();
    	       int course = sd.getCourseID();

    	       GRBLinExpr studentDemandGRB = new GRBLinExpr();
    	       
    	       for (int semester = 1; semester <= numSemesters; semester++) {
    	       	studentDemandGRB.addTerm(1, yijk[student][course][semester]);
    	       }

    	       String cname = String.format("St%d_demand_Co%d", student, course);
    	       model.addConstr(studentDemandGRB, 
    	       		        GRB.EQUAL, 
    	       		        1, 
    	       		        cname);
    	}
    }

    
    private void generateMaxCoursePerSemesterConstraint(GRBModel model) 
        throws GRBException {
        for (int student = 1; student <= numStudents; student++) {
            for (int semester = 1; semester <= numSemesters; semester++) {
            	
                GRBLinExpr maxCoursePerSemesterGRB = new GRBLinExpr();
                
                for (int course = 1; course <= numCourses; course++) {
                	maxCoursePerSemesterGRB.addTerm(1, yijk[student][course][semester]);
                }

                String cname = String.format("FullLoad_St%d_Se%d", student, semester);
                model.addConstr(maxCoursePerSemesterGRB, 
                		        GRB.LESS_EQUAL,
                                maxCoursePerSemester, 
                                cname);
            }
        }
    }

    
    private void generateCourseTakenTimeConstraint(GRBModel model) 
        throws GRBException {
        for (int student = 1; student <= numStudents; student++) {
            for (int course = 1; course <= numCourses; course++) {
            	
                GRBLinExpr timePerCourseTakenGRB = new GRBLinExpr();

                for (int semester = 1; semester <= numSemesters; semester++) {
                	timePerCourseTakenGRB.addTerm(1, yijk[student][course][semester]);

                }
                String cname = String.format("St%d_take_Co%d_only_once", student, course);
                model.addConstr(timePerCourseTakenGRB,
                		        GRB.LESS_EQUAL,
                		        timePerCourseTaken,
                		        cname);
            }
        }
    }
}
