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
	        connection = DriverManager.getConnection("jdbc:postgresql://172.24.2.6:5432/dblbokiau17","rhonore16", ")XUE7Ha");
	    }
	    catch(Exception e) {
	        System.out.println(e);

	    }
	    return connection;
	}
	
}
