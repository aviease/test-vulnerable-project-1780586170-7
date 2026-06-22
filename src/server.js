const express = require('express');
const fs = require('fs');
const path = require('path');
const { exec, execSync } = require('child_process');
const crypto = require('crypto');
const http = require('http');
const mysql = require('mysql');
const vm = require('vm');

const app = express();
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// XSS - reflected
app.get('/search', (req, res) => {
  const query = req.query.q;
  res.send(`<h1>Results for: ${query}</h1>`);
});

// XSS - stored
app.post('/comment', (req, res) => {
  const comment = req.body.comment;
  res.send(`<div class="comment">${comment}</div>`);
});

// XSS - DOM
app.get('/profile', (req, res) => {
  const name = req.query.name;
  res.send(`<script>document.write('Hello ' + '${name}')</script>`);
});

// Path traversal
app.get('/file', (req, res) => {
  const filename = req.query.name;
  const content = fs.readFileSync('/data/' + filename);
  res.send(content);
});

// Path traversal v2
app.get('/download', (req, res) => {
  const filePath = req.query.path;
  res.sendFile(filePath);
});

// Command injection
app.get('/ping', (req, res) => {
  const host = req.query.host;
  exec(`ping -c 1 ${host}`, (err, stdout) => res.send(stdout));
});

// Command injection v2
app.get('/dns', (req, res) => {
  const domain = req.query.domain;
  const result = execSync('nslookup ' + domain);
  res.send(result);
});

// Code injection via eval
app.get('/eval', (req, res) => {
  const code = req.query.code;
  const result = eval(code);
  res.json({ result });
});

// Code injection via vm
app.get('/sandbox', (req, res) => {
  const code = req.query.code;
  const sandbox = { result: null };
  vm.runInNewContext(code, sandbox);
  res.json(sandbox);
});

// Code injection via Function constructor
app.get('/func', (req, res) => {
  const body = req.query.body;
  const fn = new Function('return ' + body);
  res.json({ result: fn() });
});

// SQL injection
app.get('/user', (req, res) => {
  const id = req.query.id;
  const conn = mysql.createConnection({ host: 'localhost', user: 'root', database: 'app' });
  conn.query("SELECT * FROM users WHERE id = " + id, (err, results) => {
    res.json(results);
  });
});

// SQL injection v2
app.get('/products', (req, res) => {
  const category = req.query.cat;
  const conn = mysql.createConnection({ host: 'localhost', user: 'root', database: 'app' });
  conn.query(`SELECT * FROM products WHERE category = '${category}'`, (err, results) => {
    res.json(results);
  });
});

// SSRF
app.get('/fetch', (req, res) => {
  const url = req.query.url;
  http.get(url, (response) => {
    let data = '';
    response.on('data', chunk => data += chunk);
    response.on('end', () => res.send(data));
  });
});

// SSRF v2
app.get('/proxy', (req, res) => {
  const target = req.query.target;
  const fetch = require('node-fetch');
  fetch(target).then(r => r.text()).then(body => res.send(body));
});

// Open redirect
app.get('/redirect', (req, res) => {
  const url = req.query.url;
  res.redirect(url);
});

// Insecure deserialization
app.post('/deserialize', (req, res) => {
  const serialize = require('serialize-javascript');
  const data = req.body.data;
  const obj = eval('(' + data + ')');
  res.json(obj);
});

// Hardcoded credentials
const DB_PASSWORD = 'super_secret_password_123';
const API_KEY = 'sk-proj-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX';
const JWT_SECRET = 'my_jwt_secret_never_change';
const STRIPE_KEY = 'REDACTED_STRIPE_KEY';

// Weak crypto
app.get('/token', (req, res) => {
  const token = crypto.createHash('md5').update(req.query.user).digest('hex');
  res.json({ token });
});

// Insecure random
app.get('/session', (req, res) => {
  const sessionId = Math.random().toString(36).substring(2);
  res.cookie('session', sessionId, { httpOnly: false, secure: false });
  res.send('ok');
});

// Prototype pollution
app.post('/config', (req, res) => {
  const config = {};
  const userInput = req.body;
  Object.keys(userInput).forEach(key => {
    config[key] = userInput[key];
  });
  res.json(config);
});

// Regex DoS
app.get('/validate', (req, res) => {
  const email = req.query.email;
  const regex = /^([a-zA-Z0-9_\.\-])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
  res.json({ valid: regex.test(email) });
});

// Information exposure
app.use((err, req, res, next) => {
  res.status(500).json({ error: err.message, stack: err.stack });
});

// Arbitrary file write
app.post('/upload', (req, res) => {
  const filename = req.body.filename;
  const content = req.body.content;
  fs.writeFileSync('/uploads/' + filename, content);
  res.send('uploaded');
});

// XML external entity
app.post('/xml', (req, res) => {
  const libxmljs = require('libxmljs');
  const doc = libxmljs.parseXml(req.body.xml, { noent: true, dtdload: true });
  res.send(doc.toString());
});

// Log injection
app.get('/action', (req, res) => {
  const user = req.query.user;
  console.log(`User ${user} performed action`);
  res.send('ok');
});

// Denial of service - uncontrolled resource consumption
app.post('/parse', (req, res) => {
  const data = JSON.parse(req.body.data);
  res.json(data);
});

app.listen(3000);
