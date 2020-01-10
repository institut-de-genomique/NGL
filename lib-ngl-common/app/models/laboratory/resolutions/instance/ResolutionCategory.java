package models.laboratory.resolutions.instance;

public class ResolutionCategory {
	
	public String name;
	public Short  displayOrder;
	
	public ResolutionCategory() {
	}
	
	public ResolutionCategory(String name, short displayOrder) {
		this.name         = name;
		this.displayOrder = displayOrder;
	}

}
