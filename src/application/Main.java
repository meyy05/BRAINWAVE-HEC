package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import Services.ServicesTache;
import Models.*;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main extends Application {
    
    private ServicesTache serviceTache; //déclaration d'un service tache
    private VBox taskGrid;
    private VBox evaluationGrid; 
    private Label statTotal, statDone, statProgress, statTodo;
    private TextField searchField;
    private ComboBox<String> sortCombo, periodCombo;
    private ToggleGroup categoryGroup;
    private VBox detailPanel;
    private TabPane mainTabPane;
    private GridPane calendarGrid;
    private Label monthTitleLabel;
    private String selectedCategory = "ALL";
    private Tache selectedTask = null;
    private LocalDate dateActuelle = LocalDate.now();
    
    @Override
    public void start(Stage primaryStage) //la première fenêtre créée automatiquement par JavaFX
    {
        serviceTache = new ServicesTache();

        // Container principal
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Layout principal
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // Header
        root.setTop(createHeader());

        // Sidebar
        VBox sidebar = createSidebar();
        sidebar.setPrefWidth(320);

        // Main content
        VBox mainContent = createMainContent();
        HBox.setHgrow(mainContent, Priority.ALWAYS);
        mainContent.setMaxWidth(Double.MAX_VALUE);

        mainLayout.getChildren().addAll(sidebar, mainContent);
        root.setCenter(mainLayout);

        // ScrollPane
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPrefViewportWidth(1000);
        scrollPane.setPrefViewportHeight(500);
        scrollPane.getStyleClass().add("scroll-pane");

        // Scene avec CSS
        Scene scene = new Scene(scrollPane, 1000, 500);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());//appel du fichier css

        primaryStage.setTitle("BRAINWAVE'HEC – Planner Intelligent");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialiser l'affichage
        refreshTasks();
        refreshEvaluations();
        updateStats();
    }

    private HBox createHeader() {
    	HBox header = new HBox(20);
    	header.setPadding(new Insets(20, 32, 20, 32));
    	header.setAlignment(Pos.CENTER_LEFT);
    	header.getStyleClass().add("header");

    	// Logo
    	Image logo = new Image(
    	    Objects.requireNonNull(
    	        getClass().getResource("logo.png"),
    	        "Logo introuvable"
    	    ).toExternalForm()
    	);

    	ImageView logoView = new ImageView(logo);
    	logoView.setFitHeight(180);  // Taille réduite pour équilibre
    	logoView.setFitWidth(180);
    	logoView.setPreserveRatio(true);

    	// Titre
    	VBox titleBox = new VBox(4);  // Espacement entre les labels
    	titleBox.setAlignment(Pos.CENTER_LEFT);  // Important pour l'alignement vertical

    	Label title = new Label("BRAINWAVE'HEC");
    	title.setStyle("-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: #284E7B;");

    	Label subtitle = new Label("Planner intelligent – IHEC Carthage");
    	subtitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: rgba(255, 255, 255, 0.95);");

    	Label slogan = new Label("« Organisez votre esprit, maîtrisez votre temps »");
    	slogan.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #A53860;");

        // Boutons d'ajout
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button newEvaluationBtn = new Button("📝 Nouvelle évaluation");
        newEvaluationBtn.getStyleClass().add("btn-strong");
        newEvaluationBtn.setOnAction(e -> showEvaluationDialog(null));
        
        Button newTaskBtn = new Button("➕ Nouvelle tâche");
        newTaskBtn.getStyleClass().add("btn-primary");
        newTaskBtn.setOnAction(e -> showTaskDialog(null));
        
        buttonsBox.getChildren().addAll(newEvaluationBtn, newTaskBtn);

        titleBox.getChildren().addAll(title, subtitle, slogan);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        header.getChildren().addAll(logoView, titleBox, buttonsBox);
        return header;
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(16);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(350);
        sidebar.setPadding(new Insets(0, 0, 20, 0));
        
        // 1. RECHERCHE 
        VBox searchCard = createCard("🔍 Recherche");
        searchField = new TextField();
        searchField.setPromptText("Rechercher une tâche...");
        searchField.textProperty().addListener((obs, old, newVal) -> refreshTasks());
        searchField.getStyleClass().add("text-field");
        
        Label searchHint = new Label("Recherchez par titre ou description");
        searchHint.getStyleClass().add("small-text");
        
        searchCard.getChildren().addAll(searchField, searchHint);
        
        // 2. DÉTAILS DE TÂCHE 
        VBox detailCard = createCard("📌 Détails de la tâche");
        detailCard.setStyle("-fx-padding: 20;"); 
        
        detailPanel = new VBox();
        detailPanel.setStyle("-fx-background-color: white; -fx-padding: 0;");
        detailPanel.setPrefHeight(350);
        detailPanel.setMinHeight(300);
        
        // Centrer le message d'absence de sélection
        VBox centerBox = new VBox();
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(40));
        
        Label noSelection = new Label("Aucune tâche sélectionnée\n\nCliquez sur une tâche pour voir ses détails");
        noSelection.getStyleClass().add("small-text");
        noSelection.setAlignment(Pos.CENTER);
        noSelection.setWrapText(true);
        noSelection.setTextAlignment(TextAlignment.CENTER);
        
        centerBox.getChildren().add(noSelection);
        detailPanel.getChildren().add(centerBox);
        
        detailCard.getChildren().add(detailPanel);
        
        // 3. CATÉGORIES 
        VBox catCard = createCard("📚 Catégories");
        categoryGroup = new ToggleGroup();
        
        String[] categories = {"ALL:Toutes", "Projet:Projet", "TP:TP", 
                               "TD:TD", "Revision:Révision", "Exercices:Exercices"};
        
        VBox catList = new VBox(6);
        for (String cat : categories) {
            String[] parts = cat.split(":");
            RadioButton rb = new RadioButton(parts[1]);
            rb.setToggleGroup(categoryGroup);
            rb.setUserData(parts[0]);
            rb.getStyleClass().add("check-box");
            
            if (parts[0].equals("ALL")) {
                rb.setSelected(true);
            }
            
            rb.setOnAction(e -> {
                selectedCategory = (String) rb.getUserData();
                refreshTasks();
            });
            
            catList.getChildren().add(rb);
        }
        
        catCard.getChildren().add(catList);
        
        // 4. FILTRES
        VBox filterCard = createCard("⚙️ Filtres");
        
        Label sortLabel = new Label("Tri");
        sortLabel.getStyleClass().add("form-label");
        
        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll("Par date", "Par priorité", "Par statut");
        sortCombo.setValue("Par date");
        sortCombo.setMaxWidth(Double.MAX_VALUE);
        sortCombo.getStyleClass().add("combo-box");
        sortCombo.setOnAction(e -> refreshTasks());
        
        Label periodLabel = new Label("Période");
        periodLabel.getStyleClass().add("form-label");
        periodLabel.setPadding(new Insets(8, 0, 0, 0));
        
        periodCombo = new ComboBox<>();
        periodCombo.getItems().addAll("Toutes", "Aujourd'hui", "Cette semaine", "Ce mois");
        periodCombo.setValue("Toutes");
        periodCombo.setMaxWidth(Double.MAX_VALUE);
        periodCombo.getStyleClass().add("combo-box");
        periodCombo.setOnAction(e -> refreshTasks());
        
        filterCard.getChildren().addAll(sortLabel, sortCombo, periodLabel, periodCombo);
        
        sidebar.getChildren().addAll(searchCard, detailCard, catCard, filterCard);
        
        return sidebar;
    }
   
    private VBox createMainContent() {
        VBox main = new VBox(20);
        main.setMaxWidth(Double.MAX_VALUE);
        main.setFillWidth(true);
        
        // Stats
        HBox statsBox = createStatsBox();
        
        // Tabs
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainTabPane.getStyleClass().add("main-tab-pane");
        
        // Tab Tâches
        Tab tasksTab = new Tab("📋 Tâches");
        ScrollPane tasksScroll = new ScrollPane();
        tasksScroll.setFitToWidth(true);
        tasksScroll.getStyleClass().add("scroll-pane");
        
        taskGrid = new VBox(12);
        taskGrid.setPadding(new Insets(10));
        tasksScroll.setContent(taskGrid);
        tasksTab.setContent(tasksScroll);
        
        // Tab Évaluations
        Tab evaluationsTab = new Tab("📚 Évaluations");
        ScrollPane evalScroll = new ScrollPane();
        evalScroll.setFitToWidth(true);
        evalScroll.getStyleClass().add("scroll-pane");
        
        evaluationGrid = new VBox(12);
        evaluationGrid.setPadding(new Insets(10));
        evalScroll.setContent(evaluationGrid);
        evaluationsTab.setContent(evalScroll);
        
        // Tab Calendrier
        Tab calendarTab = new Tab("📅 Calendrier");
        calendarTab.setContent(createCalendarView());
        
        mainTabPane.getTabs().addAll(tasksTab, evaluationsTab, calendarTab);
        VBox.setVgrow(mainTabPane, Priority.ALWAYS);
        
        main.getChildren().addAll(statsBox, mainTabPane);
        return main;
    }
    
    private VBox createCalendarView() {
        VBox view = new VBox(10);
        view.setPadding(new Insets(20));
        view.getStyleClass().add("detail-card");
        
        // Barre de navigation
        HBox monthNav = new HBox(10);
        monthNav.setAlignment(Pos.CENTER);
        monthNav.setPadding(new Insets(0, 0, 15, 0));
        
        Button prevBtn = new Button("◀ Mois précédent");
        prevBtn.getStyleClass().add("btn-secondary");
        prevBtn.setOnAction(e -> {
            dateActuelle = dateActuelle.minusMonths(1);
            updateCalendarDisplay();
        });
        
        monthTitleLabel = new Label();
        monthTitleLabel.getStyleClass().add("section-title");
        monthTitleLabel.setPadding(new Insets(0, 20, 0, 20));
        
        Button nextBtn = new Button("Mois suivant ▶");
        nextBtn.getStyleClass().add("btn-secondary");
        nextBtn.setOnAction(e -> {
            dateActuelle = dateActuelle.plusMonths(1);
            updateCalendarDisplay();
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button todayBtn = new Button("📅 Aujourd'hui");
        todayBtn.getStyleClass().add("btn-strong");
        todayBtn.setOnAction(e -> {
            dateActuelle = LocalDate.now().withDayOfMonth(1);
            updateCalendarDisplay();
        });
        
        monthNav.getChildren().addAll(prevBtn, monthTitleLabel, nextBtn, spacer, todayBtn);
        
        // Grille du calendrier
        calendarGrid = new GridPane();
        calendarGrid.setHgap(2);
        calendarGrid.setVgap(2);
        calendarGrid.setPadding(new Insets(10));
        calendarGrid.getStyleClass().add("calendar-grid");
        
        // En-têtes des jours
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (int i = 0; i < 7; i++) {
            Label dayHeader = new Label(joursSemaine[i]);
            dayHeader.getStyleClass().add("calendar-day-header");
            dayHeader.setAlignment(Pos.CENTER);
            dayHeader.setPrefSize(120, 35);
            calendarGrid.add(dayHeader, i, 0);
        }
        
        view.getChildren().addAll(monthNav, calendarGrid);
        updateCalendarDisplay();
        
        return view;
    }
    
    private void updateCalendarDisplay() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        monthTitleLabel.setText(dateActuelle.format(formatter));
        
        // Nettoyer les cellules
        for (int i = 7; i < 49; i++) {
            int row = i / 7;
            int col = i % 7;
            if (row > 0 && row < 7) {
                calendarGrid.getChildren().removeIf(node -> 
                    GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col
                );
            }
        }
        
        LocalDate firstDay = dateActuelle.withDayOfMonth(1);
        int daysInMonth = firstDay.lengthOfMonth();
        int startDay = (firstDay.getDayOfWeek().getValue() + 6) % 7;
        
        for (int i = 0; i < 42; i++) {
            int row = i / 7 + 1;
            int col = i % 7;
            
            VBox dayCell = new VBox(4);
            dayCell.setPadding(new Insets(8));
            dayCell.setPrefSize(120, 100);
            dayCell.setMinSize(100, 90);
            dayCell.getStyleClass().add("calendar-day-cell");
            
            int dayNum = i - startDay + 1;
            
            if (i >= startDay && dayNum <= daysInMonth) {
                LocalDate date = LocalDate.of(firstDay.getYear(), firstDay.getMonth(), dayNum);
                
                Label dayNumber = new Label(String.valueOf(dayNum));
                dayNumber.getStyleClass().add("calendar-day-number");
                
                if (date.equals(LocalDate.now())) {
                    dayCell.getStyleClass().add("calendar-today");
                }
                
                dayCell.getChildren().add(dayNumber);
                
                List<Tache> dayTasks = serviceTache.getTachesPourDate(date).stream()
                    .limit(3)
                    .collect(Collectors.toList());
                
                for (Tache task : dayTasks) {
                    HBox taskItem = new HBox(4);
                    taskItem.setAlignment(Pos.CENTER_LEFT);
                    
                    Circle dot = new Circle(4);
                    String color = task.getPriorite().getCouleur();
                    dot.setFill(Color.web(color));
                    
                    String taskTitle = task.getTitre();
                    if (task instanceof Evaluation) {
                        taskTitle = "📚 " + taskTitle;
                    }
                    if (task.getStatut() == Statut.Terminee) {
                        taskTitle = "✅ " + taskTitle;
                    }
                    
                    Label taskLabel = new Label(taskTitle);
                    if (task.getStatut() == Statut.Terminee) {
                        taskLabel.getStyleClass().add("calendar-task-done");
                    } else {
                        taskLabel.getStyleClass().add("calendar-task");
                    }
                    taskLabel.setMaxWidth(100);
                    taskLabel.setWrapText(true);
                    
                    taskItem.getChildren().addAll(dot, taskLabel);
                    dayCell.getChildren().add(taskItem);
                    
                    String tooltipText = "📝 " + task.getTitre() + "\n" +
                        "📅 " + task.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                        "⚡ Priorité: " + task.getPriorite() + "\n" +
                        "📂 Catégorie: " + task.getCategorie() + "\n" +
                        "📊 Statut: " + (task.getStatut() == Statut.A_faire ? "À faire" : 
                                        task.getStatut() == Statut.En_cours ? "En cours" : "Terminée");
                    
                    if (task instanceof Evaluation) {
                        Evaluation eval = (Evaluation) task;
                        tooltipText += "\n📖 Matière: " + eval.getMatiere() + 
                                     "\n🎯 Type: " + eval.getType();
                    }
                    
                    Tooltip tip = new Tooltip(tooltipText);
                    Tooltip.install(taskItem, tip);
                    taskItem.setOnMouseClicked(e -> showTaskDetail(task));
                }
                
                long totalTasks = serviceTache.getTachesPourDate(date).size();
                if (totalTasks > 3) {
                    Label moreLabel = new Label("... +" + (totalTasks - 3) + " autres");
                    moreLabel.getStyleClass().add("calendar-more-tasks");
                    dayCell.getChildren().add(moreLabel);
                }
                
                dayCell.setOnMouseClicked(e -> showAllTasksForDay(date));
                
                dayCell.setOnMouseEntered(evt -> {
                    if (!date.equals(LocalDate.now())) {
                        dayCell.getStyleClass().add("calendar-day-hover");
                    }
                });
                
                dayCell.setOnMouseExited(evt -> {
                    if (!date.equals(LocalDate.now())) {
                        dayCell.getStyleClass().remove("calendar-day-hover");
                    }
                });
            } else {
                dayCell.getStyleClass().add("calendar-day-outside");
            }
            
            calendarGrid.add(dayCell, col, row);
        }
    }
    
    private void showAllTasksForDay(LocalDate date) {
        Stage dayWindow = new Stage();
        dayWindow.setTitle("Tâches du " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getStyleClass().add("detail-card");
        
        Label title = new Label("📅 " + date.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH)));
        title.getStyleClass().add("section-title");
        
        VBox tasksList = new VBox(10);
        
        List<Tache> dayTasks = serviceTache.getTachesPourDate(date).stream()
            .sorted((a, b) -> a.getPriorite().compareTo(b.getPriorite()))
            .collect(Collectors.toList());
        
        if (dayTasks.isEmpty()) {
            Label noTasks = new Label("Aucune tâche prévue pour ce jour");
            noTasks.getStyleClass().add("small-text");
            noTasks.setPadding(new Insets(20));
            tasksList.getChildren().add(noTasks);
        } else {
            for (Tache task : dayTasks) {
                HBox taskCard = new HBox(12);
                taskCard.setPadding(new Insets(12));
                taskCard.getStyleClass().add("detail-field");
                taskCard.setAlignment(Pos.CENTER_LEFT);
                
                Circle priorityCircle = new Circle(8);
                priorityCircle.setFill(Color.web(task.getPriorite().getCouleur()));
                
                VBox taskInfo = new VBox(5);
                
                String taskTitle = task.getTitre();
                if (task instanceof Evaluation) {
                    taskTitle = "📚 " + taskTitle;
                }
                
                Label taskTitleLabel = new Label(taskTitle);
                taskTitleLabel.getStyleClass().add("form-label");
                
                HBox taskMeta = new HBox(10);
                taskMeta.setAlignment(Pos.CENTER_LEFT);
                
                Label category = new Label("📂 " + task.getCategorie());
                category.getStyleClass().add("small-text");
                
                Label priority = new Label("⚡ " + task.getPriorite());
                priority.getStyleClass().add("small-text");
                
                Label status = new Label(getStatusIcon(task.getStatut()) + " " + formatStatut(task.getStatut()));
                status.getStyleClass().add("small-text");
                
                if (task instanceof Evaluation) {
                    Evaluation eval = (Evaluation) task;
                    Label matiereLabel = new Label("📖 " + eval.getMatiere());
                    matiereLabel.getStyleClass().add("small-text");
                    taskMeta.getChildren().add(matiereLabel);
                }
                
                taskMeta.getChildren().addAll(category, priority, status);
                taskInfo.getChildren().addAll(taskTitleLabel, taskMeta);
                
                HBox.setHgrow(taskInfo, Priority.ALWAYS);
                
                Button detailBtn = new Button("Voir");
                detailBtn.getStyleClass().add("btn-secondary");
                detailBtn.setOnAction(e -> {
                    showTaskDetail(task);
                    dayWindow.close();
                });
                
                taskCard.getChildren().addAll(priorityCircle, taskInfo, detailBtn);
                taskCard.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        showTaskDetail(task);
                        dayWindow.close();
                    }
                });
                
                tasksList.getChildren().add(taskCard);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(tasksList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(350);
        scrollPane.getStyleClass().add("scroll-pane");
        
        Button closeBtn = new Button("Fermer");
        closeBtn.getStyleClass().add("btn-secondary");
        closeBtn.setOnAction(e -> dayWindow.close());
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeBtn);
        
        layout.getChildren().addAll(title, scrollPane, buttonBox);
        
        Scene scene = new Scene(layout, 500, 450);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        dayWindow.setScene(scene);
        dayWindow.show();
    }

    private String getStatusIcon(Statut statut) {
        switch (statut) {
            case A_faire: return "⭕";
            case En_cours: return "🔄";
            case Terminee: return "✅";
            default: return "❓";
        }
    }
    
    private HBox createStatsBox() {
        HBox stats = new HBox(12);
        
        statTotal = createStatCard("Total tâches", "0", "total");
        statDone = createStatCard("Terminées", "0", "success");
        statProgress = createStatCard("En cours", "0", "warning");
        statTodo = createStatCard("À faire", "0", "danger");
        
        stats.getChildren().addAll(statTotal.getParent(), statDone.getParent(), 
                                   statProgress.getParent(), statTodo.getParent());
        return stats;
    }
    
    private Label createStatCard(String label, String value, String type) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16));
        
        switch (type) {
            case "total":
                card.getStyleClass().add("stat-card");
                break;
            case "success":
                card.getStyleClass().add("stat-card-success");
                break;
            case "warning":
                card.getStyleClass().add("stat-card-warning");
                break;
            case "danger":
                card.getStyleClass().add("stat-card-danger");
                break;
        }
        
        HBox.setHgrow(card, Priority.ALWAYS);
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        
        Label labelText = new Label(label);
        labelText.getStyleClass().add("stat-label");
        
        card.getChildren().addAll(valueLabel, labelText);
        return valueLabel;
    }
    
    private VBox createCard(String title) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("sidebar-card");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("sidebar-label");
        card.getChildren().add(titleLabel);
        
        return card;
    }
    
    private VBox createTaskCard(Tache tache) {
        VBox card = new VBox(10);
        card.setMaxWidth(Double.MAX_VALUE); 
        card.setPadding(new Insets(14));
        card.getStyleClass().add("detail-card");
        card.getStyleClass().add("task-card");
        card.setStyle(card.getStyle() + "-fx-border-width: 0 0 0 6; " +
                     "-fx-border-color: " + getPriorityColor(tache.getPriorite()) + "; " +
                     "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");
        
        // En-tête avec titre et date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(4);
        
        // Titre avec icône spéciale pour les évaluations
        String titreAffiche = tache.getTitre();
        if (tache instanceof Evaluation) {
            titreAffiche = "📚 " + titreAffiche;
        }
        
        Label title = new Label(titreAffiche);
        title.getStyleClass().add("form-label");
        title.setStyle("-fx-font-size: 15px;");
        
        Label meta = new Label(tache.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                              " · " + tache.getCategorie());
        meta.getStyleClass().add("small-text");
        
        titleBox.getChildren().addAll(title, meta);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        
        // ✅ CORRECTION : ComboBox avec les 3 options COMPLÈTES
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("À faire", "En cours", "Terminée");
        
        // Définir la valeur actuelle (mot complet)
        switch (tache.getStatut()) {
            case A_faire: statusCombo.setValue("À faire"); break;
            case En_cours: statusCombo.setValue("En cours"); break;
            case Terminee: statusCombo.setValue("Terminée"); break;
        }
        
        statusCombo.getStyleClass().add("status-combo");
        statusCombo.setPrefWidth(120); // Largeur suffisante pour voir le texte complet
        statusCombo.setMinWidth(120);
        statusCombo.setMaxWidth(120);
        
        // ✅ Action lors du changement de statut
        statusCombo.setOnAction(e -> {
            String selected = statusCombo.getValue();
            Statut nouveauStatut;
            
            switch (selected) {
                case "En cours": nouveauStatut = Statut.En_cours; break;
                case "Terminée": nouveauStatut = Statut.Terminee; break;
                default: nouveauStatut = Statut.A_faire; break;
            }
            
            // Mettre à jour l'objet local
            tache.setStatut(nouveauStatut);
            
            // Mettre à jour dans la base
            serviceTache.modifierTache(
                tache.getId(),
                tache.getTitre(),
                tache.getDescription(),
                tache.getPriorite(),
                tache.getDate(),
                tache.getCategorie(),
                nouveauStatut
            );
            
            // Rafraîchir l'affichage
            refreshTasks();
            refreshEvaluations();
            updateStats();
            updateCalendarDisplay();
            
            // Mettre à jour le panneau de détails si nécessaire
            if (selectedTask != null && selectedTask.getId() == tache.getId()) {
                showTaskDetail(tache);
            }
            
            // Appliquer le style barré si terminé
            if (nouveauStatut == Statut.Terminee) {
                title.getStyleClass().add("task-done");
            } else {
                title.getStyleClass().remove("task-done");
            }
        });
        
        header.getChildren().addAll(titleBox, statusCombo);
        
        // Description
        Label desc = new Label(tache.getDescription());
        desc.getStyleClass().add("label");
        desc.setWrapText(true);
        desc.setMaxWidth(Double.MAX_VALUE);
        desc.setMaxHeight(80);
        
        // Labels pour priorité et autres infos
        HBox labels = new HBox(8);
        Label prioLabel = new Label(tache.getPriorite().toString());
        labels.getChildren().add(prioLabel);
        
        if (tache instanceof Evaluation) {
            Evaluation eval = (Evaluation) tache;
            Label matiereLabel = new Label("📖 " + eval.getMatiere());
            matiereLabel.setStyle("-fx-padding: 6 8; -fx-background-color: #f3e6f8; " +
                                "-fx-border-color: #d7bde2; -fx-border-radius: 10; " +
                                "-fx-background-radius: 10; -fx-font-size: 13px; -fx-text-fill: #9B59B6;");
            
            Label typeLabel = new Label("🎯 " + eval.getType());
            typeLabel.setStyle("-fx-padding: 6 8; -fx-background-color: #e8f6f3; " +
                             "-fx-border-color: #a2d9ce; -fx-border-radius: 10; " +
                             "-fx-background-radius: 10; -fx-font-size: 13px; -fx-text-fill: #16a085;");
            
            labels.getChildren().addAll(matiereLabel, typeLabel);
        }
        
        card.getChildren().addAll(header, desc, labels);
        card.setOnMouseClicked(e -> showTaskDetail(tache));
        
        card.setOnMouseEntered(e -> 
            card.setStyle(card.getStyle() + "-fx-translate-y: -4;"));
        card.setOnMouseExited(e -> 
            card.setStyle(card.getStyle().replace("-fx-translate-y: -4;", "")));
        
        return card;
    }
    
    private void refreshTasks() {
        taskGrid.getChildren().clear();
        
        List<Tache> tasks = filterAndSortTasks();
        
        for (Tache t : tasks) {
            if (!(t instanceof Evaluation)) {
                taskGrid.getChildren().add(createTaskCard(t));
            }
        }
        
        if (taskGrid.getChildren().isEmpty()) {
            Label noTasksLabel = new Label("Aucune tâche planifiée. Cliquez sur 'Nouvelle tâche' pour en ajouter une.");
            noTasksLabel.getStyleClass().add("small-text");
            noTasksLabel.setPadding(new Insets(20));
            taskGrid.getChildren().add(noTasksLabel);
        }
        updateStats();
    }
    
    private List<Tache> filterAndSortTasks() {
        List<Tache> tasks = serviceTache.getToutesTaches();
        String search = searchField.getText().toLowerCase();
        
        if (!selectedCategory.equals("ALL")) {
            Categorie cat = Categorie.valueOf(selectedCategory);
            tasks = tasks.stream()
                .filter(t -> t.getCategorie() == cat)
                .toList();
        }
        
        if (!search.isEmpty()) {
            tasks = tasks.stream()
                .filter(t -> t.getTitre().toLowerCase().contains(search) || 
                           t.getDescription().toLowerCase().contains(search))
                .toList();
        }
        
        String period = periodCombo.getValue();
        LocalDate now = LocalDate.now();
        if (period.equals("Aujourd'hui")) {
            tasks = tasks.stream().filter(t -> t.getDate().equals(now)).toList();
        } else if (period.equals("Cette semaine")) {
            LocalDate weekEnd = now.plusDays(7);
            tasks = tasks.stream()
                .filter(t -> !t.getDate().isBefore(now) && !t.getDate().isAfter(weekEnd))
                .toList();
        } else if (period.equals("Ce mois")) {
            tasks = tasks.stream()
                .filter(t -> t.getDate().getMonth() == now.getMonth() && 
                           t.getDate().getYear() == now.getYear())
                .toList();
        }
        
        String sort = sortCombo.getValue();
        if (sort.equals("Par priorité")) {
            return serviceTache.trierParPriorite().stream()
                .filter(tasks::contains).toList();
        } else if (sort.equals("Par statut")) {
            return tasks.stream()
                .sorted((a, b) -> a.getStatut().compareTo(b.getStatut()))
                .toList();
        } else {
            return serviceTache.trierParDate().stream()
                .filter(tasks::contains).toList();
        }
    }
    
    private void updateStats() {
        List<Tache> all = serviceTache.getToutesTaches();
        statTotal.setText(String.valueOf(all.size()));
        statDone.setText(String.valueOf(all.stream()
            .filter(t -> t.getStatut() == Statut.Terminee).count()));
        statProgress.setText(String.valueOf(all.stream()
            .filter(t -> t.getStatut() == Statut.En_cours).count()));
        statTodo.setText(String.valueOf(all.stream()
            .filter(t -> t.getStatut() == Statut.A_faire).count()));
    }
    
    private void showTaskDetail(Tache tache) {
        selectedTask = tache;
        detailPanel.getChildren().clear();
        
        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: white;");
        
        // Titre avec icône appropriée
        String icone = (tache instanceof Evaluation) ? "📚" : "📝";
        
        Label title = new Label(icone + " " + tache.getTitre());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #284E7B; -fx-padding: 0 0 8 0;");
        title.setWrapText(true);
        title.setMaxWidth(300);
        
        // Ligne 1: Date et Statut
        HBox ligne1 = new HBox(10);
        ligne1.setAlignment(Pos.CENTER_LEFT);
        
        Label dateLabel = new Label("📅 " + tache.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-weight: 600;");
        
        // Badge de statut
        HBox statutBadge = new HBox(5);
        statutBadge.setAlignment(Pos.CENTER);
        statutBadge.setStyle("-fx-background-color: " + getStatusColor(tache.getStatut()) + "; -fx-background-radius: 12; -fx-padding: 4 10 4 10;");
        
        Label statusLabel = new Label(formatStatut(tache.getStatut()));
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");
        
        statutBadge.getChildren().add(statusLabel);
        ligne1.getChildren().addAll(dateLabel, statutBadge);
        
        // Ligne 2: Priorité et Catégorie
        HBox ligne2 = new HBox(10);
        ligne2.setAlignment(Pos.CENTER_LEFT);
        ligne2.setStyle("-fx-flex-wrap: wrap;");
        
        // Badge de priorité
        HBox prioriteBadge = new HBox(5);
        prioriteBadge.setAlignment(Pos.CENTER);
        prioriteBadge.setStyle("-fx-background-color: " + getPriorityColor(tache.getPriorite()) + "; -fx-background-radius: 12; -fx-padding: 4 10 4 10;");
        
        Label prioriteLabel = new Label(tache.getPriorite().toString());
        prioriteLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");
        prioriteBadge.getChildren().add(prioriteLabel);
        
        ligne2.getChildren().add(prioriteBadge);
        
        // ✅ Afficher la catégorie uniquement pour les tâches normales
        if (!(tache instanceof Evaluation)) {
            HBox categorieBadge = new HBox(5);
            categorieBadge.setAlignment(Pos.CENTER);
            categorieBadge.setStyle("-fx-background-color: #7C9ACC; -fx-background-radius: 12; -fx-padding: 4 10 4 10;");
            
            Label catLabel = new Label(tache.getCategorie().toString());
            catLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");
            categorieBadge.getChildren().add(catLabel);
            
            ligne2.getChildren().add(categorieBadge);
        }
        
        // ✅ Info spécifique aux évaluations
        if (tache instanceof Evaluation) {
            Evaluation eval = (Evaluation) tache;
            
            // Badge matière
            HBox matiereBadge = new HBox(5);
            matiereBadge.setAlignment(Pos.CENTER);
            matiereBadge.setStyle("-fx-background-color: #9B59B6; -fx-background-radius: 12; -fx-padding: 4 10 4 10;");
            
            Label matiereLabel = new Label("📖 " + eval.getMatiere());
            matiereLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");
            matiereBadge.getChildren().add(matiereLabel);
            
            // Badge type d'évaluation
            HBox typeBadge = new HBox(5);
            typeBadge.setAlignment(Pos.CENTER);
            typeBadge.setStyle("-fx-background-color: #16A085; -fx-background-radius: 12; -fx-padding: 4 10 4 10;");
            
            String typeText = eval.getType() == TypeEvaluation.Ds ? "DS" : "Examen";
            Label typeLabel = new Label("🎯 " + typeText);
            typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-font-weight: bold;");
            typeBadge.getChildren().add(typeLabel);
            
            ligne2.getChildren().addAll(matiereBadge, typeBadge);
        }
        
        // Description
        VBox descBox = new VBox(5);
        Label descTitle = new Label("Description :");
        descTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #427AA1; -fx-padding: 10 0 5 0;");
        
        TextArea descArea = new TextArea(tache.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setMaxHeight(90);
        descArea.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 12px; -fx-text-fill: #555555;");
        descArea.setPrefWidth(300);
        
        descBox.getChildren().addAll(descTitle, descArea);
        
        // Boutons d'action
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(5, 0, 0, 0));
        
        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #7C9ACC; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-background-radius: 10; -fx-border-color: #427AA1; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        editBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(editBtn, Priority.ALWAYS);
        
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #DEABAF; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 16 8 16; -fx-background-radius: 10; -fx-border-color: #DC98BD; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(deleteBtn, Priority.ALWAYS);
        
        editBtn.setOnMouseEntered(e -> editBtn.setStyle(editBtn.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(124, 154, 204, 0.4), 6, 0, 0, 2);"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle(editBtn.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(124, 154, 204, 0.4), 6, 0, 0, 2);", "")));
        
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(222, 171, 175, 0.4), 6, 0, 0, 2);"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(deleteBtn.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(222, 171, 175, 0.4), 6, 0, 0, 2);", "")));
        
        editBtn.setOnAction(e -> {
            if (tache instanceof Evaluation) {
                showEvaluationDialog((Evaluation) tache);
            } else {
                showTaskDialog(tache);
            }
        });
        
        deleteBtn.setOnAction(e -> deleteTask(tache));
        
        actionButtons.getChildren().addAll(editBtn, deleteBtn);
        
        content.getChildren().addAll(
            title,
            ligne1,
            ligne2,
            descBox,
            actionButtons
        );
        
        detailPanel.getChildren().add(content);
    }
    private String getStatusColor(Statut statut) {
        switch (statut) {
            case A_faire:
                return "#E74C3C"; 
            case En_cours:
                return "#F39C12"; 
            case Terminee:
                return "#2ECC71";
            default:
                return "#95A5A6"; 
        }
    }

 private void showEvaluationDialog(Evaluation existingEvaluation) {
     Stage dialog = new Stage();
     dialog.setTitle(existingEvaluation == null ? "Nouvelle évaluation" : "Modifier évaluation");
     
     VBox layout = new VBox(16);
     layout.setPadding(new Insets(20));
     layout.getStyleClass().add("detail-card");
     
     GridPane form = new GridPane();
     form.setHgap(12);
     form.setVgap(12);
     
     TextField titleField = new TextField();
     titleField.setPromptText("Titre de l'évaluation");
     titleField.getStyleClass().add("text-field");
     
     TextArea descArea = new TextArea();
     descArea.setPromptText("Description");
     descArea.setPrefRowCount(3);
     descArea.getStyleClass().add("text-area");
     
     TextField matiereField = new TextField();
     matiereField.setPromptText("Matière");
     matiereField.getStyleClass().add("text-field");
     
     ComboBox<TypeEvaluation> typeCombo = new ComboBox<>();
     typeCombo.getItems().addAll(TypeEvaluation.values());
     typeCombo.setValue(TypeEvaluation.Ds);
     typeCombo.getStyleClass().add("combo-box");
     
     DatePicker datePicker = new DatePicker();
     datePicker.setValue(LocalDate.now().plusDays(7));
     datePicker.getStyleClass().add("date-picker");
     
     Label prioLabel = new Label("");
     prioLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E74C3C; -fx-padding: 8 0 8 0;");
     
     Label statusLabel = new Label("");
     statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E74C3C; -fx-padding: 8 0 8 0;");
     
     Label catLabel = new Label("");
     catLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #E74C3C; -fx-padding: 8 0 8 0;");
     
     if (existingEvaluation != null) {
         titleField.setText(existingEvaluation.getTitre());
         descArea.setText(existingEvaluation.getDescription());
         matiereField.setText(existingEvaluation.getMatiere());
         typeCombo.setValue(existingEvaluation.getType());
         datePicker.setValue(existingEvaluation.getDate());
         if (existingEvaluation.getPriorite() != Priorite.Haute) {
             existingEvaluation.setPriorite(Priorite.Haute);
         }
         if (existingEvaluation.getStatut() != Statut.A_faire) {
             existingEvaluation.setStatut(Statut.A_faire);
         }
         if (existingEvaluation.getCategorie() != Categorie.Revision) {
             existingEvaluation.setCategorie(Categorie.Revision);
         }
     }
     
     Label titleLbl = new Label("Titre *");
     titleLbl.getStyleClass().add("form-label");
     Label descLbl = new Label("Description");
     descLbl.getStyleClass().add("form-label");
     Label matiereLbl = new Label("Matière *");
     matiereLbl.getStyleClass().add("form-label");
     Label typeLbl = new Label("Type d'évaluation *");
     typeLbl.getStyleClass().add("form-label");
     Label dateLbl = new Label("Date *");
     dateLbl.getStyleClass().add("form-label");
     Label catLbl = new Label("");
     catLbl.getStyleClass().add("form-label");
     Label prioLbl = new Label("");
     prioLbl.getStyleClass().add("form-label");
     Label statusLbl = new Label("");
     statusLbl.getStyleClass().add("form-label");
     
     form.add(titleLbl, 0, 0);
     form.add(titleField, 1, 0);
     form.add(descLbl, 0, 1);
     form.add(descArea, 1, 1);
     form.add(matiereLbl, 0, 2);
     form.add(matiereField, 1, 2);
     form.add(typeLbl, 0, 3);
     form.add(typeCombo, 1, 3);
     form.add(dateLbl, 0, 4);
     form.add(datePicker, 1, 4);
     form.add(catLbl, 0, 5);
     form.add(catLabel, 1, 5);
     form.add(prioLbl, 0, 6);
     form.add(prioLabel, 1, 6);
     form.add(statusLbl, 0, 7);
     form.add(statusLabel, 1, 7);
     
     titleField.setPrefWidth(300);
     descArea.setPrefWidth(300);
     matiereField.setPrefWidth(300);
     typeCombo.setPrefWidth(300);
     
     HBox buttons = new HBox(8);
     buttons.setAlignment(Pos.CENTER_RIGHT);
     
     Button saveBtn = new Button("💾 Enregistrer");
     saveBtn.getStyleClass().add("btn-strong");
     
     Button cancelBtn = new Button("Annuler");
     cancelBtn.getStyleClass().add("btn-secondary");
     
     saveBtn.setOnAction(e -> {
         if (titleField.getText().isEmpty() || datePicker.getValue() == null || 
             matiereField.getText().isEmpty()) {
             showAlert("Erreur", "Veuillez remplir les champs obligatoires (*)");
             return;
         }
         
         if (existingEvaluation == null) {

             serviceTache.ajouterEvaluation(
                 titleField.getText(),
                 descArea.getText(),
                 Priorite.Haute,
                 datePicker.getValue(),
                 Categorie.Revision,           
                 Statut.A_faire,
                 matiereField.getText(),
                 typeCombo.getValue()
             );
             showAlert("Succès", "✅ Évaluation créée avec succès !");
         } else {
             serviceTache.modifierEvaluation(
                 existingEvaluation.getId(),
                 titleField.getText(),
                 descArea.getText(),
                 Priorite.Haute,
                 datePicker.getValue(),
                 Categorie.Revision,           
                 Statut.A_faire, 
                 matiereField.getText(),
                 typeCombo.getValue()
             );
             showAlert("Succès", "✅ Évaluation modifiée avec succès !");
         }
         
         refreshTasks();
         refreshEvaluations();
         updateCalendarDisplay();
         dialog.close();
     });
     
     cancelBtn.setOnAction(e -> dialog.close());
     
     buttons.getChildren().addAll(cancelBtn, saveBtn);
     
     layout.getChildren().addAll(form, buttons);
     
     Scene scene = new Scene(layout);
     scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
     dialog.setScene(scene);
     dialog.show();
 }

 private void showTaskDialog(Tache existingTask) {
     Stage dialog = new Stage();
     dialog.setTitle(existingTask == null ? "Nouvelle tâche" : "Modifier tâche");
     
     ScrollPane scrollPane = new ScrollPane();
     scrollPane.setFitToWidth(true);
     scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
     scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
     scrollPane.getStyleClass().add("scroll-pane");
     
     VBox mainLayout = new VBox(16);
     mainLayout.setPadding(new Insets(20));
     mainLayout.getStyleClass().add("detail-card");
     
     VBox typeSelection = new VBox(8);
     ToggleGroup taskTypeGroup = new ToggleGroup();
     
     Label typeLabel = new Label("Type de tâche:");
     typeLabel.getStyleClass().add("form-label");
     
     RadioButton simpleTaskBtn = new RadioButton("Tâche Simple");
     simpleTaskBtn.setToggleGroup(taskTypeGroup);
     simpleTaskBtn.setSelected(true);
     simpleTaskBtn.setUserData("simple");
     simpleTaskBtn.getStyleClass().add("check-box");
     
     RadioButton recurrentTaskBtn = new RadioButton("Tâche Récurrente");
     recurrentTaskBtn.setToggleGroup(taskTypeGroup);
     recurrentTaskBtn.setUserData("recurrent");
     recurrentTaskBtn.getStyleClass().add("check-box");
     
     typeSelection.getChildren().addAll(typeLabel, simpleTaskBtn, recurrentTaskBtn);
     
     GridPane commonForm = new GridPane();
     commonForm.setHgap(12);
     commonForm.setVgap(12);
     
     TextField titleField = new TextField();
     titleField.setPromptText("Titre de la tâche");
     titleField.getStyleClass().add("text-field");
     
     TextArea descArea = new TextArea();
     descArea.setPromptText("Description");
     descArea.setPrefRowCount(3);
     descArea.getStyleClass().add("text-area");
     
     DatePicker datePicker = new DatePicker();
     datePicker.setValue(LocalDate.now());
     datePicker.getStyleClass().add("date-picker");
     
     ComboBox<Categorie> catCombo = new ComboBox<>();
     catCombo.getItems().addAll(Categorie.values());
     catCombo.setValue(Categorie.Revision);
     catCombo.getStyleClass().add("combo-box");
     
     ComboBox<Priorite> prioCombo = new ComboBox<>();
     prioCombo.getItems().addAll(Priorite.values());
     prioCombo.setValue(Priorite.Faible);
     prioCombo.getStyleClass().add("combo-box");
     
     ComboBox<Statut> statusCombo = new ComboBox<>();
     statusCombo.getItems().addAll(Statut.values());
     statusCombo.setValue(Statut.A_faire);
     statusCombo.getStyleClass().add("combo-box");
     
     VBox recurrentFields = new VBox(8);
     recurrentFields.setVisible(false);
     recurrentFields.setManaged(false);
     
     Label recurrentInfo = new Label("Configuration de la récurrence:");
     recurrentInfo.getStyleClass().add("form-label");
     
     ComboBox<TypeReccurrence> recurrenceTypeCombo = new ComboBox<>();
     recurrenceTypeCombo.getItems().addAll(TypeReccurrence.values());
     recurrenceTypeCombo.setValue(TypeReccurrence.Journaliere);
     recurrenceTypeCombo.getStyleClass().add("combo-box");
     
     Spinner<Integer> intervalSpinner = new Spinner<>(1, 365, 1);
     intervalSpinner.setEditable(true);
     intervalSpinner.getStyleClass().add("spinner");
     
     DatePicker endDatePicker = new DatePicker();
     endDatePicker.setValue(LocalDate.now().plusMonths(1));
     endDatePicker.getStyleClass().add("date-picker");
     
     Spinner<Integer> occurrencesSpinner = new Spinner<>(1, 100, 5);
     occurrencesSpinner.setEditable(true);
     occurrencesSpinner.getStyleClass().add("spinner");
     
     Label intervalLabel = new Label("Intervalle:");
     intervalLabel.getStyleClass().add("form-label");
     Label endDateLabel = new Label("Date de fin:");
     endDateLabel.getStyleClass().add("form-label");
     Label occurrencesLabel = new Label("Nombre d'occurrences:");
     occurrencesLabel.getStyleClass().add("form-label");
     
     Label recTypeLabel = new Label("Type de récurrence:");
     recTypeLabel.getStyleClass().add("form-label");
     
     recurrentFields.getChildren().addAll(
         recurrentInfo,
         recTypeLabel,
         recurrenceTypeCombo,
         intervalLabel,
         intervalSpinner,
         endDateLabel,
         endDatePicker,
         occurrencesLabel,
         occurrencesSpinner
     );
     
     taskTypeGroup.selectedToggleProperty().addListener((obs, old, newToggle) -> {
         if (newToggle != null) {
             String type = (String) newToggle.getUserData();
             boolean isRecurrent = type.equals("recurrent");
             recurrentFields.setVisible(isRecurrent);
             recurrentFields.setManaged(isRecurrent);
             dialog.sizeToScene();
         }
     });
     
     if (existingTask != null) {
         typeSelection.setVisible(false);
         typeSelection.setManaged(false);
         
         titleField.setText(existingTask.getTitre());
         descArea.setText(existingTask.getDescription());
         datePicker.setValue(existingTask.getDate());
         catCombo.setValue(existingTask.getCategorie());
         prioCombo.setValue(existingTask.getPriorite());
         statusCombo.setValue(existingTask.getStatut());
         
         if (existingTask instanceof TacheReccurrente) {
             TacheReccurrente tr = (TacheReccurrente) existingTask;
             recurrentTaskBtn.setSelected(true);
             recurrenceTypeCombo.setValue(tr.getTypeReccurrence());
             intervalSpinner.getValueFactory().setValue(tr.getInterval());
             endDatePicker.setValue(tr.getDateFin());
             occurrencesSpinner.getValueFactory().setValue(tr.getOccurrencesMax());
             
             recurrentFields.setVisible(true);
             recurrentFields.setManaged(true);
         }
     }
     
     Label titleLbl = new Label("Titre *");
     titleLbl.getStyleClass().add("form-label");
     Label descLbl = new Label("Description");
     descLbl.getStyleClass().add("form-label");
     Label dateLbl = new Label("Date *");
     dateLbl.getStyleClass().add("form-label");
     Label catLbl = new Label("Catégorie");
     catLbl.getStyleClass().add("form-label");
     Label prioLbl = new Label("Priorité");
     prioLbl.getStyleClass().add("form-label");
     Label statusLbl = new Label("Statut");
     statusLbl.getStyleClass().add("form-label");
     
     commonForm.add(titleLbl, 0, 0);
     commonForm.add(titleField, 1, 0);
     commonForm.add(descLbl, 0, 1);
     commonForm.add(descArea, 1, 1);
     commonForm.add(dateLbl, 0, 2);
     commonForm.add(datePicker, 1, 2);
     commonForm.add(catLbl, 0, 3);
     commonForm.add(catCombo, 1, 3);
     commonForm.add(prioLbl, 0, 4);
     commonForm.add(prioCombo, 1, 4);
     commonForm.add(statusLbl, 0, 5);
     commonForm.add(statusCombo, 1, 5);
     
     titleField.setPrefWidth(300);
     descArea.setPrefWidth(300);
     recurrenceTypeCombo.setPrefWidth(300);
     intervalSpinner.setPrefWidth(300);
     endDatePicker.setPrefWidth(300);
     occurrencesSpinner.setPrefWidth(300);
     
     HBox buttons = new HBox(8);
     buttons.setAlignment(Pos.CENTER_RIGHT);
     
     Button saveBtn = new Button("💾 Enregistrer");
     saveBtn.getStyleClass().add("btn-strong");
     
     Button cancelBtn = new Button("Annuler");
     cancelBtn.getStyleClass().add("btn-secondary");
     
     saveBtn.setOnAction(e -> {
         if (titleField.getText().isEmpty() || datePicker.getValue() == null) {
             showAlert("Erreur", "Veuillez remplir les champs obligatoires");
             return;
         }
         
         if (existingTask == null) {
             String taskType = (String) taskTypeGroup.getSelectedToggle().getUserData();
             
             if (taskType.equals("recurrent")) {
                 serviceTache.ajouterTacheReccurente(
                     titleField.getText(),
                     descArea.getText(),
                     prioCombo.getValue(),
                     datePicker.getValue(),
                     catCombo.getValue(),
                     statusCombo.getValue(),
                     recurrenceTypeCombo.getValue(),
                     intervalSpinner.getValue(),
                     endDatePicker.getValue(),
                     occurrencesSpinner.getValue(),
                     datePicker.getValue()
                 );
                 
                 TacheReccurrente nouvelleTache = new TacheReccurrente(
                     0, titleField.getText(), descArea.getText(),
                     prioCombo.getValue(), datePicker.getValue(),
                     catCombo.getValue(), statusCombo.getValue(),
                     recurrenceTypeCombo.getValue(), intervalSpinner.getValue(),
                     endDatePicker.getValue(), occurrencesSpinner.getValue(),
                     datePicker.getValue()
                 );
                 
                 List<LocalDate> dates = serviceTache.genererDatesOccurrences(nouvelleTache);
                 String message = "✅ Tâche récurrente créée avec " + dates.size() + " occurrence(s)";
                 showAlert("Succès", message);
             } else {
                 serviceTache.ajouterTacheSimple(
                     titleField.getText(),
                     descArea.getText(),
                     prioCombo.getValue(),
                     datePicker.getValue(),
                     catCombo.getValue(),
                     statusCombo.getValue()
                 );
                 showAlert("Succès", "✅ Tâche simple créée avec succès !");
             }
         } else {
             if (existingTask instanceof TacheReccurrente) {
                 serviceTache.modifierTacheReccurrente(
                     existingTask.getId(),
                     titleField.getText(),
                     descArea.getText(),
                     prioCombo.getValue(),
                     datePicker.getValue(),
                     catCombo.getValue(),
                     statusCombo.getValue(),
                     recurrenceTypeCombo.getValue(),
                     intervalSpinner.getValue(),
                     endDatePicker.getValue(),
                     occurrencesSpinner.getValue()
                 );
             } else {
                 serviceTache.modifierTache(
                     existingTask.getId(),
                     titleField.getText(),
                     descArea.getText(),
                     prioCombo.getValue(),
                     datePicker.getValue(),
                     catCombo.getValue(),
                     statusCombo.getValue()
                 );
             }
             showAlert("Succès", "✅ Tâche modifiée avec succès !");
         }
         
         refreshTasks();
         updateCalendarDisplay();
         dialog.close();
     });
     
     cancelBtn.setOnAction(e -> dialog.close());
     
     buttons.getChildren().addAll(cancelBtn, saveBtn);
     
     if (existingTask == null) {
         mainLayout.getChildren().addAll(typeSelection, commonForm, recurrentFields, buttons);
     } else {
         mainLayout.getChildren().addAll(commonForm, recurrentFields, buttons);
     }
     
     scrollPane.setContent(mainLayout);
     
     Scene scene = new Scene(scrollPane, 450, 450);
     scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
     dialog.setScene(scene);
     dialog.setMinWidth(450);
     dialog.setMinHeight(300);
     dialog.show();
 }

 private void deleteTask(Tache tache) {
     Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
     alert.setTitle("Confirmation");
     alert.setHeaderText("Supprimer la tâche");
     alert.setContentText("Voulez-vous vraiment supprimer cette tâche ?");
     
     alert.showAndWait().ifPresent(response -> {
         if (response == ButtonType.OK) {
             serviceTache.supprimerTache(tache.getId());
             refreshTasks();
             refreshEvaluations(); 
             updateCalendarDisplay();
             
             detailPanel.getChildren().clear();
             Label noSelection = new Label("Aucune tâche sélectionnée\n\nCliquez sur une tâche pour voir ses détails");
             noSelection.getStyleClass().add("small-text");
             noSelection.setAlignment(Pos.CENTER);
             noSelection.setWrapText(true);
             noSelection.setPadding(new Insets(30, 20, 30, 20));
             noSelection.setTextAlignment(TextAlignment.CENTER);
             detailPanel.getChildren().add(noSelection);
         }
     });
 }
    
    
    private String getPriorityColor(Priorite priorite) {
        return switch (priorite) {
            case Urgente -> "#E74C3C";
            case Haute -> "#FF3B30";
            case Moyenne -> "#FF9500";
            case Faible -> "#34C759";
            default -> "#95A5A6";
        };
    }
    
    private String formatStatut(Statut statut) {
        return switch (statut) {
            case A_faire -> "À faire";
            case En_cours -> "En cours";
            case Terminee -> "Terminée";
            default -> "Inconnu";
        };
    }
    
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void refreshEvaluations() {
        evaluationGrid.getChildren().clear();
        
        List<Tache> allTasks = serviceTache.getToutesTaches();
        List<Tache> evaluations = allTasks.stream()
            .filter(t -> t instanceof Evaluation)
            .collect(Collectors.toList());
        
        // Corriger les évaluations existantes si nécessaire
        for (Tache task : evaluations) {
            if (task instanceof Evaluation) {
                Evaluation eval = (Evaluation) task;
                boolean needsUpdate = false;
                
                // Vérifier et corriger la priorité
                if (eval.getPriorite() != Priorite.Haute) {
                    eval.setPriorite(Priorite.Haute);
                    needsUpdate = true;
                }
                
                // Vérifier et corriger le statut
                if (eval.getStatut() != Statut.A_faire) {
                    eval.setStatut(Statut.A_faire);
                    needsUpdate = true;
                }
                
                // Vérifier et corriger la catégorie
                if (eval.getCategorie() != Categorie.Revision) {
                    eval.setCategorie(Categorie.Revision);
                    needsUpdate = true;
                }
                
                // Mettre à jour en base si nécessaire
                if (needsUpdate) {
                    serviceTache.modifierEvaluation(
                        eval.getId(),
                        eval.getTitre(),
                        eval.getDescription(),
                        eval.getPriorite(),
                        eval.getDate(),
                        eval.getCategorie(),
                        eval.getStatut(),
                        eval.getMatiere(),
                        eval.getType()
                    );
                }
            }
        }
        
        // Recharger les évaluations après correction
        evaluations = allTasks.stream()
            .filter(t -> t instanceof Evaluation)
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .collect(Collectors.toList());
        
        for (Tache eval : evaluations) {
            evaluationGrid.getChildren().add(createEvaluationCard((Evaluation) eval));
        }
        
        if (evaluations.isEmpty()) {
            Label noEvalLabel = new Label("Aucune évaluation planifiée. Cliquez sur 'Nouvelle évaluation' pour en ajouter une.");
            noEvalLabel.getStyleClass().add("small-text");
            noEvalLabel.setPadding(new Insets(20));
            evaluationGrid.getChildren().add(noEvalLabel);
        }
    }
    
    private VBox createEvaluationCard(Evaluation evaluation) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("detail-card");
        card.getStyleClass().add("evaluation-card");
        card.setStyle(card.getStyle() + "-fx-border-width: 0 0 0 6; " +
                "-fx-border-color: " + getPriorityColor(Priorite.Haute) + "; " + // Toujours rouge pour Haute
                "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        VBox titleBox = new VBox(4);
        
        Label title = new Label("📚 " + evaluation.getTitre());
        title.getStyleClass().add("form-label");
        title.setStyle("-fx-font-size: 15px;");
        
        Label meta = new Label(evaluation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                              " · 📖 " + evaluation.getMatiere() + " · 🎯 " + evaluation.getType());
        meta.getStyleClass().add("small-text");
        
        titleBox.getChildren().addAll(title, meta);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        

        header.getChildren().addAll(titleBox);
        
        Label desc = new Label(evaluation.getDescription());
        desc.getStyleClass().add("label");
        desc.setWrapText(true);
        desc.setMaxWidth(Double.MAX_VALUE);
        
        HBox labels = new HBox(8);
        
        Label prioLabel = new Label(evaluation.getPriorite().toString());
 
        
        Label matiereLabel = new Label("📖 " + evaluation.getMatiere());
        matiereLabel.setStyle("-fx-padding: 6 8; -fx-background-color: #f3e6f8; " +
                            "-fx-border-color: #d7bde2; -fx-border-radius: 10; " +
                            "-fx-background-radius: 10; -fx-font-size: 13px; -fx-text-fill: #9B59B6;");
        
        String typeText = evaluation.getType() == TypeEvaluation.Ds ? "DS" : "Examen";
        Label typeLabel = new Label("🎯 " + typeText);
        typeLabel.setStyle("-fx-padding: 6 8; -fx-background-color: #e8f6f3; " +
                         "-fx-border-color: #a2d9ce; -fx-border-radius: 10; " +
                         "-fx-background-radius: 10; -fx-font-size: 13px; -fx-text-fill: #16a085;");
        
        labels.getChildren().addAll(prioLabel, matiereLabel, typeLabel);
        
        card.getChildren().addAll(header, desc, labels);
        card.setOnMouseClicked(e -> showTaskDetail(evaluation));
        
        card.setOnMouseEntered(e -> 
            card.setStyle(card.getStyle() + "-fx-translate-y: -4;"));
        card.setOnMouseExited(e -> 
            card.setStyle(card.getStyle().replace("-fx-translate-y: -4;", "")));
        
        return card;
    }
    
    public static void main(String[] args) {
        launch(args); //est une méthode statique de la classe :javafx.application.Application
    }
}