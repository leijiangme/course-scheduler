package edu.gatech.cs6310.projectOne;

import java.util.List;

import gurobi.*;

public abstract class Scheduler {

	protected abstract void generateConstraints() throws GRBException;
	
}
