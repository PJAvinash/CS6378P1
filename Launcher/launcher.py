import argparse
import subprocess
import re
import socket
from multiprocessing import Process

#netID
netID = 'jxp220032'

# hostname
currentHost = socket.gethostname()

# Define argument parser to accept config file
parser = argparse.ArgumentParser()
parser.add_argument("config_file", help="Path to configuration file")
args = parser.parse_args()

# Open and read the config file
with open(args.config_file, "r") as f:
    lines = f.readlines()

hosts = {}
# Loop through the lines and extract the hostname and port number using regex
for line in lines:
    match = re.match(r'^\d+ (\S+) (\d+)$', line)
    if match:
        hostname = match.group(1)
        port = int(match.group(2))
        hosts[hostname] = port

subprocess.run('cd .. && javac *.java', shell=True)
# Loop through each host and SSH into it to execute command
remotehosts = list(hosts.keys())
for host in remotehosts:
    if currentHost == host:
        command = f"java Main Launcher/{args.config_file}"
    else:
        command = f"ssh -o 'StrictHostKeyChecking=no' {netID}@{host} 'cd CS6378/P1/CS6378P1 && java Main Launcher/{args.config_file}'"
    print(command)
    process = subprocess.Popen(command, shell=True)
    process.wait()