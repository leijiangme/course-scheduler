package edu.gatech.cs6310.projectOne;

import java.security.InvalidParameterException;
import java.util.Arrays;

public class ProjectOne {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int argsIndex = Arrays.asList(args).indexOf("-i") + 1;

		if (argsIndex == 0 || argsIndex >= args.length) {
			throw new InvalidParameterException("Usage: ProjectOne -i /path/to/Student_Demand_<#>");
		}
		
		String csvFileName = args[argsIndex];
		GurobiScheduler scheduler = new GurobiScheduler();
		System.out.printf("X=%.2f", scheduler.calculateSchedule(csvFileName));
	}
}
