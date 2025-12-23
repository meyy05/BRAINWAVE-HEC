package Models;

public enum Categorie  {
	Projet,
	TP,
	Revision,
	Td,
	Exercices;
	
	@Override
	public String toString() {
		switch(this) {
		case Projet : return "Projet";
		case TP: return "TP";
		case Revision: return "Révision";
		case Td: return "TD";
		case Exercices: return "Exercices";
		default: return super.toString();
		}
	}
}
