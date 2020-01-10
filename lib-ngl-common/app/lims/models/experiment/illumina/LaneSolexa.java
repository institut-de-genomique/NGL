package lims.models.experiment.illumina;

public class LaneSolexa {
	
	public Integer lanenum;
	public Integer matmaco; 
	public Long lmnbseq; 
	public Long lmnbclustfiltr; 
	public Double lmperseqfiltr; 
	public Long lmnbclust; 
	public Double lmperclustfiltr; 
	public Long lmnbbase;
	public String lmnbcycle;
	public Integer pistnbcycle; 
	public String lmphasing;
	public String lmprephasing;
	@Override
	public String toString() {
		return "LaneSolexa [lanenum=" + lanenum + ", matmaco=" + matmaco
				+ ", lmnbseq=" + lmnbseq + ", lmnbclustfiltr=" + lmnbclustfiltr
				+ ", lmperseqfiltr=" + lmperseqfiltr + ", lmnbclust="
				+ lmnbclust + ", lmperclustfiltr=" + lmperclustfiltr
				+ ", lmnbbase=" + lmnbbase + ", lmnbcycle=" + lmnbcycle
				+ ", pistnbcycle=" + pistnbcycle + ", lmphasing=" + lmphasing
				+ ", lmprephasing=" + lmprephasing + "]";
	}
	
}
