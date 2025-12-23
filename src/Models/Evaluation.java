package Models;

import java.io.Serializable;
import java.time.LocalDate;

public class Evaluation extends Tache implements Serializable {
    private static final long serialVersionUID = 3L;
    private String matiere;
	TypeEvaluation type;

	public Evaluation(int id, String titre, String description, Priorite priorite, LocalDate date, Categorie categorie,
			Statut statut, String matiere, TypeEvaluation type) {
		super(id, titre, description, priorite, date, categorie, statut);
		this.matiere = matiere;
		this.type = type;
	}

	
	
	
	public String getMatiere() {
		return matiere;
	}

	public void setMatiere(String matiere) {
		this.matiere = matiere;
	}

	public TypeEvaluation getType() {
		return type;
	}

	public void setType(TypeEvaluation type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Evaluation [matiere=" + matiere + ", type=" + type + super.toString();
	}
	
	
}
