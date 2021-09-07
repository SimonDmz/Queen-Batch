package fr.insee.queen.batch.enums;

public enum BatchOption {
	EXTRACTDATA("EXTRACTDATA"), EXTRACTDATACOMPLETE("EXTRACTDATACOMPLETE"), 
	LOADDATA("LOADDATA"), DELETEDATA("DELETEDATA");
	
	/**
	 * label of the BatchOption
	 */
	private String label;

	/**
	 * Defaut constructor for BatchOption
	 * @param label
	 */
	BatchOption(String label) {
		this.label = label;
	}

	/**
	 * Get the label for BatchOption
	 * @return label
	 */
	public String getLabel() {
		return label;
	}
}
