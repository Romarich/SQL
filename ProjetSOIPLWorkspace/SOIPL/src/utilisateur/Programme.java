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
	
	//TODO le main permet de lancer que quelques methodes par exemple la connexion de l'utilisateur, le program en lui meme et
	// les prepare statements
	// le scanner devra juste entre close apres le menu quand on utilise une touche pour quitter le programme
	// donc pas dans le main
	public Programme() {
		this.connection = connexionDB();
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
		System.out.println("----------------------------------------------");
		int choix = 0;
		
		do {
			System.out.print("Veuillez rentrer votre choix : ");
			choix = scanner.nextInt();
		}while(!(choix > 0 && choix < 6));
		
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
		}
	}
	
	public Connection connexionDB() {
		ConnexionDB aRenvoyer = new ConnexionDB();
		return aRenvoyer.getConnection();
	}
	
	public boolean connexionUtilisateur() {
		System.out.println("Etes-vous deja inscrit ? (O/N)");
		String caract = scanner.nextLine();
		if("O".equals(caract)) {
			System.out.print("Veuillez entrer votre identifiant : ");
			String login = scanner.nextLine();
			System.out.print("Veuillez entrer votre mot de passe : ");
			String password = scanner.nextLine();
			try {
	            PreparedStatement ps = connection
	                    .prepareStatement("SELECT id_utilisateur, mot_de_passe FROM SOIPL.utilisateurs WHERE nom_utilisateur= ?");
	            ps.setString(1, login);
	            ResultSet rs = ps.executeQuery();
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
	            ps.close();

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
				PreparedStatement ps = connection.prepareStatement("SELECT inscription_utilisateur(?, ?, ?)");
				ps.setString(1,email);
				ps.setString(2,login);
				ps.setString(3,password);
				ps.executeQuery();
			}catch (SQLException se) {
				//TODO (à completer)
				System.out.println("Erreur lors de l’insertion ! Essayer une autre adresse mail, ou un autre nom d'utilisateur");
				connexionUtilisateur();
			}
			try {
				 PreparedStatement ps = connection
		                    .prepareStatement("SELECT selection_id_utilisateur_avec_nom_utilisateur(?)");
		            ps.setString(1, login);
		            ResultSet rs = ps.executeQuery();
		            rs.next();
		            utilisateur = rs.getInt(1);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}else {
			connexionUtilisateur();
		}
		return true;
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
			PreparedStatement ps = connection.prepareStatement("SELECT creation_nouvelle_question(?,?,?);" );
			ps.setInt(1,utilisateur);
			ps.setString(2,corpQuestion);
			ps.setString(3,titre);
			ps.executeQuery();	
		}catch (SQLException se) {
			System.out.println("Erreur lors de l’insertion !");
			se.printStackTrace();
			System.exit(1); 
		}
		menuAvecChoix();
	}
	
	public void visualiserQuestionsPosees() {
		System.out.println("Affichage de toutes les Questions Posees");
		try {
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM SOIPL.questions WHERE utilisateur_createur = ?");
			ps.setInt(1, utilisateur);
			ResultSet rs = ps.executeQuery();
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
            PreparedStatement ps = connection
                    .prepareStatement("SELECT * FROM SOIPL.questions");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getString(1) + ". " + rs.getString(7));
                
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
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

}


