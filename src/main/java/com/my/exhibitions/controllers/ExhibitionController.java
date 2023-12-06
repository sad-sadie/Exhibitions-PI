package com.my.exhibitions.controllers;

import com.my.exhibitions.entities.Exhibition;
import com.my.exhibitions.entities.Order;
import com.my.exhibitions.entities.dto.Payment;
import com.my.exhibitions.services.ExhibitionService;
import com.my.exhibitions.services.HallService;
import com.my.exhibitions.services.OrderService;
import com.my.exhibitions.services.UserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ExhibitionController {


    private static final Logger LOGGER = Logger.getLogger(ExhibitionController.class);
    private final ExhibitionService exhibitionService;
    private final HallService hallService;
    private final UserService userService;
    private final OrderService orderService;

    @Autowired
    public ExhibitionController(ExhibitionService exhibitionService,
                                HallService hallService,
                                UserService userService, OrderService orderService) {
        this.exhibitionService = exhibitionService;
        this.hallService = hallService;
        this.userService = userService;
        this.orderService = orderService;
    }


    @GetMapping("/addExhibition")
    public String getAddExhibition(Model model) {
        LOGGER.info("Get -> /addExhibition");
        model.addAttribute("exhibition", new Exhibition());
        model.addAttribute("halls", hallService.findAll());
        return "addExhibition";
    }

    @PostMapping("/addExhibition")
    public String addNewExhibition(@Valid @ModelAttribute("exhibition") Exhibition exhibition,
                                   BindingResult bindingResult,
                                   @RequestParam(value = "chosenHalls", required = false) List<String> halls,
                                   Model model) {
        LOGGER.info("Post -> /addExhibition");
        boolean alreadyExists = exhibitionService.existsByTheme(exhibition.getTheme());
        if(alreadyExists) {
            bindingResult.rejectValue(
                    "theme",
                    "",
                    "Exhibition with such theme already exists"
            );
        }
        if(bindingResult.hasErrors()
                || exhibition.getStartDate().after(exhibition.getEndDate())
                || halls == null) {
            model.addAttribute("halls", hallService.findAll());
            LOGGER.error("Error while adding exhibitions");
            return "addExhibition";
        }
        exhibitionService.save(exhibition, halls);
        return "redirect:/home";
    }

    @GetMapping("/getExhibitions")
    public String getExhibitions(Model model,
                                 @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                 @RequestParam(value = "sortType", required = false, defaultValue = "default") String sortType,
                                 @RequestParam(value = "canceledExhibitionId", required = false) Optional<Long> canceledExhibitionId) {
        LOGGER.info("Get -> /getExhibition");

        //exhibitionId.ifPresent(exhibitionService::addCustomer);
        canceledExhibitionId.ifPresent(exhibitionService::cancelExhibition);

        Page<Exhibition> page;
        if(sortType.equals("default")) {
            page = exhibitionService.getPage(pageNum - 1);
        } else {
            page = exhibitionService.getPage(pageNum - 1, sortType);
        }
        model.addAttribute("exhibitions", page.toList());
        model.addAttribute("sortType", sortType);
        model.addAttribute("currentPage", pageNum);
        int totalPages = page.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if(principal instanceof UserDetails){
            username = ((UserDetails) principal).getUsername();
        } else {
            username = "";
        }
        model.addAttribute("user", userService.findByUsername(username));
        model.addAttribute("exhService", exhibitionService);

        return "getExhibitions";
    }

    @GetMapping("paymentForm")
    public String getPurchasePage(@RequestParam(value = "id") Optional<Long> id,
                                  Model model) {
        LOGGER.info("Get -> /paymentForm");
        model.addAttribute("payment", new Payment());
        model.addAttribute("exhibitionId", id);
        return "paymentForm";
    }

    @PostMapping("buyTicket")
    public String buyTicket(@Valid @ModelAttribute("payment")Payment payment,
                            BindingResult bindingResult,
                            @RequestParam(value = "exhibitionId") String exhibitionId,
                            Model model) {
        LOGGER.info("Post -> /buyTicket");
        if(bindingResult.hasErrors()) {
            LOGGER.error("Error while buying a ticket");
            return "paymentForm";
        }

        int numberOfTickets = payment.getTicketNumber();
        long exhibition = ExhibitionService.getNumberFromString(exhibitionId).orElseThrow();

        orderService.createTickets(numberOfTickets, exhibition);
        List<Order> orders = orderService.findAll();
        List<Long> tickets = orderService.getTickets(numberOfTickets);
        Exhibition exhibitionEntity = exhibitionService.findById(orders.get(orders.size() - 1).getExhibitionId());

        model.addAttribute("tickets", tickets);
        model.addAttribute("exhibitionName", exhibitionEntity.getTheme());

        return "printTickets";
    }




    @GetMapping("printTicket")
    public String printTicket() {
        LOGGER.info("Get -> /printTicket");
        return "printTickets";
    }


    @GetMapping("/getStats")
    public String getStats(Model model) {
        LOGGER.info("Get -> /getStats");
        Map<Exhibition, Integer> stats = exhibitionService.getStats();
        model.addAttribute("stats", stats);
        return "getStats";
    }

    @GetMapping("/getStats/{theme}")
    public String getDetailedStats(Model model, @PathVariable String theme) {
        LOGGER.info("Get -> /getStats{" + theme + "}");
        Exhibition exhibition = exhibitionService.findByTheme(theme);
        Map<String, Integer> detailedStats = exhibitionService.getDetailedStats(exhibition.getId());
        model.addAttribute("stats", detailedStats);
        model.addAttribute("exhibition", exhibitionService.findById(exhibition.getId()));
        return "getDetailedStats";
    }
}