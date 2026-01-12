package pl.barbershopproject.barbershop.utils;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
        * Utility class providing static factory methods for creating test entities (User, Offer, Order, GuestOrder).
        * Useful for building entities with minimal boilerplate in JPA/Spring Data test scenarios.
        */

public class TestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private TestEntities() {}

    /**
     * Creates a User instance for testing purposes.
     *
     * @param firstname the user's first name
     * @param lastname  the user's last name
     * @param email     the user's email address
     * @param role      the user's role
     * @return new User instance
     */
    public static User createUser(String firstname, String lastname, String email, Role role) {

        return User.builder()
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password("passwd")
                .role(role)
                .build();

    }

    /**
     * Creates an Offer instance for testing purposes.
     *
     * @param kind the type/kind of the offer
     * @param cost the cost of the offer
     * @return new Offer instance
     */

    public static Offer createOffer(String kind, BigDecimal cost){

        Offer offer = new Offer();
        offer.setKind(kind);
        offer.setCost(cost);
        return offer;
    }

    /**
     * Creates an Order instance for testing purposes.
     *
     * @param user      the user placing the order
     * @param offer     the offer associated with the order
     * @param orderDate the date the order was placed
     * @param visitDate the date the visit is scheduled
     * @param status    the order status
     * @return new Order instance
     */
    public static Order createOrder(User user, Offer offer, LocalDateTime orderDate, LocalDateTime visitDate, Status status){
        Order order = new Order();
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(orderDate);
        order.setVisitDate(visitDate);
        order.setStatus(status);
        return order;
    }

    /**
     * Creates a GuestOrder instance for testing purposes.
     *
     * @param firstname  guest's first name
     * @param lastname   guest's last name
     * @param phonenumber guest's phone number
     * @param email      guest's email address
     * @param offer      offer associated with the guest order
     * @param orderDate  date the guest order was placed
     * @param visitDate  scheduled visit date
     * @param status     guest order status
     * @return new GuestOrder instance
     */
    public static GuestOrder createGuestOrder(String firstname, String lastname, String phonenumber,
                                   String email, Offer offer, LocalDateTime orderDate,
                                   LocalDateTime visitDate, Status status){

        GuestOrder guestOrder = new GuestOrder();
        guestOrder.setFirstname(firstname);
        guestOrder.setLastname(lastname);
        guestOrder.setPhonenumber(phonenumber);
        guestOrder.setEmail(email);
        guestOrder.setOffer(offer);
        guestOrder.setOrderDate(orderDate);
        guestOrder.setVisitDate(visitDate);
        guestOrder.setStatus(status);
        return guestOrder;

    }
}
