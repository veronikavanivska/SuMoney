package org.example.sumoney.dto.requests;


import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDelegationRequest {

    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
}