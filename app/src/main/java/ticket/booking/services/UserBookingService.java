package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.utils.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserBookingService {

    private User user;

    private List<User> userList;

    private static  final ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDB/Users.json";

    // Load users from File
    private void loadUserListFromFile() throws  IOException  {
        File users = new File(USERS_PATH);

        userList = objectMapper.readValue(users, new TypeReference<List<User>>() {});
    }

    public UserBookingService() throws  IOException {
        loadUserListFromFile();
    }

    public UserBookingService(User usr) throws IOException {
        this.user = usr;
        loadUserListFromFile();
    }


    // User Log-in check
    public Boolean loginUser() {
        Optional<User> userFound = userList.stream().filter(user1 -> {
            return user1.getName().equalsIgnoreCase(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();

        return userFound.isPresent();
    }



    // User Registration or Signing - Up User
    public Boolean signUp(User user1) {
        try {
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    /*
    * JSON -> Object : Deserialize
    * Object -> JSON : Serialize
    * */
    private void saveUserListToFile() throws IOException {
        File savedUser = new File(USERS_PATH);
        objectMapper.writeValue(savedUser, userList);
    }

    // Fetch bookings of user
    public void fetchBookings() {
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equalsIgnoreCase(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        userFetched.ifPresent(User::printTickets);
    }


    // Cancel booking of user
    public Boolean cancelBooking() throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the ticketId to cancel: ");
        String ticketId = sc.nextLine();

        if(ticketId == null) {
            System.out.println("Ticket Id cannot be null or empty");
            return Boolean.FALSE;
        }


        String finalTicketId1 = ticketId;  //Because strings are immutable
        boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(finalTicketId1));

        if(removed) {
            saveUserListToFile();
            System.out.println("Ticket Cancelled Successfully");
            return Boolean.TRUE;
        } else {
            System.out.println("Ticket Cancellation Unsuccessful");
            return Boolean.FALSE;
        }
    }


    public List<Train> getTrains(String source, String destination) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(source, destination);
        } catch (Exception e) {
            return null;
        }
    }


    public List<List<Integer>> fetchSeats(Train train) {
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int col) throws IOException {
        if(row < 0 || col < 0 || row >= train.getSeats().size() || col >= train.getSeats().get(0).size()) {
            System.out.println("Invalid seat selection");
            return Boolean.FALSE;
        }

        if(train.getSeats().get(row).get(col) == 1) {
            System.out.println("Seat already booked, Please select another seat");
            return Boolean.FALSE;
        }

        // Mark the seat as booked
        train.getSeats().get(row).set(col, 1);

        // Create a new Ticket and add to user's bookings
        String ticketId = "TICKET-" + System.currentTimeMillis();
        String userId = user.getUserId();
        String source = train.getStations().get(0); // Assuming source is the first station
        String destination = train.getStations().get(train.getStations().size() - 1); // Assuming destination is the last station
        java.util.Date dateOfTravel = new java.util.Date(); // Assuming current date for simplicity

        ticket.booking.entities.Ticket newTicket = new ticket.booking.entities.Ticket(ticketId, userId, source, destination, dateOfTravel, train);
        user.getTicketsBooked().add(newTicket);

        // Update the user in userList
        for(int i=0; i<userList.size(); i++) {
            if(userList.get(i).getUserId().equals(user.getUserId())) {
                userList.set(i, user);
                break;
            }
        }

        // Save the updated user list to file
        saveUserListToFile();

        System.out.println("Seat booked successfully with Ticket ID: " + ticketId);
        return Boolean.TRUE;
    }
}
