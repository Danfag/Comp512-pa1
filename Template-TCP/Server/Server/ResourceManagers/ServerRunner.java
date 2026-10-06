package Server.ResourceManagers;

import Server.Interface.IResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class ServerRunner implements Runnable {

    private IResourceManager resourceManager;
    private Socket socket;

    public ServerRunner(IResourceManager resourceManager, Socket socket) {
        this.resourceManager = resourceManager;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String request;
            while ((request = in.readLine()) != null) {
                String[] tokens = request.split(",");
                String command = tokens[0].trim().toUpperCase();
                String response = "";

                try {
                    switch (command) {
                        case "ADD_FLIGHT":
                            boolean addFlightRes = resourceManager.addFlight(
                                    Integer.parseInt(tokens[1]),
                                    Integer.parseInt(tokens[2]),
                                    Integer.parseInt(tokens[3])
                            );
                            response = Boolean.toString(addFlightRes);
                            break;

                        case "ADD_CARS":
                            boolean addCarsRes = resourceManager.addCars(
                                    tokens[1],
                                    Integer.parseInt(tokens[2]),
                                    Integer.parseInt(tokens[3])
                            );
                            response = Boolean.toString(addCarsRes);
                            break;

                        case "ADD_ROOMS":
                            boolean addRoomsRes = resourceManager.addRooms(
                                    tokens[1],
                                    Integer.parseInt(tokens[2]),
                                    Integer.parseInt(tokens[3])
                            );
                            response = Boolean.toString(addRoomsRes);
                            break;

                        case "ADD_CUSTOMER":
                            int addCustRes = resourceManager.newCustomer();
                            response = Integer.toString(addCustRes);
                            break;

                        case "ADD_CUSTOMER_ID":
                            boolean addCustIdRes = resourceManager.newCustomer(
                                    Integer.parseInt(tokens[1])
                            );
                            response = Boolean.toString(addCustIdRes);
                            break;

                        case "DELETE_FLIGHT":
                            boolean delFlightRes = resourceManager.deleteFlight(
                                    Integer.parseInt(tokens[1])
                            );
                            response = Boolean.toString(delFlightRes);
                            break;

                        case "DELETE_CARS":
                            boolean delCarsRes = resourceManager.deleteCars(tokens[1]);
                            response = Boolean.toString(delCarsRes);
                            break;

                        case "DELETE_ROOMS":
                            boolean delRoomsRes = resourceManager.deleteRooms(tokens[1]);
                            response = Boolean.toString(delRoomsRes);
                            break;

                        case "DELETE_CUSTOMER":
                            boolean delCustRes = resourceManager.deleteCustomer(
                                    Integer.parseInt(tokens[1])
                            );
                            response = Boolean.toString(delCustRes);
                            break;

                        case "QUERY_FLIGHT":
                            int qFlightRes = resourceManager.queryFlight(
                                    Integer.parseInt(tokens[1])
                            );
                            response = Integer.toString(qFlightRes);
                            break;

                        case "QUERY_CARS":
                            int qCarsRes = resourceManager.queryCars(tokens[1]);
                            response = Integer.toString(qCarsRes);
                            break;

                        case "QUERY_ROOMS":
                            int qRoomsRes = resourceManager.queryRooms(tokens[1]);
                            response = Integer.toString(qRoomsRes);
                            break;

                        case "QUERY_CUSTOMER":
                            String qCustRes = resourceManager.queryCustomerInfo(
                                    Integer.parseInt(tokens[1])
                            );
                            response = qCustRes != null ? qCustRes : "";
                            break;

                        case "QUERY_FLIGHT_PRICE":
                            int qFlightPriceRes = resourceManager.queryFlightPrice(
                                    Integer.parseInt(tokens[1])
                            );
                            response = Integer.toString(qFlightPriceRes);
                            break;

                        case "QUERY_CARS_PRICE":
                            int qCarsPriceRes = resourceManager.queryCarsPrice(tokens[1]);
                            response = Integer.toString(qCarsPriceRes);
                            break;

                        case "QUERY_ROOMS_PRICE":
                            int qRoomsPriceRes = resourceManager.queryRoomsPrice(tokens[1]);
                            response = Integer.toString(qRoomsPriceRes);
                            break;

                        case "RESERVE_FLIGHT":
                            boolean resFlightRes = resourceManager.reserveFlight(
                                    Integer.parseInt(tokens[1]),
                                    Integer.parseInt(tokens[2])
                            );
                            response = Boolean.toString(resFlightRes);
                            break;

                        case "RESERVE_CAR":
                            boolean resCarRes = resourceManager.reserveCar(
                                    Integer.parseInt(tokens[1]),
                                    tokens[2]
                            );
                            response = Boolean.toString(resCarRes);
                            break;

                        case "RESERVE_ROOM":
                            boolean resRoomRes = resourceManager.reserveRoom(
                                    Integer.parseInt(tokens[1]),
                                    tokens[2]
                            );
                            response = Boolean.toString(resRoomRes);
                            break;

                        case "BUNDLE":
                            // Protocol layout for bundle:
                            // BUNDLE,customerID,flight1;flight2;flight3,location,carBoolean,roomBoolean
                            int bundleCustId = Integer.parseInt(tokens[1]);

                            Vector<String> flightList = new Vector<>();
                            if (!tokens[2].isEmpty()) {
                                flightList.addAll(Arrays.asList(tokens[2].split(";")));
                            }

                            String bundleLocation = tokens[3];
                            boolean bundleCar = Boolean.parseBoolean(tokens[4]);
                            boolean bundleRoom = Boolean.parseBoolean(tokens[5]);

                            boolean bundleRes = resourceManager.bundle(
                                    bundleCustId,
                                    flightList,
                                    bundleLocation,
                                    bundleCar,
                                    bundleRoom
                            );
                            response = Boolean.toString(bundleRes);
                            break;

                        case "GET_NAME":
                            response = resourceManager.getName();
                            break;

                        default:
                            response = "ERROR: Unknown command " + command;
                    }
                } catch (Exception e) {
                    response = "ERROR: " + e.getMessage();
                }

                out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error while reading stream: " + e.getMessage());
        }
    }
}