#!/usr/local/bin/python3.5

import json
import socket
import ssl

host = 'your.domain.net' 
port = 3010
backlog = 5 
size = 4096


def decode(str):
	decoded = str
	encodings = ['utf8','iso8859-2','cp1250','cp437','cp500','cp1252']
	for e in encodings:
		try:
			decoded = str.decode(e)
			break
		except UnicodeDecodeError:
			print('UnicodeDecodeError: ' + e)
			print(str)
			pass
	return decoded


def process(line):
	data = line.split('\t')
	jsonObj = {}
	jsonObj['timestamp'] = data[0]
	jsonObj['spamstatus'] = data[1]
	jsonObj['from'] = data[2]
	jsonObj['signature'] = data[3]
	jsonObj['subject'] = data[4]
	jsonObj['status'] = data[5]
	jsonObj['msgid'] = data[6]
	jsonstr = json.dumps(jsonObj) + ','
	return jsonstr

def readLog():
	str = ''
	with open('/dspamlogdir/you@your.domain.net.log','rb') as f:
		line = f.readline()
		str = ''
		i = 0
		while line is not None and len(line) > 0 and i < 500:
			if len(line) > 1:
				str = process(decode(line)) + str
			line = f.readline()
			i = i + 1
		str = "{'dspam':[" + str + ']}'
	return str

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((host,port)) 
s.listen(backlog) 
logs = readLog()
while True:  
	client, address = s.accept()
	print('accepted: ' + str(address))
	try:
		sslClient = ssl.wrap_socket(client, server_side=True, certfile="server.pem", keyfile="server.key", ssl_version=ssl.PROTOCOL_TLSv1_2,cert_reqs=ssl.CERT_REQUIRED,ca_certs='ca.pem')
	except ssl.SSLError as e:
		print('no client cert: ' + e.strerror)
		continue
	client = None
	data = sslClient.recv(size) 
	while data and len(data) > 0:
		print(data)
		data = data.decode('utf8')
		print(data)
		req = json.loads(data)
		if 'dspam' in req.keys():
			encodedLogs = logs.encode('utf8')
			logsLen = len(encodedLogs)
			fullContent = 'Content-Length: ' + str(logsLen) + "\r\n\r\n" + logs
			sslClient.sendall(fullContent.encode('utf8'))
			print(fullContent)
		print('recv')
		data = sslClient.recv(size) 
	sslClient.close()
	print('client closed')


