package at.schauer.gregor.dormancy.domain;

/**
 * @author Gregor Schauer
 */
public class WriteOnlyDTO {
	public Long id;
	public String value;

	public WriteOnlyDTO() {
	}

	public WriteOnlyDTO(Long id, String value) {
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
		throw new UnsupportedOperationException();
	}

	public void setValue(String value) {
		this.value = value;
	}
}
