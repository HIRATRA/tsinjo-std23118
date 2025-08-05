package hei.school.demo.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DonationForm {
    private String email;
    private String fullName;
    private Double amount;
    private String method;
}
