package scr;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class databaseConnects{
    private static final Path DB_PATH = resolveDbPath();
    public static final String URL = "jdbc:sqlite:" + DB_PATH.toAbsolutePath();
    private static boolean driverChecked = false;
    private static boolean driverAvailable = false;

    private static Path resolveDbPath() {
        Path srcDb = Paths.get("src", "database.db");
        if (Files.exists(srcDb)) {
            return srcDb;
        }
        return Paths.get("database.db");
    }

    private static synchronized boolean ensureDriverLoaded() {
        if (driverChecked) {
            return driverAvailable;
        }
        driverChecked = true;
        try {
            Class.forName("org.sqlite.JDBC");
            driverAvailable = true;
        } catch (ClassNotFoundException e) {
            System.out.println("SQLite JDBC driver not found. Add sqlite-jdbc-<version>.jar to your classpath.");
            driverAvailable = false;
        }
        return driverAvailable;
    }

    private static Connection openConnection() throws SQLException {
        if (!ensureDriverLoaded()) {
            throw new SQLException("SQLite JDBC driver missing from classpath");
        }
        return DriverManager.getConnection(URL);
    }

    public static void initialise(){
        String usersSql = "CREATE TABLE IF NOT EXISTS users("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + " password TEXT NOT NULL"
                + ");";

        String generatedPasswordsSql = "CREATE TABLE IF NOT EXISTS generated_passwords("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " user_id INTEGER NOT NULL,"
                + " password TEXT NOT NULL,"
                + " created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + " FOREIGN KEY(user_id) REFERENCES users(id)"
                + ");";

        try (Connection conn = openConnection();Statement stmt = conn.createStatement()) {
            stmt.execute(usersSql);
            stmt.execute(generatedPasswordsSql);
            System.out.println("Connection to SQLite database has been established");
        } catch (SQLException e) {
            System.out.println("An error occurred:" + e.getMessage());
        }
    }

    public static void saveData (String username,String password){
        String sql = "INSERT INTO users(username,password) VALUES(?,?)";
        try (Connection conn = openConnection();PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1,username);
            pstmt.setString(2,password);
            pstmt.executeUpdate();
        }catch(SQLException e){
            System.out.println("fail to save: "+e.getMessage());
        }
    }

    public static Integer findUserIdByUsername(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to lookup user: " + e.getMessage());
        }
        return null;
    }

    public static Integer login(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
        return null;
    }

    public static Integer registerUser(String username, String password) {
        String sql = "INSERT INTO users(username,password) VALUES(?,?)";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Register failed: " + e.getMessage());
        }
        return null;
    }

    public static void saveGeneratedPassword(int userId, String password) {
        String sql = "INSERT INTO generated_passwords(user_id, password) VALUES(?,?)";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to save generated password: " + e.getMessage());
        }
    }

    public static List<String> getGeneratedPasswordsByUserId(int userId) {
        List<String> passwords = new ArrayList<>();
        String sql = "SELECT password FROM generated_passwords WHERE user_id = ? ORDER BY created_at DESC, id DESC";
        try (Connection conn = openConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    passwords.add(rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to load generated passwords: " + e.getMessage());
        }
        return passwords;
    }

    public static void main(String[] args) {
        //.initialise ();
        //String inputUser = Username_password(users());

        //Connection conn = null;

    }
}

