package Models;

public enum Statut  {
	A_faire,
	En_cours,
	Terminee;
	@Override
    public String toString() {
        switch(this) {
            case A_faire: return "À faire";
            case En_cours: return "En cours";
            case Terminee: return "Terminée";
            default: return super.toString();
        }
    }
	public String getIcone() {
        switch(this) {
            case A_faire: return "⭕";
            case En_cours: return "🔄";
            case Terminee: return "✅";
            default: return "❓";
        }
    }

}
