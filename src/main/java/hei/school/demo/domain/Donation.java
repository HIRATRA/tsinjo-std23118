package hei.school.demo.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Donation {
    private Integer id;
    private Donor donor;
    private Payment payment;
}
