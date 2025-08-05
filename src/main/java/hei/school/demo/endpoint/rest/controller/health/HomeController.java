package hei.school.demo.endpoint.rest.controller.health;


import hei.school.demo.dto.DonationForm;
import hei.school.demo.repository.DonationRepository;
import hei.school.demo.repository.HelpRepository;
import hei.school.demo.service.DonationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {
    private final DonationRepository donationRepo;
    private final HelpRepository helpRepo;
    private final DonationService donationService;

    public HomeController(DonationRepository donationRepo, HelpRepository helpRepo, DonationService donationService) {
        this.donationRepo = donationRepo;
        this.helpRepo = helpRepo;
        this.donationService = donationService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("donations", donationRepo.findAllWithDetails());
        model.addAttribute("helps", helpRepo.findAllWithDetails());
        model.addAttribute("donationForm", new DonationForm());
        return "index";
    }

    @PostMapping("/donate")
    public String donate(@ModelAttribute DonationForm form) {
        donationService.submitDonation(form.getEmail(), form.getFullName(), form.getAmount(), form.getMethod());
        return "redirect:/";
    }
}

