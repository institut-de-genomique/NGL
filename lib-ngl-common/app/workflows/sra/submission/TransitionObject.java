package workflows.sra.submission;

import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;


public interface TransitionObject {
    State getState(); // inutile de mettre public abstract car on est dans interface
    void setState(State state);
	TraceInformation getTraceInformation();
}
