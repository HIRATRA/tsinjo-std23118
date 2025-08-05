package hei.school.demo.domain;

import lombok.*;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Payment {
    private Integer id;
    private OffsetDateTime date;
    private Double amount;
    private String method;
    private String status; // VERIFYING, SUCCEEDED, FAILED
    private String externalId; // id returned by Vola
}
