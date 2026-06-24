package org.example.sumoney.entities;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
@Table(name = "delegation")
@Data
public class Delegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String destination;

    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


}
