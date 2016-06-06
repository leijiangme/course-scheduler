package edu.gatech.cs6310.projectOne;

import gurobi.*;

public abstract class Scheduler {

	protected abstract void generateConstraints() throws GRBException;
	
	protected abstract void initializeYijk() throws GRBException;
	
	protected abstract void initializeX() throws GRBException;
	
	protected abstract void setObjective() throws GRBException;
	
}
