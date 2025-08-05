package hei.school.demo.repository;

import hei.school.demo.domain.Help;
import hei.school.demo.domain.Beneficiary;
import hei.school.demo.domain.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class HelpRepository {
    private final JdbcTemplate jdbc;

    public HelpRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Help mapRowToHelp(ResultSet rs, int rowNum) throws SQLException {
        Beneficiary beneficiary = Beneficiary.builder()
                .id(rs.getInt("beneficiary_id"))
                .email(rs.getString("beneficiary_email"))
                .fullName(rs.getString("beneficiary_full_name"))
                .build();

        Payment payment = Payment.builder()
                .id(rs.getInt("payment_id"))
                .date(rs.getObject("payment_date", OffsetDateTime.class))
                .amount(rs.getDouble("payment_amount"))
                .method(rs.getString("payment_method"))
                .status(rs.getString("payment_status"))
                .externalId(rs.getString("payment_external_id"))
                .build();

        return Help.builder()
                .id(rs.getInt("help_id"))
                .beneficiary(beneficiary)
                .payment(payment)
                .description(rs.getString("description"))
                .build();
    }

    /**
     * Return all helps with beneficiary and payment info ordered by payment date desc.
     */
    public List<Help> findAllWithDetails() {
        String sql = """
            SELECT h.id AS help_id,
                   b.id AS beneficiary_id, b.email AS beneficiary_email, b.full_name AS beneficiary_full_name,
                   p.id AS payment_id, p.date AS payment_date, p.amount AS payment_amount, p.method AS payment_method, p.status AS payment_status, p.external_id AS payment_external_id,
                   h.description
            FROM help h
            JOIN beneficiary b ON h.beneficiary_id = b.id
            JOIN payment p ON h.payment_id = p.id
            ORDER BY p.date DESC
            """;
        return jdbc.query(sql, this::mapRowToHelp);
    }

    /**
     * Helper to create a help row (not required by exam UI, but useful).
     * Returns generated id.
     */
    public int save(Integer beneficiaryId, Integer paymentId, String description) {
        jdbc.update("INSERT INTO help(beneficiary_id, payment_id, description) VALUES (?,?,?)",
                beneficiaryId, paymentId, description);
        return jdbc.queryForObject("SELECT currval(pg_get_serial_sequence('help','id'))", Integer.class);
    }

    public Optional<Help> findByIdWithDetails(int id) {
        String sql = """
            SELECT h.id AS help_id,
                   b.id AS beneficiary_id, b.email AS beneficiary_email, b.full_name AS beneficiary_full_name,
                   p.id AS payment_id, p.date AS payment_date, p.amount AS payment_amount, p.method AS payment_method, p.status AS payment_status, p.external_id AS payment_external_id,
                   h.description
            FROM help h
            JOIN beneficiary b ON h.beneficiary_id = b.id
            JOIN payment p ON h.payment_id = p.id
            WHERE h.id = ?
            """;
        List<Help> list = jdbc.query(sql, this::mapRowToHelp, id);
        return list.stream().findFirst();
    }
}
