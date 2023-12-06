package com.my.exhibitions.services;

import com.my.exhibitions.entities.Exhibition;
import com.my.exhibitions.entities.User;
import com.my.exhibitions.repositories.ExhibitionRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExhibitionService {

    private final static Logger LOGGER = Logger.getLogger(ExhibitionService.class);
    private static final int DEFAULT_PAGE_SIZE = 5;

    private final ExhibitionRepository exhibitionRepository;
    private final HallService hallService;
    private final UserService userService;

    @Autowired
    public ExhibitionService(ExhibitionRepository exhibitionRepository, HallService hallService, UserService userService) {
        this.exhibitionRepository = exhibitionRepository;
        this.hallService = hallService;
        this.userService = userService;
    }

    public void save(Exhibition exhibition, List<String> halls) {
        LOGGER.info("Saving exhibition " + exhibition.getTheme());
        exhibition.setHalls(halls.stream().map(Long::parseLong).map(hallService::findById).collect(Collectors.toSet()));
        save(exhibition);
    }

    public void save(Exhibition exhibition) {
        LOGGER.info("Saving exhibition " + exhibition.getTheme());
        exhibitionRepository.save(exhibition);
    }


    public Exhibition findById(long id) {
        LOGGER.info("Getting exhibition with id " + id);
        return exhibitionRepository.findById(id);
    }

    public Page<Exhibition> getPage(int pageNum, String sortType) {
        LOGGER.info("Getting exhibitions on page with number " + pageNum + ", sorted by " + sortType);
        Pageable paging = PageRequest.of(pageNum, DEFAULT_PAGE_SIZE, Sort.by(sortType));
        return exhibitionRepository.findAll(paging);
    }

    public Page<Exhibition> getPage(int pageNum) {
        LOGGER.info("Getting exhibitions on page with number " + pageNum);
        Pageable paging = PageRequest.of(pageNum, DEFAULT_PAGE_SIZE);
        return exhibitionRepository.findAll(paging);
    }

    public void cancelExhibition(long id) {
        LOGGER.info("Cancelling exhibition with id " + id);
        Exhibition exhibition = exhibitionRepository.findById(id);
        exhibition.removeHalls();
        exhibition.removeUsers();
        exhibitionRepository.deleteById(id);
    }



    public Map<Exhibition, Integer> getStats() {
        LOGGER.info("Getting exhibitions' stats on page");
        Map<Exhibition, Integer> stats = new LinkedHashMap<>();
        List<Exhibition> exhibitions = exhibitionRepository.findAll();
        exhibitions.forEach(exhibition -> {
            int ticketsSold = exhibitionRepository.getNumberOfTicketsSoldByID(exhibition.getId());
            stats.put(exhibition, ticketsSold);
        });
        return stats;
    }

    public int getNumberOfTicketsBoughtForUserAtExhibition(User user, Exhibition exhibition) {
        if(user == null) {
            return 0;
        }
        LOGGER.info("Getting number of tickets by " + user.getUsername() + " for " + exhibition.getTheme());
        return exhibitionRepository.getNumberOfTicketsBoughtBySingleUser(exhibition.getId(), user.getId());
    }

    public Map<String, Integer> getDetailedStats(long exhibitionId) {
        LOGGER.info("Getting detailed stats for exhibition " + exhibitionId);
        Map<String, Integer> stats = new LinkedHashMap<>();
        List<User> users = userService.findAll();
        users.forEach(user -> stats.put(
                user.getUsername(),
                exhibitionRepository.getNumberOfTicketsBoughtBySingleUser(exhibitionId, user.getId())
        ));
        return stats.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public boolean existsByTheme(String theme) {
        LOGGER.info("Checking if the exhibition " + theme + " exists");
        return exhibitionRepository.existsByTheme(theme);
    }

    public Exhibition findByTheme(String theme) {
        LOGGER.info("Getting the exhibition " + theme + " exists");
        return exhibitionRepository.findByTheme(theme);
    }

    public static Optional<Long> getNumberFromString(String input) {
        Pattern pattern = Pattern.compile("\\[([0-9]+)\\]");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            String numberString = matcher.group(1);
            try {
                long number = Long.parseLong(numberString);
                return Optional.of(number);
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}