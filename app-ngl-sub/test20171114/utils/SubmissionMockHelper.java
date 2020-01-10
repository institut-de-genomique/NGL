package utils;

import models.laboratory.common.instance.State;
import models.sra.submit.common.instance.Submission;

public class SubmissionMockHelper {

	public static Submission newSubmission(String code)
	{
		Submission submission = new Submission();
		submission.code=code;
		submission.state=new State("S"+code,"test");
		
		return submission;
	}
}
