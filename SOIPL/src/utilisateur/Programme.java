package utilisateur;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
	//private PreparedStatement psVisualiserInformationsUtilisateur;
	private PreparedStatement psIntroductionNouvelleReponse;
	private PreparedStatement psUtilisateurPasDesactive;
	private PreparedStatement psSelectionDeTousLesTags;
	private PreparedStatement psSelectionQuestionParTag;
	private PreparedStatement psVote;
	private PreparedStatement psEditionQuestion;
	private PreparedStatement psEditionTitreQuestion;
	private PreparedStatement psVisualiserQuestionsRepondue;
	private PreparedStatement psAjoutTag;
	private PreparedStatement psCloturerQuestion;
	
	public Programme(){
		this.connection = connexionDB();	
		try {
			this.psSelectionUtilisateurDejaPresent = connection.prepareStatement("SELECT id_utilisateur, mot_de_passe FROM SOIPL.utilisateurs WHERE nom_utilisateur= ?");
			this.psInscriptionNouvelUtil = connection.prepareStatement("SELECT SOIPL.inscription_utilisateur(?, ?, ?)");
			this.psSelectionDeLUtilisateurEnCours = connection.prepareStatement("SELECT SOIPL.selection_id_utilisateur_avec_nom_utilisateur(?)");
			this.psIntroductionNouvelleQuestion = connection.prepareStatement("SELECT SOIPL.creation_nouvelle_question(?,?,?);" );
			this.psVisualiserQuestionsPosees = connection.prepareStatement("SELECT * FROM SOIPL.questions WHERE utilisateur_createur = ?");
			this.psVisualiserQuestionsPoseesSpecifiqueId = connection.prepareStatement("SELECT * FROM SOIPL.view_questions_utilisateurs WHERE id_question = ?");
			this.psVisualiserToutesLesQuestions = connection.prepareStatement("SELECT * FROM SOIPL.view_toutes_questions");
			this.psVisualiserReponses = connection.prepareStatement("SELECT * FROM SOIPL.view_reponses_utilisateurs WHERE id_question = ?");
			//this.psVisualiserInformationsUtilisateur = connection.prepareStatement("SELECT * FROM SOIPL.utilisateurs WHERE id_utilisateur  = ?");
			this.psIntroductionNouvelleReponse = connection.prepareStatement("SELECT SOIPL.creation_reponse(?,?,?)");
			this.psUtilisateurPasDesactive = connection.prepareStatement("SELECT desactive FROM SOIPL.utilisateurs WHERE id_utilisateur =?");
			this.psSelectionDeTousLesTags = connection.prepareStatement("SELECT * FROM SOIPL.tags");
			this.psSelectionQuestionParTag = connection.prepareStatement("SELECT * FROM SOIPL.view_questions_tags WHERE id_tag = ?");
			this.psVote = connection.prepareStatement("SELECT SOIPL.creation_vote(?,?,?)");
			this.psEditionQuestion = connection.prepareStatement("SELECT SOIPL.edition_question(?,?,?)");
			this.psEditionTitreQuestion = connection.prepareStatement("SELECT SOIPL.edition_titre_question(?,?,?)");
			this.psVisualiserQuestionsRepondue = connection.prepareStatement("SELECT * FROM SOIPL.view_toutes_questions_titre WHERE id_utilisateur = ?");
			this.psAjoutTag = connection.prepareStatement("SELECT SOIPL.ajout_tag_question(?,?)");
			this.psCloturerQuestion = connection.prepareStatement("SELECT SOIPL.cloturer_question(?,?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void menuAvecChoix() {
		//scanner.reset();
		utilisateurDesactive();
		System.out.println("----------------------------------------------");
		System.out.println("|           Que voulez-vous faire ?          |");
		System.out.println("----------------------------------------------");
		System.out.println("|1. Introduire une nouvelle question.        |");
		System.out.println("|2. Visualiser les questions posées.         |");
		System.out.println("|3. Visualiser les questions repondues.      |");
		System.out.println("|4. Visualiser toutes les questions.         |");
		System.out.println("|5. Visualiser les questions d'un tag.       |");
		System.out.println("|6. Deconnexion utilisateur.                 |");
		System.out.println("|7. Eteindre le programme.                   |");
		System.out.println("----------------------------------------------");
		int choix = 0;
		
		do {
			System.out.print("Veuillez rentrer votre choix : ");
			choix = Integer.parseInt(scanner.nextLine());
		}while(!(choix > 0 && choix < 8));
		
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
				connexionUtilisateur();
				break;
			case 7:
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
	            	System.out.println("Identifiant, Mot de passe incorrect ou l'utilisateur a été désactivé");
	            	connexionUtilisateur();
	            }else {
	            	menuAvecChoix();
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
				//s
				System.out.println("Erreur lors de lâ€™insertion ! Essayer une autre adresse mail, ou un autre nom d'utilisateur");
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
			menuAvecChoix();
		}else {
			connexionUtilisateur();
		}
	}
	
	public void introduireNouvelleQuestion() {
		utilisateurDesactive();
		System.out.println("Introduisez le titre de votre nouvelle question");
		String titre="";
		while("".equals(titre)) {
			titre = scanner.nextLine();
		}
		System.out.println("Introduisez le corps de votre question");
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
			System.out.println("Erreur lors de lâ€™insertion !");
			se.printStackTrace();
			System.exit(1);
		}
		menuAvecChoix();
	}
	
	public void visualiserQuestionsPosees() {
		utilisateurDesactive();
		try {
			psVisualiserQuestionsPosees.setInt(1, this.utilisateur);
			ResultSet rs = psVisualiserQuestionsPosees.executeQuery();
			ArrayList<Integer> ids = new ArrayList<Integer>();
			while (rs.next()) {
                System.out.println(rs.getString(1) + ". " + rs.getString(7));
                ids.add(Integer.parseInt(rs.getString(1)));
            }
		}catch(SQLException se) {
			System.out.println(this.utilisateur);
			System.out.println(se);
			System.out.println("Vous n'avez pas encore posé de questions");
			menuAvecChoix();
		}
		System.out.println("Quel question souhaitez voir en detail ?");
		int choixVisualisationQuestionSpecifique= Integer.parseInt(scanner.nextLine());
		voirQuestion(choixVisualisationQuestionSpecifique);
		menuAvecChoix();
	}
	
	public void toutesLesQuestions() {
		utilisateurDesactive();
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
        	e.printStackTrace();
			menuAvecChoix();
        }
		System.out.println("");
		int choixVisualisationQuestionSpecifique;

		do {
			System.out.println("Quel question souhaitez voir en detail ?");
			choixVisualisationQuestionSpecifique = Integer.parseInt(scanner.nextLine());
		}while(!ids.contains(choixVisualisationQuestionSpecifique));
		voirQuestion(choixVisualisationQuestionSpecifique);
		menuAvecChoix();
	}
	
	public void visualiserQuestionsRepondues() {
		utilisateurDesactive();
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			psVisualiserQuestionsRepondue.setInt(1,this.utilisateur);
            ResultSet rs = psVisualiserQuestionsRepondue.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString(3) + ". " + rs.getString(1));
                ids.add(Integer.parseInt(rs.getString(3)));
            }
            rs.close();
        }catch(SQLException se) {
			System.out.println(se);
		}catch (Exception e) {
        	System.out.println("Vous n'avez pas encore répondu Ã  des questions");
        	e.printStackTrace();
			menuAvecChoix();
        }
		int choixVisualisationQuestionSpecifique;
		do {
			System.out.println("Quel question souhaitez voir en detail ?");
			choixVisualisationQuestionSpecifique = Integer.parseInt(scanner.nextLine());
		}while(!ids.contains(choixVisualisationQuestionSpecifique));
		voirQuestion(choixVisualisationQuestionSpecifique);
		menuAvecChoix();
	}
	
	public void visualiserQuestionsAvecTag() {
		utilisateurDesactive();
		System.out.println("voici la liste des tags");
		try {
			ResultSet rs = psSelectionDeTousLesTags.executeQuery();
			while(rs.next()) {
				System.out.println(rs.getInt(1) + ". " + rs.getString(2));
			}
		}catch(SQLException se) {
			se.printStackTrace();
		}
		System.out.println("Veuillez choisir le numero du tag que vous souhaitez regarder");
		int chiffre = Integer.parseInt(scanner.nextLine());
		ArrayList<Integer> ids = new ArrayList<Integer>();
		try {
			psSelectionQuestionParTag.setInt(1, chiffre);
			ResultSet rs = psSelectionQuestionParTag.executeQuery();
			while (rs.next()) {
                System.out.println(rs.getString(1) + ". " + rs.getString(7));
                ids.add(Integer.parseInt(rs.getString(1)));
            }
            rs.close();
		}catch(SQLException se) {
			se.printStackTrace();
		}
		int choixVisualisationQuestionSpecifique;
		do {
			System.out.println("Quel question souhaitez voir en detail ?");
			choixVisualisationQuestionSpecifique = Integer.parseInt(scanner.nextLine());
		}while(!ids.contains(choixVisualisationQuestionSpecifique));
		voirQuestion(choixVisualisationQuestionSpecifique);
		menuAvecChoix();
	}
	
	public void utilisateurDesactive() {
		try {
			psUtilisateurPasDesactive.setInt(1, utilisateur);
            ResultSet rs = psUtilisateurPasDesactive.executeQuery();
            boolean ok = false;
            while (rs.next()) {
            	ok = rs.getBoolean(1);
            }
            if(ok) {
            	System.out.println("Vous avez été désactivé");
            	connexionUtilisateur();
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public void voirQuestion(int choixVisualisationQuestionSpecifique) {
		try {
			psVisualiserQuestionsPoseesSpecifiqueId.setInt(1, choixVisualisationQuestionSpecifique);
			ResultSet rs1 = psVisualiserQuestionsPoseesSpecifiqueId.executeQuery();
			System.out.println("##############################################");
			while (rs1.next()) {
            	System.out.println("Question de : " + rs1.getString(9));
            	System.out.println(rs1.getString(1) + ". " + rs1.getString(7));
                System.out.println("\t" + rs1.getString(6));
                System.out.print(rs1.getString(3));
                if(rs1.getString(4) != null) {
                	System.out.println(", derniÃ¨re edition :" + rs1.getString(5) + " par " + rs1.getString(4));
                }else {
                	System.out.println();
                }
                System.out.println("##############################################");
                System.out.println("");
            }
		}catch(SQLException se) {
			System.out.println(se);
			System.out.println("Cette question spécifique n'existe pas");
			toutesLesQuestions();
		}
		
		System.out.println("Réponses Ã  la question :");
		HashMap<Integer,Integer> map = new HashMap<Integer, Integer>(); 
		try {
			psVisualiserReponses.setInt(1, choixVisualisationQuestionSpecifique);
			ResultSet rs2 = psVisualiserReponses.executeQuery();
			System.out.println("----------------------------------------------");
			while (rs2.next()) {
				map.put(rs2.getInt(2), rs2.getInt(1));
            	System.out.println(rs2.getInt(2) + ". Réponse de : " + rs2.getString(8) + " le " + rs2.getString(7));
                System.out.println("\t" + rs2.getString(6));
                System.out.println("Score de la réponse : " + rs2.getString(5));
        		System.out.println("----------------------------------------------");
                System.out.println("");
			}
		}catch(SQLException se) {
		System.out.println(se);
		}

		System.out.println("Que souhaitez-vous faire ?");
		System.out.println("     > Répondre (R)");
		System.out.println("     > Voter (V)");
		System.out.println("     > Editer (E)");
		System.out.println("     > Ajouter Tags (T)");
		System.out.println("     > Cloturer la Question (C)");
		System.out.println("     > Quitter (Q)");
		String rep = scanner.nextLine();
		
		switch(rep) {
		
		case "R" : 
			try {
				System.out.println("Entrez votre réponse");
				String reponse = scanner.nextLine();
				psIntroductionNouvelleReponse.setInt(1, choixVisualisationQuestionSpecifique);
				psIntroductionNouvelleReponse.setString(2, reponse);
				psIntroductionNouvelleReponse.setInt(3, this.utilisateur);
				psIntroductionNouvelleReponse.executeQuery();
				System.out.println("");
				System.out.println("Merci !");
				System.out.println("");
			}catch(SQLException se) {
				System.out.println(se);
			}
			break;
		
		case "V" :
			String vote = "";
			try{
				while(!(("P").equals(vote) || ("N").equals(vote))) {
					System.out.println("voulez- vous voter positivement (P) ou négativement (N) ? ");
					vote = scanner.nextLine();
				}
				System.out.println("Entrez le numéro de la réponse");
				psVote.setInt(1, this.utilisateur);
				psVote.setBoolean(2, vote.equals("P"));
				psVote.setInt(3, map.get((Integer.parseInt(scanner.nextLine()))));
				psVote.executeQuery();
			}catch(SQLException se) {
				System.out.println(se);
			}
			break;
	
		case "E" :
			try {
				System.out.println("Voulez vous modifier le titre (T) ou le corps de la question (Q) ?");
				String modification = scanner.nextLine();
				if(modification.equals("Q")) {
				System.out.println("Entrez votre modification");
				String reponse = scanner.nextLine();
				psEditionQuestion.setInt(3, choixVisualisationQuestionSpecifique);
				psEditionQuestion.setString(1, reponse);
				psEditionQuestion.setInt(2, this.utilisateur);
				psEditionQuestion.executeQuery();
				System.out.println("");
				System.out.println("Merci !");
				System.out.println("");
				}
				if(modification.equals("T")) {
				System.out.println("Entrez le nouveau titre");
				String reponse = scanner.nextLine();
				psEditionTitreQuestion.setInt(3, choixVisualisationQuestionSpecifique);
				psEditionTitreQuestion.setString(1, reponse);
				psEditionTitreQuestion.setInt(2, this.utilisateur);
				psEditionTitreQuestion.executeQuery();
				System.out.println("");
				System.out.println("Merci !");
				System.out.println("");
				}
			}catch(SQLException se) {
				System.out.println(se);
			}
			break;
		case "T" :
			try {
				System.out.println("Quel tag voulez-vous ajouter ?");
				String tag = scanner.nextLine();
				psAjoutTag.setString(1, tag);
				psAjoutTag.setInt(2, choixVisualisationQuestionSpecifique);
				psAjoutTag.executeQuery();
				System.out.println("");
				System.out.println("Merci !");
				System.out.println("");
			}catch(SQLException se) {
				System.out.println(se);
			}
			break;
		
		case "C" :
			try {
				psCloturerQuestion.setInt(1, choixVisualisationQuestionSpecifique);
				psCloturerQuestion.setInt(2, utilisateur);
				psCloturerQuestion.executeQuery();
			}catch(SQLException se) {
				System.out.println(se);
			}
		}
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
			psVote.close();
			psEditionQuestion.close();
			psEditionTitreQuestion.close();
			psAjoutTag.close();
			scanner.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Au revoir et Ã  bientÃ´t");
		System.exit(0);
	}
}