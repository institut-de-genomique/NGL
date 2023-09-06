package lims.models.experiment.illumina;

public class BanqueSolexa {
	
	public Integer banco;
	public String prsco;
	public String adnnom;
	public Integer lanenum;
	public Integer laneco;
	public String tagkeyseq;
	
	@Override
	public String toString() {
		return "BanqueSolexa [banco=" + banco + ", prsco=" + prsco
				+ ", adnnom=" + adnnom + ", lanenum=" + lanenum + ", laneco="
				+ laneco + "]";
	}

}
