# ./run_rmi.sh > /dev/null

echo "Edit file run_middleware.sh to include instructions for launching the middleware"
echo '  $1 - hostname of Flights'
echo '  $2 - hostname of Cars'
echo '  $3 - hostname of Rooms'

java -Djava.rmi.server.codebase=file:$(pwd)/ Middleware.TCPMiddleware $1 $2 $3 $4 $5 $6 $7 $8 $9
