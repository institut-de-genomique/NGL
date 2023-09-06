package lims.models.experiment.illumina;

public class RunSolexa {
		public Integer placo; 
		public Integer num; 			// Numero ligne 
    	public String runslnom;       	// Nom du un 
    	public String runsldc; 			// Date de creation 
    	public String runslddt; 		// Date de debut de transfert  
    	public String runsldft;  		// Date de fin de transfert 
    	public Boolean runsldispatch; 	// bit ,// Run dispatch o/n 
		public Long runslnbcluster; 	// nb cluster 
		public Long runslnbseq; 		// nb sequences 
		public Long rnslnbbasetot ;  // nb bases total 
		public String  runslposition; 	// Position 
		public String  runslvrta ; 		// Version RTA 
		public String  runslvflowcell; 	// Version Flowcell 
		public Integer runslctrlane; 	// Controle lane 0 si toutes les lanes ont été utilisées 
		public Integer mismatch; 		//donne la possibilité de modifier la valeur de mismatch par défaut
		@Override
		public String toString() {
			return "RunSolexa [placo=" + placo + ", num=" + num + ", runslnom="
					+ runslnom + ", runsldc=" + runsldc + ", runslddt="
					+ runslddt + ", runsldft=" + runsldft + ", runsldispatch="
					+ runsldispatch + ", runslnbcluster=" + runslnbcluster
					+ ", runslnbseq=" + runslnbseq + ", rnslnbbasetot="
					+ rnslnbbasetot + ", runslposition=" + runslposition
					+ ", runslvrta=" + runslvrta + ", runslvflowcell="
					+ runslvflowcell + ", runslctrlane=" + runslctrlane
					+ ", mismatch=" + mismatch + "]";
		}
		public boolean validate() {
			
			if (mismatch == null) {
				return false;
			}
			
			/*if (num == null) {
				return false;
			}
			if (placo == null) {
				return false;
			}*/
			if (rnslnbbasetot == null) {
				return false;
			}
			/*
			if (runslctrlane == null) {
				return false;
			}
			*/
			if (runsldc == null) {
				return false;
			}
			if (runslddt == null) {
				return false;
			}
			if (runsldft == null) {
				return false;
			}
			if (runsldispatch == null) {
				return false;
			}
			if (runslnbcluster == null) {
				return false;
			}
			if (runslnbseq == null) {
				return false;
			}
			if (runslnom == null) {
				return false;
			}
			/*
			if (runslposition == null) {
				return false;
			}
			*/
			if (runslvflowcell == null) {
				return false;
			}
			if (runslvrta == null) {
				return false;
			}
			return true;
		}
	
}
