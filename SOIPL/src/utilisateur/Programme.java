package utilisateur;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	
	public Programme(){
		this.connection = connexionDB();	
		try {
			this.psSelectionUtilisateurDejaPresent = connection.prepareStatement("SELECT id_utilisateur, mot_de_passe FROM SOIPL.utilisateurs WHERE nom_utilisateur= ?");
			this.psInscriptionNouvelUtil = connection.prepareStatement("SELECT inscription_utilisateur(?, ?, ?)");
			this.psSelectionDeLUtilisateurEnCours = connection.prepareStatement("SELECT selection_id_utilisateur_avec_nom_utilisateur(?)");
			this.psIntroductionNouvelleQuestion = connection.prepareStatement("SELECT creation_nouvelle_question(?,?,?);" );
			this.psVisualiserQuestionsPosees = connection.prepareStatement("SELECT * FROM SOIPL.questions WHERE utilisateur_createur = ?");
			this.psVisualiserQuestionsPoseesSpecifiqueId = connection.prepareStatement("SELECT * FROM SOIPL.reponses WHERE id_question = ?");
			this.psVisualiserToutesLesQuestions = connection.prepareStatement("SELECT * FROM SOIPL.questions");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void menuAvecChoix() {
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
			choix = scanner.nextInt();
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
		int choixVisualisationQuestionSpecifique= scanner.nextInt();
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
		try {
            ResultSet rs = psVisualiserToutesLesQuestions.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString(1) + ". " + rs.getString(7));
            }
            rs.close();
        } catch (Exception e) {
        	System.out.println("Vous n'avez pas encore posé de questions");
			menuAvecChoix();
        }
		//TODO soucis au niveau de l'affichage voir pk
		System.out.println("Quel question souhaitez voir en detail ?");
		int choixVisualisationQuestionSpecifique= scanner.nextInt();
		try {
			psVisualiserQuestionsPoseesSpecifiqueId.setInt(1, choixVisualisationQuestionSpecifique);
			ResultSet rs1 = psVisualiserQuestionsPoseesSpecifiqueId.executeQuery();
			int i = 0;
			System.out.println(rs1.getString(1));
			while(rs1.next()){
				i++;
				System.out.println(i + " " + rs1.getString(1) + " " + rs1.getString(2) + " " + rs1.getString(3));
			}
		}catch(SQLException se) {
			
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
			scanner.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Au revoir et à bientôt");
		System.exit(0);
	}
}


