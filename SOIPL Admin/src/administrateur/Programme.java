package administrateur;

import connexion.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

public class Programme {
	public Connection connection;
	private static Scanner scanner = new Scanner(System.in);
	private PreparedStatement psAjoutTag;
	private PreparedStatement psAugmentationForcee;
	private PreparedStatement psDesactiverCompte;
	private PreparedStatement psHistoriqueQuestions;
	private PreparedStatement psHistoriqueReponses;
	
	public Programme(){
		this.connection = connexionDB();
		try {
			this.psAjoutTag = connection.prepareStatement("SELECT SOIPL.creation_nouveau_tag(?)");
			this.psAugmentationForcee = connection.prepareStatement("SELECT SOIPL.augmentation_forcee_statut_utilisateur(?,?)");
			this.psDesactiverCompte = connection.prepareStatement("SELECT SOIPL.desactivation_compte_utilisateur(?)");
			this.psHistoriqueQuestions = connection.prepareStatement("SELECT * FROM SOIPL.questions WHERE utilisateur_createur = ? AND date_creation >= ? AND date_creation <= ?");
			this.psHistoriqueReponses = connection.prepareStatement("");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void menuAvecChoix() {
		System.out.println("----------------------------------------------");
		System.out.println("|           Que voulez-vous faire ?          |");
		System.out.println("----------------------------------------------");
		System.out.println("|1. Desactiver un compte.                    |");
		System.out.println("|2. Augmentation forcee d'un compte.         |");
		System.out.println("|3. Ajouter un tag.                          |");
		System.out.println("|4. Historique d'un utilisateur.             |");
		System.out.println("|5. Eteindre le programme.                   |");
		System.out.println("----------------------------------------------");
		int choix = 0;
		
		do {
			System.out.print("Veuillez rentrer votre choix : ");
			choix = scanner.nextInt();
		}while(!(choix > 0 && choix < 6));
		
		switch(choix) {
			case 1:
				desactiverCompte();
				break;
			case 2:
				augmentationForcee();
				break;
			case 3:
				ajouterTag();
				break;
			case 4:
				historiqueUtilisateur();
				break;
			case 5:
				fermerLeProgramme();
				break;
		}
	}
	
	private void ajouterTag() {
		System.out.println("Veuillez rentrer le nom du tag que vous souhaitez ajouter :");
		String tag = scanner.next();
		try {
			psAjoutTag.setString(1, tag);
            ResultSet rs = psAjoutTag.executeQuery();
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		menuAvecChoix();
	}

	private void augmentationForcee() {
		System.out.println("Veuillez rentrer l'id de l'utilisateur dont vous souhaitez augmenter le status :");
		int idUtilisateur = scanner.nextInt();
		System.out.println("Veuillez rentrer le status que vous souhaitez lui mettre :");
		String status = scanner.next();
		try {
			psAugmentationForcee.setInt(1, idUtilisateur);
			psAugmentationForcee.setString(2, status);
            ResultSet rs = psAugmentationForcee.executeQuery();
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		menuAvecChoix();
	}

	private void desactiverCompte() {
		System.out.println("Veuillez rentrer l'id de l'utilisateur dont vous souhaitez desactiver le compte :");
		int idUtilisateur = scanner.nextInt();
		try {
			psDesactiverCompte.setInt(1, idUtilisateur);
			ResultSet rs = psDesactiverCompte.executeQuery();
			rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		menuAvecChoix();
		
	}
	
	private void historiqueUtilisateur() {
		System.out.println("Veuillez rentrer l'id de l'utilisateur dont vous souhaitez afficher l'historique :");
		int idUtilisateur = scanner.nextInt();
		System.out.println("Veuillez rentrer la date de debut sous le format ('yyyy-MM-ddTHH:mm:ss.SSS'):");
		LocalDateTime dateDebut = LocalDateTime.parse(scanner.next());
		System.out.println(dateDebut);
		System.out.println("Veuillez rentrer la date de fin sous le format ('yyyy-MM-ddTHH:mm:ss.SSS'):");
		LocalDateTime dateFin = LocalDateTime.parse(scanner.next());
		System.out.println(dateFin);
		System.out.println(Timestamp.valueOf(dateFin));
		System.out.println("---------------------------------------");
		System.out.println("| Liste de l'historique des questions |");
		System.out.println("---------------------------------------");
		try {
			psHistoriqueQuestions.setInt(1, idUtilisateur);
			psHistoriqueQuestions.setTimestamp(2, Timestamp.valueOf(dateDebut));
			psHistoriqueQuestions.setTimestamp(3, Timestamp.valueOf(dateFin));
			ResultSet rs = psHistoriqueQuestions.executeQuery();
			while (rs.next()) {
                if(rs.getInt(4) == 0) {
                	System.out.println(rs.getInt(1) + ". " + rs.getInt(2)+"|"+ rs.getTimestamp(3)+"|"+ rs.getString(6) +"|"+ rs.getString(7) +"|"+ rs.getBoolean(8));
                }else {                	
                	System.out.println(rs.getInt(1) + ". " + rs.getInt(2) +"|"+ rs.getTimestamp(3) +"|"+ rs.getInt(4)+"|"+ rs.getTimestamp(5) +"|"+ rs.getString(6) +"|"+ rs.getString(7) +"|"+ rs.getBoolean(8));
                }
            }
			rs.close();
		}catch(Exception e) {
			System.out.println("Il n'y a pas de questions de l'utilisateur entre ces dates-là.");
		}
		System.out.println();
		System.out.println("---------------------------------------");
		System.out.println("| Liste de l'historique des reponses  |");
		System.out.println("---------------------------------------");
		try {
			psHistoriqueReponses.setInt(1, idUtilisateur);
			psHistoriqueReponses.setTimestamp(2, Timestamp.valueOf(dateDebut));
			psHistoriqueReponses.setTimestamp(3, Timestamp.valueOf(dateFin));
			ResultSet rsRep = psHistoriqueReponses.executeQuery();
			while (rsRep.next()) {
                System.out.println(rsRep.getInt(1) + ". " + rsRep.getInt(2)+"|"+ rsRep.getInt(3)+"|"+ rsRep.getInt(4) +"|"+ rsRep.getInt(5)+"|"+ rsRep.getString(6)+"|"+ rsRep.getTimestamp(7));
            }
			rsRep.close();
		}catch(Exception e) {
			System.out.println("Il n'y a pas de réponses de l'utilisateur entre ces dates-là.");
		}
		System.out.println();
		menuAvecChoix();
	}

	public Connection connexionDB() {
		ConnexionDB aRenvoyer = new ConnexionDB();
		return aRenvoyer.getConnection();
	}
	
	private void fermerLeProgramme(){
		try {
			psAjoutTag.close();
			psAugmentationForcee.close();
			psDesactiverCompte.close();
		} catch (SQLException e) {}
		scanner.close();
		System.out.println("Au revoir et à bientôt");
		System.exit(0);
	}
}