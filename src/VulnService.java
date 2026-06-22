import java.net.*;
import java.io.*;
import java.sql.*;
import java.security.*;
import javax.xml.parsers.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.servlet.http.*;
import org.xml.sax.*;

public class VulnService extends HttpServlet {

    private static final String DB_PASSWORD = "admin123";
    private static final String API_KEY = "AKIAIOSFODNN7EXAMPLE";
    private static final String SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    // SQL injection
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/app", "root", DB_PASSWORD);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id = " + id);
        response.getWriter().print(rs.toString());
    }

    // SQL injection v2
    public ResultSet searchUsers(String name) throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/app", "root", "pass");
        Statement stmt = conn.createStatement();
        return stmt.executeQuery("SELECT * FROM users WHERE name = '" + name + "'");
    }

    // SSRF
    public String fetchUrl(String userUrl) throws Exception {
        URL url = new URL(userUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) result.append(line);
        return result.toString();
    }

    // XXE
    public void parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.parse(new InputSource(new StringReader(xml)));
    }

    // Command injection
    public String runCommand(String input) throws Exception {
        Process process = Runtime.getRuntime().exec("sh -c " + input);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        return reader.readLine();
    }

    // Command injection v2
    public void execCommand(HttpServletRequest request) throws Exception {
        String cmd = request.getParameter("cmd");
        Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
    }

    // Path traversal
    public void readFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filename = request.getParameter("file");
        FileInputStream fis = new FileInputStream("/data/" + filename);
        byte[] data = new byte[1024];
        fis.read(data);
        response.getOutputStream().write(data);
    }

    // XSS
    public void renderPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String name = request.getParameter("name");
        response.getWriter().print("<h1>Hello " + name + "</h1>");
    }

    // XSS v2
    public void showError(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String error = request.getParameter("error");
        response.setContentType("text/html");
        response.getWriter().println("<div class='error'>" + error + "</div>");
    }

    // Insecure deserialization
    public Object deserialize(byte[] data) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return ois.readObject();
    }

    // Insecure deserialization from request
    public void processData(HttpServletRequest request) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
        Object obj = ois.readObject();
    }

    // Weak crypto - DES
    public byte[] encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        SecretKeySpec key = new SecretKeySpec("12345678".getBytes(), "DES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes());
    }

    // Weak crypto - MD5
    public String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(password.getBytes());
        return new String(digest);
    }

    // Weak crypto - SHA1
    public String tokenize(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return new String(md.digest(input.getBytes()));
    }

    // Insecure random
    public String generateToken() {
        java.util.Random random = new java.util.Random();
        return String.valueOf(random.nextLong());
    }

    // Open redirect
    public void redirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String url = request.getParameter("url");
        response.sendRedirect(url);
    }

    // LDAP injection
    public void searchLdap(String username) throws Exception {
        javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext();
        ctx.search("ou=users", "uid=" + username, null);
    }

    // XPath injection
    public void searchXpath(String input) throws Exception {
        javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
        javax.xml.xpath.XPath xpath = factory.newXPath();
        xpath.evaluate("//users[name='" + input + "']", new InputSource(new StringReader("<users/>")));
    }

    // Log injection
    public void logAction(HttpServletRequest request) {
        String user = request.getParameter("user");
        System.out.println("Action by: " + user);
    }

    // Insecure cookie
    public void setCookie(HttpServletResponse response) {
        javax.servlet.http.Cookie cookie = new javax.servlet.http.Cookie("session", "value");
        cookie.setHttpOnly(false);
        cookie.setSecure(false);
        response.addCookie(cookie);
    }

    // Null dereference
    public String processRequest(HttpServletRequest request) {
        String value = request.getParameter("key");
        return value.toUpperCase();
    }

    // Resource leak
    public String readConfig() throws Exception {
        FileInputStream fis = new FileInputStream("/etc/config");
        byte[] data = new byte[1024];
        fis.read(data);
        return new String(data);
    }

    // Trust boundary violation
    public void storeInSession(HttpServletRequest request) {
        String data = request.getParameter("data");
        request.getSession().setAttribute("trusted_data", data);
    }
}
