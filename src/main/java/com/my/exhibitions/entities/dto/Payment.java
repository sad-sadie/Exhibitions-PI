package com.my.exhibitions.entities.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.validation.constraints.*;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Date cannot be empty")
    @Future(message = "Your card is expired")
    private Date date;

    @Max(value = 50, message = "You cannot buy more than 50 tickets")
    private int ticketNumber;

    @Pattern(regexp = "\\b(?:\\d[ -]*?){13,16}\\b", message = "Invalid card number")
    private String cardNumber;

    @Pattern(regexp ="\\b\\d{3,4}\\b", message = "Invalid card CVC")
    private String CVC;

}