package main

import (
	"crypto/md5"
	"crypto/des"
	"database/sql"
	"encoding/hex"
	"fmt"
	"io/ioutil"
	"math/rand"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"text/template"
)

const (
	AWSAccessKey     = "AKIAIOSFODNN7EXAMPLE"
	AWSSecretKey     = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
	DatabasePassword = "super_secret_password_123"
	APIToken         = "ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
	StripeKey        = "REDACTED_STRIPE_KEY"
	SlackWebhook     = "https://example.invalid/REDACTED_SLACK"
	PrivateKey       = "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEA0Z3VS5JJcds3xfn/ygWyF..."
)

// SQL injection
func getUser(w http.ResponseWriter, r *http.Request) {
	id := r.URL.Query().Get("id")
	db, _ := sql.Open("mysql", "root:password@/app")
	rows, _ := db.Query("SELECT * FROM users WHERE id = " + id)
	defer rows.Close()
	fmt.Fprintf(w, "%v", rows)
}

// SQL injection v2
func searchProducts(w http.ResponseWriter, r *http.Request) {
	name := r.URL.Query().Get("name")
	db, _ := sql.Open("mysql", "root:password@/app")
	db.Query(fmt.Sprintf("SELECT * FROM products WHERE name = '%s'", name))
}

// Command injection
func ping(w http.ResponseWriter, r *http.Request) {
	host := r.URL.Query().Get("host")
	cmd := exec.Command("sh", "-c", "ping -c 1 "+host)
	output, _ := cmd.Output()
	w.Write(output)
}

// Command injection v2
func lookup(w http.ResponseWriter, r *http.Request) {
	domain := r.URL.Query().Get("domain")
	out, _ := exec.Command("bash", "-c", "nslookup "+domain).Output()
	w.Write(out)
}

// Path traversal
func readFile(w http.ResponseWriter, r *http.Request) {
	filename := r.URL.Query().Get("name")
	data, _ := ioutil.ReadFile("/data/" + filename)
	w.Write(data)
}

// Path traversal v2
func serveFile(w http.ResponseWriter, r *http.Request) {
	name := r.URL.Query().Get("file")
	http.ServeFile(w, r, filepath.Join("/uploads", name))
}

// SSRF
func fetch(w http.ResponseWriter, r *http.Request) {
	url := r.URL.Query().Get("url")
	resp, _ := http.Get(url)
	body, _ := ioutil.ReadAll(resp.Body)
	w.Write(body)
}

// XSS
func search(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query().Get("q")
	fmt.Fprintf(w, "<h1>Results for: %s</h1>", q)
}

// Template injection
func render(w http.ResponseWriter, r *http.Request) {
	tpl := r.URL.Query().Get("tpl")
	t, _ := template.New("page").Parse(tpl)
	t.Execute(w, nil)
}

// Open redirect
func redirectHandler(w http.ResponseWriter, r *http.Request) {
	url := r.URL.Query().Get("url")
	http.Redirect(w, r, url, http.StatusFound)
}

// Weak crypto - MD5
func hashPassword(password string) string {
	h := md5.New()
	h.Write([]byte(password))
	return hex.EncodeToString(h.Sum(nil))
}

// Weak crypto - DES
func encryptData(data []byte) ([]byte, error) {
	block, _ := des.NewCipher([]byte("12345678"))
	encrypted := make([]byte, len(data))
	block.Encrypt(encrypted, data)
	return encrypted, nil
}

// Insecure random
func generateToken() string {
	return fmt.Sprintf("%d", rand.Int63())
}

// Arbitrary file write
func writeFile(w http.ResponseWriter, r *http.Request) {
	filename := r.URL.Query().Get("name")
	content := r.URL.Query().Get("content")
	os.WriteFile("/tmp/"+filename, []byte(content), 0777)
	w.Write([]byte("ok"))
}

// Insecure file permissions
func createConfig() {
	os.WriteFile("/etc/app/config.json", []byte("{}"), 0777)
}

// Log injection
func logAction(w http.ResponseWriter, r *http.Request) {
	user := r.URL.Query().Get("user")
	fmt.Printf("User %s performed action\n", user)
	w.Write([]byte("logged"))
}

func main() {
	http.HandleFunc("/user", getUser)
	http.HandleFunc("/products", searchProducts)
	http.HandleFunc("/ping", ping)
	http.HandleFunc("/lookup", lookup)
	http.HandleFunc("/file", readFile)
	http.HandleFunc("/serve", serveFile)
	http.HandleFunc("/fetch", fetch)
	http.HandleFunc("/search", search)
	http.HandleFunc("/render", render)
	http.HandleFunc("/redirect", redirectHandler)
	http.HandleFunc("/write", writeFile)
	http.HandleFunc("/log", logAction)
	http.ListenAndServe(":8080", nil)
}
