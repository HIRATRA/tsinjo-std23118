package hei.school.demo.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import hei.school.demo.repository.PaymentRepository;
import java.util.List;
import java.util.Map;

@Service
public class PaymentPollingService {
    private final PaymentRepository paymentRepo;
    private final WebClient vola;
    private final String apiKey;

    public PaymentPollingService(PaymentRepository paymentRepo, WebClient vola, org.springframework.core.env.Environment env) {
        this.paymentRepo = paymentRepo;
        this.vola = vola;
        this.apiKey = env.getProperty("tsinjo.vola.api.key");
    }

    // toutes les 20s : vérifier les paiements en VERIFYING
    @Scheduled(fixedDelay = 20_000)
    public void pollPendingPayments() {
        List<Integer> pending = paymentRepo.findIdsByStatus("VERIFYING");
        for (Integer pid : pending) {
            // get external id
            var opt = paymentRepo.findById(pid);
            if (opt.isEmpty() || opt.get().getExternalId()==null) continue;
            String externalId = opt.get().getExternalId();
            try {
                Map response = vola.get()
                        .uri(uriBuilder -> uriBuilder.path("/payments/{id}").build(externalId))
                        .header("Authorization", "Bearer " + apiKey)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block();
                String state = (String) response.get("state"); // SUCCEEDED or FAILED etc.
                if ("SUCCEEDED".equalsIgnoreCase(state)) {
                    paymentRepo.updateStatus(pid, "SUCCEEDED");
                } else if ("FAILED".equalsIgnoreCase(state)) {
                    paymentRepo.updateStatus(pid, "FAILED");
                }
            } catch (Exception e) {
                // log and continue
            }
        }
    }
}
