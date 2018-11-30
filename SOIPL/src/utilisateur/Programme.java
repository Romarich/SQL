package utilisateur;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import source.BCrypt;

public class Programme {
	public Connection connection;
	private int utilisateur;
	private static Scanner scanner = new Scanner(System.in);
	private PreparedStatement psSelectionUtilisateurDejaPresent;
	private PreparedStatement psInscriptionNouvelUtil;
	private PreparedStatement psSelectionDeLUtilisateurEnCours;
	private PreparedStatement psIntroductionNouvelleQuestion;
	private PreparedStatement psVisualiserQuestionsPosees;
	private PreparedStatement psVisualiserQuestionsPoseesSpecifiqueId;
	private PreparedStatement psVisualiserToutesLesQuestions;
	private PreparedStatement psVisualiserReponses;
	private PreparedStatement psVisualiserInformationsUtilisateur;
	private PreparedStatement psIntroductionNouvelleReponse;
	
	public Programme(){
		this.connection = connexionDB();	
		try {
			this.psSelectionUtilisateurDejaPresent = connection.prepareStatement("SELECT id_utilisateur, mot_de_passe FROM SOIPL.utilisateurs WHERE nom_utilisateur= ?");
			this.psInscriptionNouvelUtil = connection.prepareStatement("SELECT SOIPL.inscription_utilisateur(?, ?, ?)");
			this.psSelectionDeLUtilisateurEnCours = connection.prepareStatement("SELECT SOIPL.selection_id_utilisateur_avec_nom_utilisateur(?)");
			this.psIntroductionNouvelleQuestion = connection.prepareStatement("SELECT SOIPL.creation_nouvelle_question(?,?,?);" );
			this.psVisualiserQuestionsPosees = connection.prepareStatement("SELECT * FROM SOIPL.questions WHERE utilisateur_createur = ?");
			this.psVisualiserQuestionsPoseesSpecifiqueId = connection.prepareStatement("SELECT q.*, u.nom_utilisateur FROM SOIPL.questions q, SOIPL.utilisateurs u WHERE q.id_question = ? AND u.id_utilisateur = q.utilisateur_createur");
			this.psVisualiserToutesLesQuestions = connection.prepareStatement("SELECT * FROM SOIPL.questions");
			this.psVisualiserReponses = connection.prepareStatement("SELECT r.*, u.nom_utilisateur FROM SOIPL.reponses r, SOIPL.utilisateurs u WHERE r.id_question = ? AND u.id_utilisateur = r.id_utilisateur ORDER BY r.score DESC");
			this.psVisualiserInformationsUtilisateur = connection.prepareStatement("SELECT * FROM SOIPL.utilisateurs WHERE id_utilisateur  = ?");
			this.psIntroductionNouvelleReponse = connection.prepareStatement("SELECT SOIPL.creation_reponse(?,?,?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void menuAvecChoix() {
		//scanner.reset();
		System.out.println("----------------------------------------------");
		System.out.println("|           Que voulez-vous faire ?          |");
		System.out.println("----------------------------------------------");
		System.out.println("|1. Introduire une nouvelle question.        |");
		System.out.println("|2. Visualiser les questions posées.         |");
		System.out.println("|3. Visualiser les questions repondues.      |");
		System.out.println("|4. Visualiser toutes les questions.         |");
		System.out.println("|5. Visualiser les questions d'un tag.       |");
		System.out.println("|6. Eteindre le programme.                   |");
		System.out.println("----------------------------------------------");
		int choix = 0;
		
		do {
			System.out.print("Veuillez rentrer votre choix : ");
			choix = Integer.parseInt(scanner.nextLine());
		}while(!(choix > 0 && choix < 7));
		
		switch(choix) {
			case 1:
				introduireNouvelleQuestion();
				break;
			case 2:
				visualiserQuestionsPosees();
				break;
			case 3:
				visualiserQuestionsRepondues();
				break;
			case 4:
				toutesLesQuestions();
				break;
			case 5:
				visualiserQuestionsAvecTag(); 
				break;
			case 6:
				fermerLeProgramme();
				break;
		}
	}
	
	public Connection connexionDB() {
		ConnexionDB aRenvoyer = new ConnexionDB();
		return aRenvoyer.getConnection();
	}
	
	public void connexionUtilisateur() {
		System.out.println("Etes-vous deja inscrit ? (O/N)");
		String caract = scanner.nextLine();
		if("O".equals(caract)) {
			System.out.print("Veuillez entrer votre identifiant : ");
			String login = scanner.nextLine();
			System.out.print("Veuillez entrer votre mot de passe : ");
			String password = scanner.nextLine();
			try {
				psSelectionUtilisateurDejaPresent.setString(1, login);
	            ResultSet rs = psSelectionUtilisateurDejaPresent.executeQuery();
	            boolean ok = false;
	            while (rs.next()) {
	                utilisateur = rs.getInt(1);
	                ok = BCrypt.checkpw(password, rs.getString(2));
	            }
	            if(!ok) {
	            	System.out.println("Mot de passe incorrect");
	            	connexionUtilisateur();
	            }
	            rs.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}else if("N".equals(caract)){
			System.out.print("Veuillez entrer votre email : ");
			String email = scanner.nextLine();
			System.out.print("Veuillez entrer votre identifiant : ");
			String login = scanner.nextLine();
			System.out.print("Veuillez entrer votre mot de passe : ");
			String password = scanner.nextLine();
			password = BCrypt.hashpw(password, BCrypt.gensalt());
			try {
				psInscriptionNouvelUtil.setString(1,email);
				psInscriptionNouvelUtil.setString(2,login);
				psInscriptionNouvelUtil.setString(3,password);
				psInscriptionNouvelUtil.executeQuery();
			}catch (SQLException se) {
				//TODO (à completer)
				System.out.println("Erreur lors de l’insertion ! Essayer une autre adresse mail, ou un autre nom d'utilisateur");
				connexionUtilisateur();
			}
			try {				 
				psSelectionDeLUtilisateurEnCours.setString(1, login);
		        ResultSet rs = psSelectionDeLUtilisateurEnCours.executeQuery();
		        rs.next();
		        utilisateur = rs.getInt(1);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}else {
			connexionUtilisateur();
		}
	}
	
	public void introduireNouvelleQuestion() {
		System.out.println("Introduisez le titre de votre nouvelle question");
		String titre="";
		while("".equals(titre)) {
			titre = scanner.nextLine();
		}
		System.out.println("Introduisez votre nouvelle question");
		String corpQuestion="";
		while("".equals(corpQuestion)) {
			corpQuestion = scanner.nextLine();
		}
		try {
			psIntroductionNouvelleQuestion.setInt(1,utilisateur);
			psIntroductionNouvelleQuestion.setString(2,corpQuestion);
			psIntroductionNouvelleQuestion.setString(3,titre);
			psIntroductionNouvelleQuestion.executeQuery();	
		}catch (SQLException se) {
			System.out.println("Erreur lors de l’insertion !");
			se.printStackTrace();
			System.exit(1); 
		}
		menuAvecChoix();
	}
	
	public void visualiserQuestionsPosees() {
		try {
			
			psVisualiserQuestionsPosees.setInt(1, utilisateur);
			ResultSet rs = psVisualiserQuestionsPosees.executeQuery();
			int i = 0;
			while(rs.next()){
				i++;
				System.out.println(i + " " + rs.getString(0));
			}
		}catch(SQLException se) {
			System.out.println("Vous n'avez pas encore posé de questions");
			menuAvecChoix();
		}
		System.out.println("Quel question souhaitez voir en detail ?");
		int choixVisualisationQuestionSpecifique= Integer.parseInt(scanner.nextLine());
		try {
			psVisualiserQuestionsPoseesSpecifiqueId.setInt(1, choixVisualisationQuestionSpecifique);
			ResultSet rs = psVisualiserQuestionsPoseesSpecifiqueId.executeQuery();
			int i = 0;
			while(rs.next()){
				i++;
				System.out.println(i + " " + rs.getString(0));
			}
		}catch(SQLException se) {
			
		}
		menuAvecChoix();
	}
	
	public void toutesLesQuestions() {
		System.out.println("Affichage de toutes les questions");
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
            ResultSet rs = psVisualiserToutesLesQuestions.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString(1) + ". " + rs.getString(7));
                ids.add(Integer.parseInt(rs.getString(1)));
            }
            rs.close();
        } catch (Exception e) {
        	System.out.println("Vous n'avez pas encore posé de questions");
			menuAvecChoix();
        }
		System.out.println("");
		//TODO soucis au niveau de l'affichage voir pk
		int choixVisualisationQuestionSpecifique;
		do {
			System.out.println("Quel question souhaitez voir en detail ?");
			choixVisualisationQuestionSpecifique = Integer.parseInt(scanner.nextLine());
		}while(!ids.contains(choixVisualisationQuestionSpecifique));
		try {
			psVisualiserQuestionsPoseesSpecifiqueId.setInt(1, choixVisualisationQuestionSpecifique);
			ResultSet rs1 = psVisualiserQuestionsPoseesSpecifiqueId.executeQuery();
			while (rs1.next()) {
				System.out.println("----------------------------------------------");
            	System.out.println(rs1.getString(9));
            	System.out.println(rs1.getString(1) + ", " + rs1.getString(7));
                System.out.println("");
                System.out.println(rs1.getString(6));
                System.out.println("");
                System.out.print(rs1.getString(3));
                if(rs1.getString(4) != null) {
                	System.out.println(", dernière edition :" + rs1.getString(4) + " par " + rs1.getString(5));
                }else {
                	System.out.println();
                }
        		System.out.println("----------------------------------------------");
                System.out.println("");
            }
		}catch(SQLException se) {
			
		}
		
		try {
			psVisualiserReponses.setInt(1, choixVisualisationQuestionSpecifique);
			ResultSet rs2 = psVisualiserReponses.executeQuery();
			while (rs2.next()) {
				System.out.println("----------------------------------------------");
            	System.out.println(rs2.getString(8) + " " + rs2.getString(1) + " " + rs2.getString(7) );
                System.out.println("");
                System.out.println(rs2.getString(5) + " : " + rs2.getString(6));
                System.out.println("");
                System.out.println(rs2.getString(3));
        		System.out.println("----------------------------------------------");
                System.out.println("");
                System.out.println("");
			}
		}catch(SQLException se) {
		
		}
		
		String statut = "";
		
		try {
			psVisualiserInformationsUtilisateur.setInt(1, this.utilisateur);
			ResultSet rs3 = psVisualiserInformationsUtilisateur.executeQuery();
			while (rs3.next()) {
				statut = rs3.getString(5);
			}
		}catch(SQLException se) {
		
		}
		switch(statut) {
			case "avancé":
				System.out.println("Entrez votre réponse, ou tapez P pour voter positivement pour une réponse");
			break;
			
			case "master":
				System.out.println("Entrez votre réponse, ou tapez P pour voter positivement pour une réponse, N pour voter négativement pour une réponse");
			break;
			
			default : 
				System.out.println("Entrez votre réponse :");
				//scanner.reset();
				String reponse = scanner.nextLine();
						
				try {
					psIntroductionNouvelleReponse.setInt(1, choixVisualisationQuestionSpecifique);
					psIntroductionNouvelleReponse.setString(2, reponse);
					psIntroductionNouvelleReponse.setInt(3, this.utilisateur);
					psIntroductionNouvelleReponse.executeQuery();
					System.out.println("");
					System.out.println("Merci !");
					System.out.println("");
				}catch(SQLException se) {
					se.printStackTrace();
					System.out.println("erreur");
				}
				
			break;
			
			
		}
		
		menuAvecChoix();
	}
	
	public void visualiserQuestionsRepondues() {
		System.out.println("Affichage de toutes les questions répondues");
		menuAvecChoix();
	}
	
	public void visualiserQuestionsAvecTag() {
		System.out.println("Affichage de toutes les questions avec tag specifique");
		menuAvecChoix();
	}
	
	public void fermerLeProgramme(){
		try {
			psSelectionUtilisateurDejaPresent.close();
			psInscriptionNouvelUtil.close();
			psSelectionDeLUtilisateurEnCours.close();
			psIntroductionNouvelleQuestion.close();
			psVisualiserQuestionsPosees.close();
			psVisualiserQuestionsPoseesSpecifiqueId.close();
			psVisualiserToutesLesQuestions.close();
			psVisualiserReponses.close();
			scanner.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Au revoir et à bientôt");
		System.exit(0);
	}
}


