#!/bin/bash
# Usage: ./run_middleware.sh <flights-host> <cars-host> <rooms-host>

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <flights-host> <cars-host> <rooms-host>"
    exit 1
fi

./run_rmi.sh > /dev/null 2>&1
java -Djava.rmi.server.codebase=file:$(pwd)/ Server.RMI.RMIMiddleware "$1" "$2" "$3"
