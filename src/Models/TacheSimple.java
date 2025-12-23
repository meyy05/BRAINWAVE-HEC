package Models;

import java.io.Serializable;
import java.time.LocalDate;

public class TacheSimple extends Tache implements Serializable {
    private static final long serialVersionUID = 2L;
    public TacheSimple(int id, String titre, String description, Priorite priorite, LocalDate date,
			Categorie categorie, Statut statut) {
		super(id, titre, description, priorite, date, categorie, statut);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
