package com.quimibot.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AnimalCrossingService {

    private static String urlPrediccion = "https://turnipprophet.io/index.html?action=";
    private static final String HOST = "host";
    private static final String USERNAME = "usuario";
    private static final String PASSWORD = "contraseña";
    private static String rutaChrome = "/app/.chromedriver/bin/chromedriver";
    private static Map<String, String> predicciones = new HashMap<String, String>();
    private static String fechaPredicciones = "";

    public static String getNabos(String usuario) {
        String res = "PRECIOS DE LOS NABOS DE " + usuario.toUpperCase() + ":\n\n";

        //Listado de días en español
        Map<String, String> traduccion = new HashMap<String, String>();

        traduccion.put("Monday", "Lunes");
        traduccion.put("Tuesday", "Martes");
        traduccion.put("Wednesday", "Miércoles");
        traduccion.put("Thursday", "Jueves");
        traduccion.put("Friday", "Viernes");
        traduccion.put("Saturday", "Sábado");
        traduccion.put("Sunday", "Domingo");

        try {

            //Conexion con BBDD
            List<String> texto = getNabosBBDD(usuario);

            //Se ordena la información de los nabos por fecha
            List<String> fechas = getFechas();
            List<String> ordenada = new ArrayList<String>();
            for (String s : fechas) {
                Integer contador = 0;
                for (String t : texto) {
                    if (t.contains(s)) {
                        contador++;
                        ordenada.add(t);
                        if (contador == 2) {
                            break;
                        }

                    }
                }
            }

            //Si no hay datos
            if (ordenada.isEmpty()) {
                res = "No se dispone de datos de nabos para la persona " + usuario.substring(0, 1).toUpperCase() + usuario.substring(1).toLowerCase();
            }

            //Si hay datos
            else {
                for (String s : ordenada) {
                    if (s.contains(usuario.toLowerCase())) {

                        //Se meten los datos al mensaje
                        String[] trozos = s.split(",");
                        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                        Date date = format.parse(trozos[1]);
                        DateFormat format2 = new SimpleDateFormat("EEEE");
                        String dia = format2.format(date);
                        res += traduccion.get((dia.substring(0, 1).toUpperCase() + dia.substring(1))) + " " + trozos[1] + " por la " + trozos[2] + ": " + trozos[3] + " bayas por nabo.\n";
                    }
                }
            }

        } catch (Exception e) {
            //Si se produce algún error
            e.printStackTrace();
            res = "Ha ocurrido un error al intentar recuperar el precio de los nabos.";
        }
        return res;
    }

    public static String getNabosPorDia() {
        //Fecha de hoy
        Calendar cal = Calendar.getInstance();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaABuscar = "";
        String momento = "";
        Integer horaActual = cal.get(Calendar.HOUR_OF_DAY);
        String horaServidor = String.format("(Hora servidor %02d:%02d)", horaActual, cal.get(Calendar.MINUTE));
        int weekday = cal.get(Calendar.DAY_OF_WEEK);

        //Se comprueba la hora
        if ((horaActual >= 8 || (horaActual >= 5 && weekday == 1)) && horaActual < 12) {
            momento = "mañana";
        } else if (weekday != 1 && horaActual >= 12 && horaActual < 22) {
            momento = "tarde";
        } else {

            if (weekday == 1 && horaActual >= 12) {
                return String.format("Ya se han vendido los nabos, llegas tarde \uD83D\uDE12 \n\n%s", horaServidor);
            } else if (weekday == 1 && horaActual < 5) {
                return String.format("Aún no ha llegado Juliana, por favor, sé paciente \uD83D\uDE12 \n\n%s", horaServidor);
            } else {
                return String.format("Ahora mismo la tienda está cerrada, vuelve a una hora más decente \uD83D\uDE12 \n\n%s", horaServidor);
            }

        }
        fechaABuscar = dtf.format(LocalDate.now());

        String res = String.format("PRECIO DE LOS NABOS ACTUALMENTE %s:\n\n", horaServidor);
        try {
            //Se cogen los nabos usando la fecha de hoy y el momento actual
            res += getNabosPorFechaBBDD(fechaABuscar, momento);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
            return "Se ha producido un error al buscar la información solicitada.";
        }

        return res;

    }

    public static String getPrediccion(String usuario) throws SQLException {
        List<String> fechas = getFechas();

        if (predicciones.containsKey(usuario.toLowerCase()) && fechas.contains(fechaPredicciones)) {
            String res = predicciones.get(usuario.toLowerCase());
            return res;
        } else {
            if (!fechas.contains(fechaPredicciones)) {
                fechaPredicciones = "";
                predicciones = new HashMap<String, String>();
            }
            String res = "PREDICCIÓN DE PRECIOS DE LOS NABOS DE " + usuario.toUpperCase() + "\n\n";
            //Listado de días que se recogerá más tarde
            String[] textoDias = {"Lunes", "Lunes", "Martes", "Martes", "Miércoles", "Miércoles", "Jueves", "Jueves", "Viernes", "Viernes", "Sábado", "Sábado"};

            //Se obtienen los nabos del usuario
            List<String> lineas = getNabosBBDD(usuario.toLowerCase());

            //Si no tiene nabos, no se realiza la predicción
            if (lineas.isEmpty()) {
                return "No se puede realizar la predicción puesto que no se disponen de datos para la persona " + usuario.substring(0, 1).toUpperCase() + usuario.substring(1).toLowerCase();
            }

            //Se sacan todas las fechas de la semana
            List<String> dias = getFechas();
            System.out.println("Listado de días: \n");
            for (String s : dias) {
                System.out.println("- " + s + "\n");
            }

            //Se monta el array con todas las posiciones
            String[] precios = new String[13];
            for (String s : lineas) {

                //Si es del usuario que estamos consultando
                if (s.contains(usuario.toLowerCase())) {
                    String[] trozos = s.split(",");
                    String dia = trozos[1];
                    Integer diaListado = dias.indexOf(dia);
                    if (diaListado.equals(-1)) {
                        return "Ha ocurrido un error mientras se intentaba obtener la predicción de nabos.";
                    }
                    //Si es el domingo
                    else if (diaListado.equals(0)) {
                        precios[0] = trozos[3];
                    } else {
                        if (trozos[2].equals("mañana")) {
                            precios[2 * (diaListado - 1) + 1] = trozos[3];
                            System.out.println("Hueco " + (2 * (diaListado - 1) + 1) + ": " + trozos[3]);
                        } else {
                            precios[2 * (diaListado - 1) + 2] = trozos[3];
                            System.out.println("Hueco " + (2 * (diaListado - 1) + 2) + ": " + trozos[3]);
                        }
                    }
                }
            }

            WebDriverService webDriverService = new WebDriverService();
            WebDriver driver = webDriverService.getDriver();

            try {
                driver.get(urlPrediccion);

                WebDriverWait wait = new WebDriverWait(driver, 15);
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='buy']")));

                Select idioma = new Select(driver.findElement(By.xpath("//select[@id='language']")));
                idioma.selectByVisibleText("English");

                for (int i = 0; i < 13; i++) {
                    if (precios[i] != null) {
                        if (i == 0) {
                            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='buy']")));
                            driver.findElement(By.xpath("//input[@id='buy']")).sendKeys(precios[i]);
                            Thread.sleep(100);
                        } else {
                            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//input[@id='sell_" + (i + 1) + "']")));
                            driver.findElement(By.xpath("//input[@id='sell_" + (i + 1) + "']")).sendKeys(precios[i]);
                            Thread.sleep(100);
                        }
                    }
                }

                Thread.sleep(1000);

                for (int i = 4; i <= 15; i = i + 2) {
                    String morningPrice = driver.findElement(By.xpath("//td[contains(text(),'All patterns')]/../td[" + i + "]")).getText();
                    String afternoonPrice = driver.findElement(By.xpath("//td[contains(text(),'All patterns')]/../td[" + (i + 1) + "]")).getText();
                    res += "Precios para el " + textoDias[i - 3] + ": Mañana -> " + morningPrice + ", Tarde -> " + afternoonPrice + "\n";
                }
                fechaPredicciones = getDate();
                predicciones.put(usuario.toLowerCase(), res);
                driver.close();
            } catch (Exception e) {
                driver.close();
                e.printStackTrace();
            }


            return res;
        }

    }

    private static List<String> getFechas() {
        List<String> dias = new ArrayList<String>();
        Calendar cal = Calendar.getInstance();
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TimeZone tz = cal.getTimeZone();
        ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
        LocalDateTime now = LocalDateTime.ofInstant(cal.toInstant(), zid);
        System.out.println("Weekday: " + weekday);

        if (weekday == 1) {
            dias.add(dtf.format(now));
            return dias;
        }

        dias.add(dtf.format(now.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY))));
        if (weekday == 2) {
            dias.add(dtf.format(now));
            return dias;
        }

        dias.add(dtf.format(now.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))));
        if (weekday == 3) {
            dias.add(dtf.format(now));
            return dias;
        }

        dias.add(dtf.format(now.with(TemporalAdjusters.previous(DayOfWeek.TUESDAY))));
        if (weekday == 4) {
            dias.add(dtf.format(now));
            return dias;
        }

        dias.add(dtf.format(now.with(TemporalAdjusters.previous(DayOfWeek.WEDNESDAY))));
        if (weekday == 5) {
            dias.add(dtf.format(now));
            return dias;
        }

        dias.add(dtf.format(now.with(TemporalAdjusters.previous(DayOfWeek.THURSDAY))));
        if (weekday == 6) {
            dias.add(dtf.format(now));
            return dias;
        }

        dias.add(dtf.format(now.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY))));
        dias.add(dtf.format(now));
        return dias;

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

    private static List<String> getNabosBBDD(String usuario) throws SQLException {
        Connection conn = null;
        List<String> lista = new ArrayList<String>();
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
            List<String> fechas = getFechas();
            Boolean primero = true;
            if (!fechas.isEmpty()) {
                String query = "SELECT * FROM \"NABOS\" n WHERE n.usuario = '" + usuario.toLowerCase() + "' and (";
                System.out.println("FECHAS---------");
                for (String s : fechas) {
                    System.out.println(s);
                    if (!primero) {
                        query += " or ";
                    }
                    query += "n.fecha = '" + s + "'";
                    primero = false;
                }
                query += ")";
                ResultSet rs = stat.executeQuery(query);
                conn.close();

                while (rs.next()) {
                    String persona = rs.getString("USUARIO");
                    String fecha = rs.getString("FECHA");
                    String momento = rs.getString("MOMENTO");
                    String precio = rs.getString("PRECIO");
                    String res = persona + "," + fecha + "," + momento + "," + precio;
                    lista.add(res);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("No se ha podido realizar la consulta.");
            conn.close();
        }
        return lista;
    }

    private static String getNabosPorFechaBBDD(String fechaConsultada, String momento) throws SQLException {
        Connection conn = null;
        String total = "";
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
            String query = "SELECT * FROM \"NABOS\" n WHERE n.fecha = '" + fechaConsultada + "' AND n.momento = '" + momento + "'";
            ResultSet rs = stat.executeQuery(query);
            System.out.println("Query realizada con éxito.");
            System.out.println("Elementos:");
            conn.close();

            while (rs.next()) {
                String persona = rs.getString("USUARIO");
                String precio = rs.getString("PRECIO");
                String res = persona.substring(0, 1).toUpperCase() + persona.substring(1) + " tiene los nabos a " + precio + " bayas.";
                System.out.println(res);
                total += res + "\n";
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("No se ha podido realizar la consulta.");
            conn.close();
        }
        return total;
    }

    public static String setNabosBBDD(String usuario, String momento, Integer precio) throws SQLException {
        Connection conn = null;
        String respuesta = "";
        Calendar cal = Calendar.getInstance();
        int weekday = cal.get(Calendar.DAY_OF_WEEK);
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
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TimeZone tz = cal.getTimeZone();
        ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
        LocalDateTime now = LocalDateTime.ofInstant(cal.toInstant(), zid);
        String fecha = dtf.format(now);

        if (weekday == 1 && momento.equals("tarde")) {
            respuesta = "Hoy es domingo, no se pueden vender nabos \uD83D\uDE14";
        }

        try {
            ResultSet rs = stat.executeQuery("SELECT * FROM \"NABOS\" n WHERE n.usuario = '" + usuario.toLowerCase() + "' AND FECHA = '" + fecha + "' AND MOMENTO = '" + momento + "'");

            if (!rs.next()) {
                PreparedStatement st = conn.prepareStatement("INSERT INTO \"NABOS\" (USUARIO, FECHA, MOMENTO, PRECIO) VALUES (?, ?, ?, ?);");
                st.setString(1, usuario.toLowerCase());
                st.setString(2, fecha);
                st.setString(3, momento);
                st.setInt(4, precio);
                Integer res = st.executeUpdate();
                System.out.println(res);
                conn.close();
                System.out.println("Precio añadido con éxito.");
                respuesta = "Precio añadido con éxito.";
                if (predicciones.containsKey(usuario)) {
                    predicciones.remove(usuario);
                }

            } else {
                PreparedStatement st = conn.prepareStatement("UPDATE \"NABOS\" SET PRECIO = ? WHERE USUARIO = ? AND FECHA = ? AND MOMENTO = ?;");
                st.setInt(1, precio);
                st.setString(2, usuario.toLowerCase());
                st.setString(3, fecha);
                st.setString(4, momento);
                Integer res = st.executeUpdate();
                System.out.println(res);
                conn.close();
                System.out.println("Precio actualizado con éxito.");
                respuesta = "Precio actualizado con éxito.";
                if (predicciones.containsKey(usuario)) {
                    predicciones.remove(usuario);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("No se ha podido realizar la actualización de los datos.");
            conn.close();
            return "No se ha podido realizar la actualización de los datos.";
        }
        return respuesta;
    }

    public static void reset() {
        predicciones = new HashMap<String, String>();
    }

    private static String getDate() {
        Calendar cal = Calendar.getInstance();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TimeZone tz = cal.getTimeZone();
        ZoneId zid = tz == null ? ZoneId.systemDefault() : tz.toZoneId();
        LocalDateTime now = LocalDateTime.ofInstant(cal.toInstant(), zid);
        return dtf.format(now);
    }

}
