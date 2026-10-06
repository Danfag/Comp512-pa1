// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.*;

import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public class ResourceManager implements IResourceManager
{
	protected String m_name = "";

	// STATIC: Ensures all instances and subclasses share the exact same hashmap storage
	protected static RMHashMap m_data = new RMHashMap();

	public ResourceManager(String p_name)
	{
		m_name = p_name;
	}

	// Reads a data item
	protected RMItem readData(String key)
	{
		synchronized(m_data) {
			RMItem item = m_data.get(key);
			if (item != null) {
				return (RMItem)item.clone();
			}
			return null;
		}
	}

	// Writes a data item
	protected void writeData(String key, RMItem value)
	{
		synchronized(m_data) {
			m_data.put(key, value);
		}
	}

	// Remove the item out of storage
	protected void removeData(String key)
	{
		synchronized(m_data) {
			m_data.remove(key);
		}
	}

	// Deletes the item
	protected boolean deleteItem(String key)
	{
		Trace.info("RM::deleteItem(" + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(key);
		if (curObj == null)
		{
			Trace.warn("RM::deleteItem(" + key + ") failed--item doesn't exist");
			return false;
		}
		else
		{
			if (curObj.getReserved() == 0)
			{
				removeData(curObj.getKey());
				Trace.info("RM::deleteItem(" + key + ") item deleted");
				return true;
			}
			else
			{
				Trace.info("RM::deleteItem(" + key + ") item can't be deleted because some customers have reserved it");
				return false;
			}
		}
	}

	// Query the number of available seats/rooms/cars
	protected int queryNum(String key)
	{
		Trace.info("RM::queryNum(" + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(key);
		int value = 0;
		if (curObj != null)
		{
			value = curObj.getCount();
		}
		Trace.info("RM::queryNum(" + key + ") returns count=" + value);
		return value;
	}

	// Query the price of an item
	protected int queryPrice(String key)
	{
		Trace.info("RM::queryPrice(" + key + ") called");
		ReservableItem curObj = (ReservableItem)readData(key);
		int value = 0;
		if (curObj != null)
		{
			value = curObj.getPrice();
		}
		Trace.info("RM::queryPrice(" + key + ") returns cost=$" + value);
		return value;
	}

	// Reserve an item
	protected boolean reserveItem(int customerID, String key, String location)
	{
		Trace.info("RM::reserveItem(customer=" + customerID + ", " + key + ", " + location + ") called" );
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
			return false;
		}

		ReservableItem item = (ReservableItem)readData(key);
		if (item == null)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
			return false;
		}
		else if (item.getCount() == 0)
		{
			Trace.warn("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") failed--No more items");
			return false;
		}
		else
		{
			customer.reserve(key, location, item.getPrice());
			writeData(customer.getKey(), customer);

			item.setCount(item.getCount() - 1);
			item.setReserved(item.getReserved() + 1);
			writeData(item.getKey(), item);

			Trace.info("RM::reserveItem(" + customerID + ", " + key + ", " + location + ") succeeded");
			return true;
		}
	}

	public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException
	{
		Trace.info("RM::addFlight(" + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
		Flight curObj = (Flight)readData(Flight.getKey(flightNum));
		if (curObj == null)
		{
			Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
			writeData(newObj.getKey(), newObj);
			Trace.info("RM::addFlight() created new flight " + flightNum);
		}
		else
		{
			curObj.setCount(curObj.getCount() + flightSeats);
			if (flightPrice > 0) { curObj.setPrice(flightPrice); }
			writeData(curObj.getKey(), curObj);
			Trace.info("RM::addFlight() modified existing flight " + flightNum);
		}
		return true;
	}

	public boolean addCars(String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addCars(" + location + ", " + count + ", $" + price + ") called");
		Car curObj = (Car)readData(Car.getKey(location));
		if (curObj == null)
		{
			Car newObj = new Car(location, count, price);
			writeData(newObj.getKey(), newObj);
			Trace.info("RM::addCars() created new location " + location);
		}
		else
		{
			curObj.setCount(curObj.getCount() + count);
			if (price > 0) { curObj.setPrice(price); }
			writeData(curObj.getKey(), curObj);
			Trace.info("RM::addCars() modified existing location " + location);
		}
		return true;
	}

	public boolean addRooms(String location, int count, int price) throws RemoteException
	{
		Trace.info("RM::addRooms(" + location + ", " + count + ", $" + price + ") called");
		Room curObj = (Room)readData(Room.getKey(location));
		if (curObj == null)
		{
			Room newObj = new Room(location, count, price);
			writeData(newObj.getKey(), newObj);
			Trace.info("RM::addRooms() created new room location " + location);
		} else {
			curObj.setCount(curObj.getCount() + count);
			if (price > 0) { curObj.setPrice(price); }
			writeData(curObj.getKey(), curObj);
			Trace.info("RM::addRooms() modified existing location " + location);
		}
		return true;
	}

	public boolean deleteFlight(int flightNum) throws RemoteException { return deleteItem(Flight.getKey(flightNum)); }
	public boolean deleteCars(String location) throws RemoteException { return deleteItem(Car.getKey(location)); }
	public boolean deleteRooms(String location) throws RemoteException { return deleteItem(Room.getKey(location)); }

	public int queryFlight(int flightNum) throws RemoteException { return queryNum(Flight.getKey(flightNum)); }
	public int queryCars(String location) throws RemoteException { return queryNum(Car.getKey(location)); }
	public int queryRooms(String location) throws RemoteException { return queryNum(Room.getKey(location)); }

	public int queryFlightPrice(int flightNum) throws RemoteException { return queryPrice(Flight.getKey(flightNum)); }
	public int queryCarsPrice(String location) throws RemoteException { return queryPrice(Car.getKey(location)); }
	public int queryRoomsPrice(String location) throws RemoteException { return queryPrice(Room.getKey(location)); }

	public String queryCustomerInfo(int customerID) throws RemoteException
	{
		Trace.info("RM::queryCustomerInfo(" + customerID + ") called");
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null) { return ""; }
		System.out.println(customer.getBill());
		return customer.getBill();
	}

	public int newCustomer() throws RemoteException
	{
		int cid = Integer.parseInt(String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
				String.valueOf(Math.round(Math.random() * 100 + 1)));
		Customer customer = new Customer(cid);
		writeData(customer.getKey(), customer);
		return cid;
	}

	public boolean newCustomer(int customerID) throws RemoteException
	{
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null)
		{
			customer = new Customer(customerID);
			writeData(customer.getKey(), customer);
			return true;
		}
		return false;
	}

	public boolean deleteCustomer(int customerID) throws RemoteException
	{
		Customer customer = (Customer)readData(Customer.getKey(customerID));
		if (customer == null) { return false; }

		RMHashMap reservations = customer.getReservations();
		for (String reservedKey : reservations.keySet())
		{
			ReservedItem reserveditem = customer.getReservedItem(reservedKey);
			ReservableItem item = (ReservableItem)readData(reserveditem.getKey());
			item.setReserved(item.getReserved() - reserveditem.getCount());
			item.setCount(item.getCount() + reserveditem.getCount());
			writeData(item.getKey(), item);
		}

		removeData(customer.getKey());
		return true;
	}

	public boolean reserveFlight(int customerID, int flightNum) throws RemoteException
	{
		return reserveItem(customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
	}

	public boolean reserveCar(int customerID, String location) throws RemoteException
	{
		return reserveItem(customerID, Car.getKey(location), location);
	}

	public boolean reserveRoom(int customerID, String location) throws RemoteException
	{
		return reserveItem(customerID, Room.getKey(location), location);
	}

	public boolean bundle(int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
	{
		return false;
	}

	public String getName() throws RemoteException { return m_name; }
}