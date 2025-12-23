package Models;

public enum Priorite  {
	Urgente,
	Haute,
	Moyenne,
	Faible;
	
	@Override
    public String toString() {
        switch(this) {
            case Urgente: return "Urgente";
            case Haute: return "Haute";
            case Moyenne: return "Moyenne";
            case Faible: return "Faible";
            default: return super.toString();
        }
    }
    public String getCouleur() {
        switch(this) {
            case Urgente: return "#E74C3C";  // Rouge
            case Haute: return "#E67E22";    // Orange
            case Moyenne: return "#F39C12";  // Jaune
            case Faible: return "#27AE60";   // Vert
            default: return "#95A5A6";       // Gris
        }
    }
}


