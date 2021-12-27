package tor.secure.chat.protocol.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

	private Connection connect = null;
	private Statement statement = null;

	public DatabaseConnection(String host, String port, String user, String database, String password) {
		System.out.println("[database] :: connecting");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			connect = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?" + "user=" + user + "&password=" + password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			statement = connect.createStatement(
				ResultSet.TYPE_SCROLL_SENSITIVE, 
				ResultSet.CONCUR_UPDATABLE);
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
		System.out.println("[database] :: connected");
	}

	public void execute(String sql) {
		try {
			statement.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet query(String query) {
		try {
			return statement.executeQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

}
