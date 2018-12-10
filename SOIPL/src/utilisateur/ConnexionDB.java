package utilisateur;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnexionDB {
	
	Connection connection;
	
	public ConnexionDB() {
		
	}
	
	public Connection getConnection() {
	    try {
	        Class.forName("org.postgresql.Driver");
	        connection = null;
<<<<<<< HEAD
	        connection = DriverManager.getConnection("jdbc:postgresql://172.24.2.6:5432/dblbokiau17","rhonore16", ")XUE7Ha");
=======
	        connection = DriverManager.getConnection(
	        	"jdbc:postgresql://localhost:5432/SOIPL","postgres", "azerty1.");
	        	//"jdbc:postgresql://localhost:5432/postgres","postgres", "26qy68o6P1");
	        	//"jdbc:postgresql://172.24.2.6:5432/dblbokiau17","lbokiau17", "Qamq=277");
	        	//"jdbc:postgresql://172.24.2.6:5432/dblbokiau17","rhonore16", ")XUE7Ha");
>>>>>>> b907c294642c48b5ccc423947d4c295f2e919635
	    }
	    catch(Exception e) {
	        System.out.println(e);

	    }
	    return connection;
	}
	
}
