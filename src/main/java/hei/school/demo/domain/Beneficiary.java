package hei.school.demo.domain;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Beneficiary {
    private Integer id;
    private String email;
    private String fullName;
}
