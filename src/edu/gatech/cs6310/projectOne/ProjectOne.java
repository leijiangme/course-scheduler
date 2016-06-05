package edu.gatech.cs6310.projectOne;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class ProjectOne {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		  int filenameIndex = Arrays.asList(args).indexOf("-i") + 1;

	        if (filenameIndex == 0 || filenameIndex >= args.length) {
	            throw new InvalidParameterException("Usage: java "
	                            + ProjectOne.class.getName()
	                            + " -i <student demands filename>");
	        }

	        String inputFilename = args[filenameIndex];

	        GurobiScheduler scheduler = new GurobiScheduler();
	        double objValue = scheduler.calculateSchedule(inputFilename);
	        System.out.printf("X=%.2f", objValue);

	}

}
