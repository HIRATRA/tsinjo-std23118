package hei.school.demo.service;

import hei.school.demo.domain.Beneficiary;
import hei.school.demo.domain.Help;
import hei.school.demo.domain.Payment;
import hei.school.demo.repository.BeneficiaryRepository;
import hei.school.demo.repository.HelpRepository;
import hei.school.demo.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HelpService {

    private static final Logger logger = LoggerFactory.getLogger(HelpService.class);

    private final HelpRepository helpRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final PaymentRepository paymentRepository;

    public HelpService(HelpRepository helpRepository,
                       BeneficiaryRepository beneficiaryRepository,
                       PaymentRepository paymentRepository) {
        this.helpRepository = helpRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Retourne toutes les aides avec leurs détails (beneficiary + payment),
     * ordonnées anti-chronologiquement par date de paiement (défini dans le repository).
     */
    public List<Help> findAllHelps() {
        logger.debug("Récupération de toutes les aides avec détails");
        return helpRepository.findAllWithDetails();
    }

    /**
     * Trouve une aide par son id (avec détails).
     */
    public Optional<Help> findHelpById(int id) {
        logger.debug("Recherche de l'aide id={}", id);
        return helpRepository.findByIdWithDetails(id);
    }

    /**
     * Crée une aide complète : (1) crée ou récupère le beneficiary, (2) crée le payment,
     * (3) crée la ligne help. Tout est fait dans une transaction.
     *
     * Cette méthode est utile pour insérer des aides depuis des scripts ou tests.
     *
     * @param beneficiaryEmail email du bénéficiaire
     * @param beneficiaryName  nom complet du bénéficiaire
     * @param amount           montant de l'aide
     * @param method           moyen de paiement (ex: TRANSFER, BANK, CASH)
     * @param description      description de l'accident / motif de l'aide
     * @return l'objet Help créé avec ses relations (id généré)
     */
    @Transactional
    public Help createHelp(String beneficiaryEmail,
                           String beneficiaryName,
                           double amount,
                           String method,
                           String description) {
        logger.info("Création d'une aide pour {} ({}), montant={} via {}, description='{}'",
                beneficiaryName, beneficiaryEmail, amount, method, description);

        // 1) Trouver ou créer le beneficiary
        Beneficiary beneficiary = beneficiaryRepository.findByEmail(beneficiaryEmail)
                .orElseGet(() -> {
                    Beneficiary b = Beneficiary.builder()
                            .email(beneficiaryEmail)
                            .fullName(beneficiaryName)
                            .build();
                    beneficiaryRepository.save(b);
                    logger.info("Beneficiary créé id={} email={}", b.getId(), b.getEmail());
                    return b;
                });

        // 2) Créer le payment (pour une aide on suppose qu'il est SUCCEEDED quand on l'insère)
        Payment payment = Payment.builder()
                .date(OffsetDateTime.now())
                .amount(amount)
                .method(method)
                .status("SUCCEEDED") // car l'aide est inscrite en sortie effective
                .build();
        paymentRepository.save(payment);
        logger.info("Payment créé id={} amount={} status={}", payment.getId(), payment.getAmount(), payment.getStatus());

        // 3) Créer la ligne help
        int helpId = helpRepository.save(beneficiary.getId(), payment.getId(), description);
        // récupérer l'objet complet pour retourner
        Help created = helpRepository.findByIdWithDetails(helpId)
                .orElseThrow(() -> {
                    logger.error("Impossible de récupérer l'aide nouvellement créée id={}", helpId);
                    return new IllegalStateException("Help created but not found id=" + helpId);
                });

        logger.info("Aide créée id={} pour beneficiary_id={}", created.getId(), beneficiary.getId());
        return created;
    }

}
