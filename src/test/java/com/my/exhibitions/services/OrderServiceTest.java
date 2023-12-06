package com.my.exhibitions.services;

import com.my.exhibitions.entities.Exhibition;
import com.my.exhibitions.entities.Order;
import com.my.exhibitions.entities.User;
import com.my.exhibitions.repositories.ExhibitionRepository;
import com.my.exhibitions.repositories.OrderRepository;
import com.my.exhibitions.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.my.exhibitions.entities.enums.Role.USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @MockBean
    OrderRepository orderRepository;
    @InjectMocks
    OrderService orderService;

    @MockBean
    ExhibitionRepository exhibitionRepository;
    @InjectMocks
    ExhibitionService exhibitionService;

    @InjectMocks
    UserService userService;
    @MockBean
    UserRepository userRepository;

    Order order;


    @Autowired
    public OrderServiceTest(OrderService orderService) {
        this.orderService = orderService;
    }

    @BeforeEach
    public void setUp() {
        order = new Order();
        order.setId(1000L);
        //order.setTheme("themE");
    }
    @Test
    public void testFindAll() {
        Order order1 = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(order, order1));
        List<Order> orders = orderService.findAll();
        verify(orderRepository).findAll();
        assertThat(orders).isEqualTo(List.of(order, order1));
    }
    @Test
    public void testFindAllNull() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<Order> orders = orderService.findAll();

        verify(orderRepository).findAll();
        assertThat(orders).isEqualTo(Collections.emptyList());
    }
    @Test
    public void getTickets1() {
        Order order1 = new Order();
        order1.setId(1001L);
        when(orderRepository.findAll()).thenReturn(List.of(order, order1));
        List<Long> orders = orderService.getTickets(1);
        verify(orderRepository).findAll();
        assertThat(orders).isEqualTo(List.of(order1.getId()));
    }
    @Test
    public void getTickets2() {
        Order order1 = new Order();
        order1.setId(1001L);
        when(orderRepository.findAll()).thenReturn(List.of(order, order1));
        List<Long> orders = orderService.getTickets(2);
        verify(orderRepository).findAll();
        assertThat(orders).isEqualTo(List.of(order.getId(), order1.getId()));
    }
    @Test
    public void buyTicket() {

        User user = new User();
        user.setId(10000L);
        user.setUsername("sasha");
        user.setPassword("123");
        user.setRole(USER);
        user.setExhibitions(new ArrayList<>());
        userRepository.save(user);

        Exhibition exhibition = new Exhibition();
        exhibition.setId(1000L);
        exhibition.setTheme("themE");
        exhibition.setUsers(new ArrayList<>());
        exhibitionRepository.save(exhibition);
        when(exhibitionRepository.findById(exhibition.getId())).thenReturn(exhibition);
        orderService.createTicket(exhibition.getId(), user);

        List<Exhibition> exhibitions = user.getExhibitions();
        List<User> users = exhibition.getUsers();

        assertThat(exhibitions).isNotEmpty();
        assertThat(users).isNotEmpty();
    }
}
