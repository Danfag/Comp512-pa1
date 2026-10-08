#Usage: ./run_server.sh [<rmi_name>]

./run_rmi.sh > /dev/null 2>&1
# Pass in server name and port
java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.Main $1 $2
