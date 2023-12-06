package com.my.exhibitions.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "exhibition_sequence")
    @SequenceGenerator(name = "exhibition_sequence", sequenceName = "exhibition_sequence", allocationSize = 1)
    private long id;

    @Column(name = "exhibition_id")
    private long exhibitionId;

    @Column(name = "user_id")
    private long userId;
}