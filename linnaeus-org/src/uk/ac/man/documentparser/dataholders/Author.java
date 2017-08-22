package uk.ac.man.documentparser.dataholders;

public class Author {
	private String surname;
	private String forenames;
	private String email;

	public Author(String surname, String fornames, String email){
		this.forenames = fornames;
		this.surname = surname;
		this.email = email;
	}

	/**
	 * @return the surname
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * @return the forenames
	 */
	public String getForenames() {
		return forenames;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	public String toString(){
		if (forenames != null)
			return surname + ", " + forenames;
		else 
			return surname;
	}
}
