package Models;

import java.io.Serializable;
import java.time.LocalDate;

public class TacheReccurrente extends Tache implements Serializable {
    private static final long serialVersionUID = 4L; // Important pour la sérialisation
    private TypeReccurrence TypeReccurrence;   
	private int interval;                    
	private LocalDate dateFin;              
	private int occurrencesMax;              
	private LocalDate prochaineOccurrence;
	
	public TacheReccurrente(int id,  String titre, String description, Priorite priorite, LocalDate date,
			Categorie categorie, Statut statut, TypeReccurrence recurrenceType, int interval, LocalDate dateFin,
			int occurrencesMax, LocalDate prochaineOccurrence) {
		super(id, titre, description, priorite, date, categorie, statut);
		this.TypeReccurrence = recurrenceType;
		this.interval = interval;
		this.dateFin = dateFin;
		this.occurrencesMax = occurrencesMax;
		this.prochaineOccurrence = prochaineOccurrence;
	}
	

	public TypeReccurrence getTypeReccurrence() {
		return TypeReccurrence;
	}


	public void setTypeReccurrence(TypeReccurrence typeReccurrence) {
		TypeReccurrence = typeReccurrence;
	}


	public int getInterval() {
		return interval;
	}


	public void setInterval(int interval) {
		this.interval = interval;
	}


	public LocalDate getDateFin() {
		return dateFin;
	}


	public void setDateFin(LocalDate dateFin) {
		this.dateFin = dateFin;
	}


	public int getOccurrencesMax() {
		return occurrencesMax;
	}


	public void setOccurrencesMax(int occurrencesMax) {
		this.occurrencesMax = occurrencesMax;
	}
	public LocalDate getProchaineOccurrence() {
	    return prochaineOccurrence;
	}

	public void setProchaineOccurrence(LocalDate prochaineOccurrence) {
	    this.prochaineOccurrence = prochaineOccurrence;
	}


	


	@Override
	public String toString() {
		return "TacheReccurente [TypeReccurrence=" + TypeReccurrence + ", interval=" + interval + ", dateFin=" + dateFin
				+ ", occurrencesMax=" + occurrencesMax + ", prochaineOccurrence=" + prochaineOccurrence + super.toString();
	}

	
}