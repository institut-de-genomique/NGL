package lims.models;

public class Manip {
	
	public Integer matmaco;
	public String matmanom;
	public String plaqueId;
	public String plaqueX;
	public String plaqueY;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((matmaco == null) ? 0 : matmaco.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Manip other = (Manip) obj;
		if (matmaco == null) {
			if (other.matmaco != null)
				return false;
		} else if (!matmaco.equals(other.matmaco))
			return false;
		return true;
	}

}
