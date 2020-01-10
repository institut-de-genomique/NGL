package controllers.experiments.api;

import java.util.Set;

import controllers.ListForm;

public class ExperimentUpdateForm  extends ListForm{
	public boolean stopProcess;
	public boolean retry;
	public String nextStateCode;
	public Set<String> processResolutionCodes;
}
