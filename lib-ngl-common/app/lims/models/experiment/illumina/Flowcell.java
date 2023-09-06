package lims.models.experiment.illumina;

import java.util.Collection;

import lims.models.experiment.ContainerSupport;

public class Flowcell implements ContainerSupport {

	public String containerSupportCode;
	
	public Collection<Lane> lanes;
	
}
