package administrateur;

import connexion.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Programme {
	public Connection connection;
	private static Scanner scanner = new Scanner(System.in);
	private PreparedStatement psAjoutTag;
	private PreparedStatement psAugmentationForcee;
	private PreparedStatement psDesactiverCompte;
	
	public Programme(){
		this.connection = connexionDB();
		try {
			this.psAjoutTag = connection.prepareStatement("SELECT creation_nouveau_tag(?)");
			this.psAugmentationForcee = connection.prepareStatement("SELECT augmentation_forcee_statut_utilisateur(?,?)");
			this.psDesactiverCompte = connection.prepareStatement("SELECT desactivation_compte_utilisateur(?)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void menuAvecChoix() {
		System.out.println("----------------------------------------------");
		System.out.println("|           Que voulez-vous faire ?          |");
		System.out.println("----------------------------------------------");
		System.out.println("|1. Desactiver un compte.                    |");
		System.out.println("|2. Augmentation forc�e d'un compte.         |");
		System.out.println("|3. Ajouter un tag.                          |");
		System.out.println("|4. Eteindre le programme.                   |");
		System.out.println("----------------------------------------------");
		int choix = 0;
		
		do {
			System.out.print("Veuillez rentrer votre choix : ");
			choix = scanner.nextInt();
		}while(!(choix > 0 && choix < 5));
		
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
		
	}

	public Connection connexionDB() {
		ConnexionDB aRenvoyer = new ConnexionDB();
		return aRenvoyer.getConnection();
	}
	
	public void fermerLeProgramme(){
		scanner.close();
		System.out.println("Au revoir et � bient�t");
		System.exit(0);
	}
}