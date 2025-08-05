package hei.school.demo.repository;

import hei.school.demo.domain.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;

import java.time.OffsetDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Payment> mapper = (rs, i) ->
            Payment.builder()
                    .id(rs.getInt("id"))
                    .date(rs.getObject("date", OffsetDateTime.class))
                    .amount(rs.getDouble("amount"))
                    .method(rs.getString("method"))
                    .status(rs.getString("status"))
                    .externalId(rs.getString("external_id"))
                    .build();

    public PaymentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Payment save(Payment p) {
        jdbc.update("INSERT INTO payment(date, amount, method, status, external_id) VALUES (?,?,?,?,?)",
                Timestamp.from(p.getDate().toInstant()), p.getAmount(), p.getMethod(), p.getStatus(), p.getExternalId());
        Integer id = jdbc.queryForObject("SELECT currval(pg_get_serial_sequence('payment','id'))", Integer.class);
        p.setId(id);
        return p;
    }

    public void updateStatus(Integer id, String status){
        jdbc.update("UPDATE payment SET status = ? WHERE id = ?", status, id);
    }

    // Méthode ajoutée pour mettre à jour external_id
    public void updateExternalId(Integer id, String externalId) {
        jdbc.update("UPDATE payment SET external_id = ? WHERE id = ?", externalId, id);
    }

    public Optional<Payment> findByExternalId(String externalId){
        List<Payment> list = jdbc.query("SELECT * FROM payment WHERE external_id = ?", mapper, externalId);
        return list.stream().findFirst();
    }

    public List<Payment> findAll(){ return jdbc.query("SELECT * FROM payment ORDER BY date DESC", mapper); }

    // --- Méthodes ajoutées pour PaymentPollingService ---

    /**
     * Retourne la liste des ids des payments ayant le status fourni.
     */
    public List<Integer> findIdsByStatus(String status) {
        return jdbc.queryForList("SELECT id FROM payment WHERE status = ?", Integer.class, status);
    }

    /**
     * Trouve un Payment par son id.
     */
    public Optional<Payment> findById(Integer id) {
        List<Payment> list = jdbc.query("SELECT * FROM payment WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }
}
