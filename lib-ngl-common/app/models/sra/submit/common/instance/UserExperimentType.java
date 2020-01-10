package models.sra.submit.common.instance;


public class UserExperimentType {
	private String alias = null;
	private String libraryStrategy = null;
	private String librarySource = null;
	private String librarySelection = null;
	private String libraryProtocol = null;
	private String libraryName = null;
	private String nominalLength = null;
	private String title = null;


	public UserExperimentType() {
	}
	
	public UserExperimentType(String alias) {
		this.setAlias(alias);
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getLibraryStrategy() {
		return libraryStrategy;
	}

	public void setLibraryStrategy(String libraryStrategy) {
		//verifier que libraryStrategy est bien autorise => Controle deporté dans validation
		/*String lcLibraryStrategy = libraryStrategy.toLowerCase();
		if (! VariableSRA.mapLibraryStrategy.containsKey(lcLibraryStrategy)) {
			throw new SraException("La library strategy indiquee '" + libraryStrategy +
					"' n'appartient pas a la liste des valeurs autorisees :\n" +
					VariableSRA.mapLibraryStrategy.keySet().toString());
		}*/
		this.libraryStrategy = libraryStrategy;
	}

	public String getLibrarySource() {
		return librarySource;
	}

	public void setLibrarySource(String librarySource) {
		//verifier que librarySource est  autorisee => Controle deporté dans validation
		/*String lcLibrarySource = librarySource.toLowerCase();
		if (! VariableSRA.mapLibrarySource.containsKey(lcLibrarySource)) {
			throw new SraException("La library source indiquee '" + librarySource +
					"' n'appartient pas a la liste des valeurs autorisees :\n" +
					VariableSRA.mapLibrarySource.keySet().toString());
		}*/
		this.librarySource = librarySource;
	}

	public String getLibrarySelection() {
		return librarySelection;
	}

	public void setLibrarySelection(String librarySelection) {
		//verifier que librarySelection est  autorisee => Controle deporté dans validation
/*		String lcLibrarySelection = librarySelection.toLowerCase();
		if (! VariableSRA.mapLibrarySelection.containsKey(lcLibrarySelection)) {
			throw new SraException("La library selection indiquee '" + librarySelection +
					"' n'appartient pas a la liste des valeurs autorisees :\n" +
					VariableSRA.mapLibrarySelection.keySet().toString());
		}*/
		this.librarySelection = librarySelection;
	}
	public String getLibraryProtocol() {
		return libraryProtocol;
	}

	public void setLibraryProtocol(String libraryProtocol){
		//verifier que librarySelection est  autorisee => Controle deporté dans validation
		this.libraryProtocol = libraryProtocol;
	}
	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getNominalLength() {
		return nominalLength;
	}

	public void setNominalLength(String nominalLength) {
		this.nominalLength = nominalLength;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


}
