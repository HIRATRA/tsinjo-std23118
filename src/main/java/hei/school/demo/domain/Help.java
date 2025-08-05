package hei.school.demo.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Help {
    private Integer id;
    private Beneficiary beneficiary;
    private Payment payment;
    private String description;
}
