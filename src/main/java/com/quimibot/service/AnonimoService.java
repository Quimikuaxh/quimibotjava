package com.quimibot.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AnonimoService {

    private static final String HOST = "host";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";

    public static String getChatId() throws Exception {
        
    	String id = getChatIdBBDD("anonimo");
        return id;
    }
    
    private static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            System.out.println("Connection failed! PostgreSQL JDBC Driver is not working.");
        }
        Connection conn = DriverManager.getConnection("jdbc:postgresql://" + HOST + "?sslmode=require", USERNAME, PASSWORD);
        return conn;
    }

    private static String getChatIdBBDD(String nombre) throws Exception {
        Connection conn = null;
        String id="";
        try {
            conn = getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error al establecer la conexión.");
        }
        if (conn != null) {
            System.out.println("Conexión con la base de datos establecida.");
        } else {
            System.out.println("No se pudo establecer la conexión.");
        }

        Statement stat = conn.createStatement();

        try {
            String query = "SELECT * FROM \"ANONIMO\" a WHERE a.nombre = '" + nombre + "'";

            ResultSet rs = stat.executeQuery(query);
            rs.next();
            id = rs.getString("CHATID");
            conn.close();
            if(id.equals("")){
            	throw new Exception("No se encontró el identificador");
            }
               
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("No se ha podido realizar la consulta.");
            conn.close();
        }
        return id;
    }
    
    public static String setChatIdBBDD(String nombre, String id) throws Exception{
    	Connection conn = null;
        String respuesta = "";
        System.out.println("ID:"+id);
        
        try {
            conn = getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Error al establecer la conexión.");
            return "Se ha producido un error al establecer la conexión con la base de datos.";
        }
        if (conn != null) {
            System.out.println("Conexión con la base de datos establecida.");
        } else {
            System.out.println("No se pudo establecer la conexión.");
            return "Se ha producido un error al establecer la conexión con la base de datos.";
        }
        
        Statement stat = conn.createStatement();
        
        try {
        	 String query = "SELECT * FROM \"ANONIMO\" a WHERE a.nombre = '" + nombre + "'";
             ResultSet rs = stat.executeQuery(query);
            if (rs.next()) {
	            PreparedStatement st = conn.prepareStatement("UPDATE \"ANONIMO\" SET CHATID = ? WHERE NOMBRE = ?;");
	            st.setString(1, id);
	            st.setString(2, nombre);
	            Integer res = st.executeUpdate();
	            System.out.println(res);
	            conn.close();
            }
            else{
            	throw new Exception("No se ha encontrado la entrada con el identificador");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("No se ha podido realizar la actualización de los datos.");
            conn.close();
            return "No se ha podido realizar la actualización de los datos.";
        }
        return respuesta;
        
    }

}
