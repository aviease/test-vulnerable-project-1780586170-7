import sqlite3, os, subprocess, pickle, hashlib, tempfile, yaml, xml.etree.ElementTree as ET
from flask import Flask, request, redirect, render_template_string, send_file, session, make_response

app = Flask(__name__)
app.secret_key = "hardcoded_secret_key_123456"

@app.route('/user')
def get_user():
    username = request.args.get('username')
    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE username = '" + username + "'")
    return str(cursor.fetchall())

@app.route('/search')
def search():
    q = request.args.get('q')
    cursor = sqlite3.connect('db.sqlite').cursor()
    cursor.execute(f"SELECT * FROM items WHERE name LIKE '%{q}%'")
    return str(cursor.fetchall())

@app.route('/exec')
def run_command():
    cmd = request.args.get('cmd')
    os.system(cmd)
    return "done"

@app.route('/run')
def run_proc():
    cmd = request.args.get('cmd')
    result = subprocess.check_output(cmd, shell=True)
    return result

@app.route('/ping')
def ping():
    host = request.args.get('host')
    output = os.popen(f"ping -c 1 {host}").read()
    return output

@app.route('/redirect')
def open_redirect():
    url = request.args.get('url')
    return redirect(url)

@app.route('/upload')
def upload():
    data = request.get_data()
    obj = pickle.loads(data)
    return str(obj)

@app.route('/yaml_load')
def yaml_load():
    data = request.get_data()
    obj = yaml.load(data)
    return str(obj)

@app.route('/template')
def ssti():
    name = request.args.get('name')
    template = f"Hello {name}!"
    return render_template_string(template)

@app.route('/render')
def ssti2():
    tpl = request.args.get('tpl')
    return render_template_string(tpl)

@app.route('/file')
def read_file():
    filename = request.args.get('name')
    with open('/data/' + filename) as f:
        return f.read()

@app.route('/download')
def download():
    path = request.args.get('path')
    return send_file(path)

@app.route('/xml')
def xxe():
    xml_data = request.get_data()
    tree = ET.fromstring(xml_data)
    return ET.tostring(tree).decode()

@app.route('/hash')
def weak_hash():
    data = request.args.get('data')
    return hashlib.md5(data.encode()).hexdigest()

@app.route('/login', methods=['POST'])
def login():
    password = request.form.get('password')
    if password == "admin123":
        session['auth'] = True
        return "ok"
    return "fail"

@app.route('/cookie')
def insecure_cookie():
    resp = make_response("logged in")
    resp.set_cookie('session_id', 'abc123', httponly=False, secure=False)
    return resp

@app.route('/eval')
def eval_code():
    code = request.args.get('code')
    result = eval(code)
    return str(result)

@app.route('/compile')
def compile_code():
    code = request.args.get('code')
    exec(compile(code, '<string>', 'exec'))
    return "done"

@app.route('/ssrf')
def ssrf():
    import urllib.request
    url = request.args.get('url')
    response = urllib.request.urlopen(url)
    return response.read()

@app.route('/write')
def arbitrary_write():
    filename = request.args.get('name')
    content = request.args.get('content')
    with open(f'/tmp/{filename}', 'w') as f:
        f.write(content)
    return "written"

@app.route('/log')
def log_injection():
    user_input = request.args.get('msg')
    app.logger.info(f"User action: {user_input}")
    return "logged"

@app.route('/regex')
def regex_dos():
    import re
    pattern = request.args.get('pattern')
    text = request.args.get('text')
    re.match(pattern, text)
    return "matched"

DB_PASSWORD = "super_secret_db_password"
API_KEY = "sk-proj-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
AWS_SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
