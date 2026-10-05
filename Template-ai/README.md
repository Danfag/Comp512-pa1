# COMP 512 Programming Assignment 1 - RMI Distribution

The RMI version is distributed as:

```
Client -> Middleware -> Flights ResourceManager
                     -> Cars ResourceManager
                     -> Rooms ResourceManager
```

Customers are replicated at the three ResourceManagers. Each RM stores only the
reservations for the resource type it owns. The Middleware combines the three
customer bills and coordinates customer deletion and bundle reservations.

## Compile

```bash
cd Server
make

cd ../Client
make
```

## Run on one machine

Open five terminals from `Template/Server` / `Template/Client`:

```bash
# Server terminal 1
./run_server.sh Flights

# Server terminal 2
./run_server.sh Cars

# Server terminal 3
./run_server.sh Rooms

# Server terminal 4
./run_middleware.sh localhost localhost localhost

# Client terminal (from Template/Client)
./run_client.sh localhost Middleware
```

## Run on different machines

Start each ResourceManager on its machine with the corresponding RMI name:
`Flights`, `Cars`, or `Rooms`. Then start the Middleware with the three hostnames:

```bash
./run_middleware.sh <flights-host> <cars-host> <rooms-host>
```

Finally point the unchanged client at the Middleware machine:

```bash
./run_client.sh <middleware-host> Middleware
```

`run_servers.sh` can also be configured with four course-machine hostnames to
launch the three RMs and Middleware using tmux/ssh.
