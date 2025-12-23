package Models;

import java.io.Serializable;
import java.time.LocalDate;

// ⚠️ IMPORTANT : Ajouter "implements Serializable" pour la persistance
public class Tache implements Serializable {
    private static final long serialVersionUID = 1L; // Important pour la sérialisation
    protected int id;
	protected String titre,description;
	protected Priorite priorite;
	protected LocalDate Date;
	protected Categorie categorie;
	protected Statut statut;
	
	public Tache(int id, String titre, String description, Priorite priorite, LocalDate date,
			Categorie categorie, Statut statut) {
		super();
		this.id = id;
		this.titre = titre;
		this.description = description;
		this.priorite = priorite;
		Date = date;
		this.categorie = categorie;
		this.statut = statut;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitre() {
		return titre;
	}
	public void setTitre(String titre) {
		this.titre = titre;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Priorite getPriorite() {
		return priorite;
	}
	public void setPriorite(Priorite priorite) {
		this.priorite = priorite;
	}
	public LocalDate getDate() {
		return Date;
	}
	public void setDate(LocalDate date) {
		Date = date;
	}
	public Categorie getCategorie() {
		return categorie;
	}
	public void setCategorie(Categorie categorie) {
		this.categorie = categorie;
	}
	public Statut getStatut() {
		return statut;
	}
	public void setStatut(Statut statut) {
		this.statut = statut;
	}
	@Override
	public String toString() {
		return "Tache [id=" + id +  ", titre=" + titre + ", description=" + description
				+ ", priorite=" + priorite + ", Date=" + Date + ", categorie=" + categorie + ", statut=" + statut + "]";
	}
}
