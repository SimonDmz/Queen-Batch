package fr.insee.queen.batch;

public enum Model {
	NOMENCLATURE("nomenclatures"),
	SAMPLE("sample");
	/**
	 * label of the Model
	 */
	private String label;

	/**
	 * Defaut constructor for a Model
	 * @param label
	 */
	Model(String label) {
		this.label = label;
	}

	/**
	 * Get the label for a Model
	 * @return label
	 */
	public String getLabel() {
		return label;
	}
}
