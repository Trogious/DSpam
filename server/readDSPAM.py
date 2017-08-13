#!/usr/local/bin/python3.5

import json
import socket
import ssl

host = 'swmud.net' 
port = 3000
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


def getJsonResponse(entries, requestId):
	jsonObj = {}
	jsonObj['jsonrpc'] = '2.0'
	jsonObj['id'] = requestId
	jsonObj['result'] = { 'entries': entries }
	return jsonObj


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
	return jsonObj

def getEntries():
	entries = []
	with open('/sith/twelman/twelman@swmud.net.log','rb') as f:
		line = f.readline()
		str = ''
		i = 0
		while line is not None and len(line) > 0 and i < 500:
			if len(line) > 1:
				entry = process(decode(line))
				entries.append(entry)
			line = f.readline()
			i = i + 1
	return entries

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
s.bind((host,port)) 
s.listen(backlog) 
historyEntries = getEntries()[::-1]
while True:  
	client, address = s.accept()
	print('accepted: ' + str(address))
	try:
		sslClient = ssl.wrap_socket(client, server_side=True, certfile="server.pem", keyfile="server.key", ssl_version=ssl.PROTOCOL_TLSv1_2,cert_reqs=ssl.CERT_REQUIRED,ca_certs='ca.pem')
	except ssl.SSLError as e:
		print('no client cert: ' + e.strerror)
		client.close()
		continue
	client = None
	data = sslClient.recv(size) 
	while data and len(data) > 0:
		print(data)
		data = data.decode('utf8')
		print(data)
		req = json.loads(data)
		keys = req.keys()
		if 'jsonrpc' in keys and req['jsonrpc'] == '2.0' and 'method' in keys and 'id' in keys:
			method = req['method']
			if 'retrain' == method:
				if 'params' in keys and 'entries' in req['params']:
					entries = req['params']['entries']
					if len(entries) > 0:
						for entry in entries:
							print('entry: %s' % (entry['signature']))
			elif 'get_entries' == method:
				response = json.dumps(getJsonResponse(historyEntries, req['id']))
				encodedResponse = response.encode('utf8')
				respLen = len(encodedResponse)
				fullContent = 'Content-Length: ' + str(respLen) + "\r\n\r\n" + response
				sslClient.sendall(fullContent.encode('utf8'))
				print(fullContent)
		print('recv')
		data = sslClient.recv(size) 
	sslClient.close()
	print('client closed')


