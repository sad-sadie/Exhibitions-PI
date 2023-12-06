package com.my.exhibitions.services;

import com.my.exhibitions.entities.Exhibition;
import com.my.exhibitions.entities.Order;
import com.my.exhibitions.entities.User;
import com.my.exhibitions.repositories.OrderRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class OrderService {
    private final static Logger LOGGER = Logger.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ExhibitionService exhibitionService;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserService userService, ExhibitionService exhibitionService) {
        this.orderRepository = orderRepository;
        this.userService = userService;
        this.exhibitionService = exhibitionService;
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }
    public List<Long> getTickets(int numberOfTickets) {
        List<Order> orders = this.findAll();
        return orders.subList(orders.size() - numberOfTickets, orders.size())
                .stream()
                .map(Order::getId)
                .collect(Collectors.toList());
    }
    public void createTickets(int numberOfTickets, long exhibition) {
        User currentUser = this.getCurrentUser();
        for(int i = 0; i < numberOfTickets; ++i) {
            this.createTicket(exhibition,currentUser);
        }
    }
    public void createTicket(long exhibitionId, User currentUser) {
        LOGGER.info("Confirming ticket purchase for " + currentUser.getUsername() + " for exhibition with id " + exhibitionId);
        Exhibition exhibition = exhibitionService.findById(exhibitionId);
        List<Exhibition> exhibitions = currentUser.getExhibitions();
        exhibitions.add(exhibition);

        List<User> users = exhibition.getUsers();
        users.add(currentUser);

        exhibitionService.save(exhibition);
        userService.update(currentUser);
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails)principal).getUsername();
        return userService.findByUsername(username);
    }
}
