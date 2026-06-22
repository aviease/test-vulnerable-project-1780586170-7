<?php
// SQL injection
function getUser($id) {
    $conn = new mysqli("localhost", "root", "password", "app");
    $result = $conn->query("SELECT * FROM users WHERE id = " . $_GET['id']);
    return $result->fetch_all();
}

// SQL injection v2
function searchProducts() {
    $name = $_GET['name'];
    $pdo = new PDO("mysql:host=localhost;dbname=app", "root", "password");
    $stmt = $pdo->query("SELECT * FROM products WHERE name = '$name'");
    return $stmt->fetchAll();
}

// Command injection
function ping() {
    $host = $_GET['host'];
    system("ping -c 1 " . $host);
}

// Command injection v2
function execCmd() {
    $cmd = $_POST['cmd'];
    exec($cmd, $output);
    return implode("\n", $output);
}

// Command injection v3
function processFile() {
    $filename = $_GET['file'];
    passthru("cat " . $filename);
}

// XSS
function showSearch() {
    $q = $_GET['q'];
    echo "<h1>Results for: $q</h1>";
}

// XSS v2
function displayError() {
    $error = $_GET['error'];
    echo "<div class='alert'>$error</div>";
}

// Path traversal
function readFile() {
    $file = $_GET['file'];
    return file_get_contents("/data/" . $file);
}

// Path traversal v2
function downloadFile() {
    $path = $_GET['path'];
    readfile($path);
}

// File inclusion
function loadTemplate() {
    $page = $_GET['page'];
    include($page);
}

// File inclusion v2
function loadModule() {
    $module = $_GET['module'];
    require_once("modules/" . $module . ".php");
}

// SSRF
function fetchUrl() {
    $url = $_GET['url'];
    return file_get_contents($url);
}

// SSRF v2
function proxyRequest() {
    $target = $_GET['target'];
    $ch = curl_init($target);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    return curl_exec($ch);
}

// Insecure deserialization
function processData() {
    $data = $_POST['data'];
    $obj = unserialize($data);
    return $obj;
}

// XXE
function parseXml() {
    $xml = file_get_contents("php://input");
    $doc = simplexml_load_string($xml, 'SimpleXMLElement', LIBXML_NOENT);
    return $doc->asXML();
}

// Open redirect
function redirect() {
    $url = $_GET['url'];
    header("Location: " . $url);
}

// Weak crypto
function hashPassword($password) {
    return md5($password);
}

// Weak crypto v2
function generateToken() {
    return sha1(rand());
}

// Insecure random
function createSession() {
    return bin2hex(rand());
}

// Hardcoded credentials
define('DB_PASSWORD', 'admin123');
define('API_KEY', 'sk-proj-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX');
define('AWS_SECRET', 'wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY');
define('STRIPE_SECRET', 'REDACTED_STRIPE_KEY');

// LDAP injection
function searchLdap() {
    $username = $_GET['user'];
    $ds = ldap_connect("localhost");
    ldap_search($ds, "ou=users,dc=example,dc=com", "uid=" . $username);
}

// Log injection
function logActivity() {
    $action = $_GET['action'];
    error_log("User action: " . $action);
}

// Information exposure
function handleError($e) {
    echo "<pre>" . $e->getMessage() . "\n" . $e->getTraceAsString() . "</pre>";
}

// Insecure cookie
function setSession() {
    setcookie("session_id", "abc123", 0, "/", "", false, false);
}

// eval
function calculate() {
    $expr = $_GET['expr'];
    eval('$result = ' . $expr . ';');
    return $result;
}

// preg_replace code execution
function processRegex() {
    $pattern = $_GET['pattern'];
    $input = $_GET['input'];
    preg_replace($pattern, '', $input);
}
?>
