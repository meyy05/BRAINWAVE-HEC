package Services;

import Models.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;

public class ServicesTache {
    private static final String FICHIER_SAUVEGARDE = "taches_sauvegarde.dat";//fichier de sauvegarde : constante
    
    private List<Tache> taches;//déclaration d'une liste
    private int prochainId;//L'id de la prochaine tache
    
    // Constructeur
    public ServicesTache() {
        this.taches = new ArrayList<>();
        this.prochainId = 1;
        chargerDepuisFichier();
    }
    
    //Charge les tâches depuis le fichier de sauvegarde
    @SuppressWarnings("unchecked")
    private void chargerDepuisFichier() {
        File fichier = new File(FICHIER_SAUVEGARDE);
        
        if (!fichier.exists()) {
            this.taches = new ArrayList<>();
            this.prochainId = 1;
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(FICHIER_SAUVEGARDE))) {
            
            Object obj = ois.readObject();//lecture depuis fichier 
            
            if (obj instanceof List<?>) {
                this.taches = (List<Tache>) obj;
            } else {
                System.err.println("Format du fichier invalide.");
                this.taches = new ArrayList<>();
            }
            
            this.prochainId = ois.readInt();
            
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur lors du chargement : " + e.getMessage());
            this.taches = new ArrayList<>();
            this.prochainId = 1;
        }
    }
    
    //Sauvegarde toutes les tâches dans le fichier
    private void sauvegarderDansFichier() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FICHIER_SAUVEGARDE))) {
            
            oos.writeObject(taches);//écriture dans fichier
            oos.writeInt(prochainId);
            
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }
    // fonctions de Recherche
    //recherche par l'id
    public Tache rechercherTacheParId(int id) {
        for (Tache t : taches) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }
    //recherche par titre
    public Tache rechercherTache(String titre) {
        for (Tache t : taches) {
            if (t.getTitre() != null && t.getTitre().equals(titre)) {
                return t;
            }
        }
        return null;
    }
    // Ajouter tache:
    
    //pour éviter conflit d'incrémentation (double)
    private int obtenirProchainId() {
        return prochainId++;
    }
    //méthode d'ajout d'une tache simple
    public TacheSimple ajouterTacheSimple(String titre, String description,Priorite priorite, LocalDate date,Categorie categorie, Statut statut) {
        
        TacheSimple tache = new TacheSimple(obtenirProchainId(),titre,description,priorite, date,categorie,statut);
        
        taches.add(tache);
        sauvegarderDansFichier();
        
        return tache;
    }
    //méthode d'ajout d'une tache reccurrente
    public TacheReccurrente ajouterTacheReccurente(String titre, String description, Priorite priorite, LocalDate date, Categorie categorie, Statut statut, TypeReccurrence recurrenceType, int interval, LocalDate dateFin,int occurrencesMax, LocalDate prochaineOccurrence) {
        
        TacheReccurrente tache = new TacheReccurrente(obtenirProchainId(),titre,description,priorite,date,categorie, statut,recurrenceType, interval, dateFin, occurrencesMax, prochaineOccurrence);
        taches.add(tache);
        sauvegarderDansFichier();
        
        return tache;
    }
    //méthode d'ajout d'une évaluation
    public Evaluation ajouterEvaluation(String titre, String description, Priorite priorite, LocalDate date, Categorie categorie, Statut statut, String matiere, TypeEvaluation type) {
        Evaluation evaluation = new Evaluation(obtenirProchainId(),titre, description, priorite,  date,categorie, statut,  matiere, type);
        taches.add(evaluation);
        sauvegarderDansFichier();
        return evaluation;
    }
    
    // Modifier
    public boolean modifierTache(int id, String titre, String description, Priorite priorite, LocalDate date, Categorie categorie, Statut statut) {
        
        Tache tache = rechercherTacheParId(id);
        
        if (tache == null) {
            return false;
        }
        
        tache.setTitre(titre);
        tache.setDescription(description);
        tache.setPriorite(priorite);
        tache.setCategorie(categorie);
        tache.setDate(date);
        tache.setStatut(statut);
        
        sauvegarderDansFichier();
        
        return true;
    }
    
    public boolean modifierTacheReccurrente(int id, String titre, String description, Priorite priorite, LocalDate date, Categorie categorie, Statut statut,TypeReccurrence recurrenceType, int interval, LocalDate dateFin,  int occurrencesMax) {
        
        Tache tache = rechercherTacheParId(id);
        
        if (tache == null || !(tache instanceof TacheReccurrente)) {
            return false;
        }
        
        TacheReccurrente tr = (TacheReccurrente) tache;
        
        tr.setTitre(titre);
        tr.setDescription(description);
        tr.setPriorite(priorite);
        tr.setDate(date);
        tr.setCategorie(categorie);
        tr.setStatut(statut);
        tr.setTypeReccurrence(recurrenceType);
        tr.setInterval(interval);
        tr.setDateFin(dateFin);
        tr.setOccurrencesMax(occurrencesMax);
        
        sauvegarderDansFichier();
        
        return true;
    }
    
    public boolean modifierEvaluation(int id, String titre, String description, Priorite priorite, LocalDate date, Categorie categorie, Statut statut, String matiere, TypeEvaluation type) {
        
        Tache tache = rechercherTacheParId(id);
        
        if (tache == null || !(tache instanceof Evaluation)) {
            return false;
        }
        
        Evaluation evaluation = (Evaluation) tache;
        
        evaluation.setTitre(titre);
        evaluation.setDescription(description);
        evaluation.setPriorite(priorite);
        evaluation.setDate(date);
        evaluation.setCategorie(categorie);
        evaluation.setStatut(statut);
        evaluation.setMatiere(matiere);
        evaluation.setType(type);
        
        sauvegarderDansFichier();
        
        return true;
    }
    
    public boolean changerStatut(int id, Statut nouveauStatut) {
        Tache tache = rechercherTacheParId(id);
        
        if (tache == null) {
            return false;
        }
        
        tache.setStatut(nouveauStatut);
        sauvegarderDansFichier();
        
        return true;
    }
    
    // Supprimer tache
    
    public boolean supprimerTache(int id) {
        Tache tache = rechercherTacheParId(id);
        
        if (tache == null) {
            return false;
        }
        
        taches.remove(tache);
        sauvegarderDansFichier();
        
        return true;
    }
    
    // Gestion des taches reccurrentes   
    public List<LocalDate> genererDatesOccurrences(TacheReccurrente tache) {
        List<LocalDate> dates = new ArrayList<>();//initialiser les dates dans une liste 
        LocalDate dateCourante = tache.getDate();
        LocalDate dateFin = tache.getDateFin();
        int occurrencesGenerees = 0;
        int MAX_OCCURRENCES = 1000;
        
        while ((dateFin == null || !dateCourante.isAfter(dateFin)) && (tache.getOccurrencesMax() == 0 || occurrencesGenerees < tache.getOccurrencesMax()) && occurrencesGenerees < MAX_OCCURRENCES) {
            
            dates.add(dateCourante);
            occurrencesGenerees++;
            
            switch (tache.getTypeReccurrence()) {
                case Journaliere:
                    dateCourante = dateCourante.plusDays(tache.getInterval());
                    break;
                case Hebdomadaire:
                    dateCourante = dateCourante.plusWeeks(tache.getInterval());
                    break;
                case Mensuelle:
                    dateCourante = dateCourante.plusMonths(tache.getInterval());
                    break;

            }
        }
        
        return dates;
    }
    //Retourner toutes les tâches simples et récurrentes prévues pour une date donnée: utilisé dans le calendrier
    public List<Tache> getTachesPourDate(LocalDate date) {
        List<Tache> resultats = new ArrayList<>();
        
        for (Tache tache : taches) {
            if (tache instanceof TacheReccurrente) //vérifier si la tache est reccurrente
            	{
                TacheReccurrente tr = (TacheReccurrente) tache;
                List<LocalDate> datesOccurrences = genererDatesOccurrences(tr);
                
                if (datesOccurrences.contains(date)) {
                    TacheSimple occurrence = new TacheSimple(
                        tache.getId() * 1000 + datesOccurrences.indexOf(date),
                        tache.getTitre() + " (Récurrente)",
                        tache.getDescription(),
                        tache.getPriorite(),
                        date,
                        tache.getCategorie(),
                        tache.getStatut()
                    );
                    resultats.add(occurrence);
                }
            } else {
                if (tache.getDate() != null && tache.getDate().equals(date)) {
                    resultats.add(tache);
                }
            }
        }
        
        return resultats;
    }
    
    // Méthodes de tri:
    
    public List<Tache> trierParDate() {
        return taches.stream()
                .sorted(Comparator.comparing(Tache::getDate, 
                    Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }
    
    public List<Tache> trierParPriorite() {
        return taches.stream()
                .sorted(Comparator.comparing(Tache::getPriorite))
                .collect(Collectors.toList());
    }
    
    // Méthode de filtrage
  
    
    public List<Tache> filtrerParCategorie(Categorie categorie) {
        return taches.stream()
                .filter(t -> t.getCategorie() == categorie)
                .collect(Collectors.toList());
    }
    
    public List<Tache> filtrerParStatut(Statut statut) {
        return taches.stream()
                .filter(t -> t.getStatut() == statut)
                .collect(Collectors.toList());
    }
    
    // Méthode pour récuperer la liste des taches
    
    public List<Tache> getToutesTaches() {
        return new ArrayList<>(taches);
    }
    // Méthode pour récuperer le nombre des taches
    public int getNombreTaches() {
        return taches.size();
    }
}