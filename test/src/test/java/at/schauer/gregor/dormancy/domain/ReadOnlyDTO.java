package at.schauer.gregor.dormancy.domain;

/**
 * @author Gregor Schauer
 */
public class ReadOnlyDTO {
	public Long id;
	public String value;

	public ReadOnlyDTO() {
	}

	public ReadOnlyDTO(Long id, String value) {
		this.id = id;
		this.value = value;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		throw new UnsupportedOperationException();
	}
}
