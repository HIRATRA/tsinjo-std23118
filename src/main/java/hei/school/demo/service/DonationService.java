package hei.school.demo.service;

import hei.school.demo.domain.Donation;
import hei.school.demo.domain.Donor;
import hei.school.demo.domain.Payment;
import hei.school.demo.repository.DonationRepository;
import hei.school.demo.repository.DonorRepository;
import hei.school.demo.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class DonationService {
    private static final Logger logger = LoggerFactory.getLogger(DonationService.class);

    private final DonorRepository donorRepo;
    private final PaymentRepository paymentRepo;
    private final DonationRepository donationRepo;
    private final WebClient vola;

    public DonationService(DonorRepository donorRepo,
                           PaymentRepository paymentRepo,
                           DonationRepository donationRepo,
                           WebClient vola) {
        this.donorRepo = donorRepo;
        this.paymentRepo = paymentRepo;
        this.donationRepo = donationRepo;
        this.vola = vola;
    }

    /**
     * Crée un donor, crée un payment local en status VERIFYING, appelle Vola pour créer le paiement
     * (on tente de récupérer l'externalId), met à jour le payment local, crée la donation et renvoie
     * l'objet Donation complet.
     */
    @Transactional
    public Donation submitDonation(String email, String fullName, double amount, String method) {
        logger.info("Soumission don: {} <{}> montant={} via {}", fullName, email, amount, method);

        // 1) create donor (or insert)
        Donor donor = Donor.builder()
                .email(email)
                .fullName(fullName)
                .build();
        donorRepo.save(donor);
        logger.debug("Donor saved id={}", donor.getId());

        // 2) create local payment with VERIFYING status
        Payment p = Payment.builder()
                .date(OffsetDateTime.now())
                .amount(amount)
                .method(method)
                .status("VERIFYING")
                .build();
        paymentRepo.save(p);
        logger.debug("Payment local created id={} status={}", p.getId(), p.getStatus());

        // 3) call Vola to create external payment
        Map<String, Object> body = Map.of(
                "amount", amount,
                "method", method,
                "payer", Map.of("email", email, "name", fullName)
        );

        String externalId = null;
        try {
            // On bloque ici pour récupérer l'external id rapidement (ok pour examen).
            externalId = vola.post()
                    .uri("/payments")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(m -> m.get("id") != null ? String.valueOf(m.get("id")) : null)
                    .block();
            logger.info("Vola returned externalId={}", externalId);
        } catch (Exception e) {
            logger.warn("Erreur appel Vola (création payment) : {}", e.getMessage());
            // keep externalId null, le Payment restera en VERIFYING et le polling mettra à jour ensuite
        }

        // 4) update payment with externalId if present
        if (externalId != null) {
            paymentRepo.updateExternalId(p.getId(), externalId);
            // update in-memory object too
            p.setExternalId(externalId);
        } else {
            logger.debug("Aucun externalId reçu, payment restera en VERIFYING");
        }

        // 5) create donation link
        int donationId = donationRepo.save(donor.getId(), p.getId());
        logger.info("Donation created id={} donor_id={} payment_id={}", donationId, donor.getId(), p.getId());

        // 6) fetch and return created donation with details
        Optional<Donation> created = donationRepo.findByIdWithDetails(donationId);
        return created.orElseThrow(() -> {
            logger.error("Impossible de récupérer la donation id={}", donationId);
            return new IllegalStateException("Donation created but not found id=" + donationId);
        });
    }
}
