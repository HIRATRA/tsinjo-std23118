package hei.school.demo.repository;

import hei.school.demo.domain.Donation;
import hei.school.demo.domain.Donor;
import hei.school.demo.domain.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class DonationRepository {
    private final JdbcTemplate jdbc;
    private final DonorRepository donorRepo;
    private final PaymentRepository paymentRepo;

    public DonationRepository(JdbcTemplate jdbc, DonorRepository donorRepo, PaymentRepository paymentRepo) {
        this.jdbc = jdbc;
        this.donorRepo = donorRepo;
        this.paymentRepo = paymentRepo;
    }

    /**
     * Insert a donation linking donor_id and payment_id.
     * Returns generated donation id.
     */
    public int save(Integer donorId, Integer paymentId) {
        jdbc.update("INSERT INTO donation(donor_id, payment_id) VALUES (?, ?)", donorId, paymentId);
        return jdbc.queryForObject("SELECT currval(pg_get_serial_sequence('donation','id'))", Integer.class);
    }

    private Donation mapRowToDonation(ResultSet rs, int rowNum) throws SQLException {
        // Build Donor
        Donor donor = Donor.builder()
                .id(rs.getInt("donor_id"))
                .email(rs.getString("donor_email"))
                .fullName(rs.getString("donor_full_name"))
                .build();

        // Build Payment
        Payment payment = Payment.builder()
                .id(rs.getInt("payment_id"))
                .date(rs.getObject("payment_date", OffsetDateTime.class))
                .amount(rs.getDouble("payment_amount"))
                .method(rs.getString("payment_method"))
                .status(rs.getString("payment_status"))
                .externalId(rs.getString("payment_external_id"))
                .build();

        return Donation.builder()
                .id(rs.getInt("donation_id"))
                .donor(donor)
                .payment(payment)
                .build();
    }

    /**
     * Return all donations with donor and payment details ordered anti-chronological by payment date.
     */
    public List<Donation> findAllWithDetails() {
        String sql = """
            SELECT d.id AS donation_id,
                   donor.id AS donor_id, donor.email AS donor_email, donor.full_name AS donor_full_name,
                   p.id AS payment_id, p.date AS payment_date, p.amount AS payment_amount, p.method AS payment_method, p.status AS payment_status, p.external_id AS payment_external_id
            FROM donation d
            JOIN donor ON d.donor_id = donor.id
            JOIN payment p ON d.payment_id = p.id
            ORDER BY p.date DESC
            """;
        return jdbc.query(sql, this::mapRowToDonation);
    }

    /**
     * Find donation by donation id with details.
     */
    public Optional<Donation> findByIdWithDetails(int donationId) {
        String sql = """
            SELECT d.id AS donation_id,
                   donor.id AS donor_id, donor.email AS donor_email, donor.full_name AS donor_full_name,
                   p.id AS payment_id, p.date AS payment_date, p.amount AS payment_amount, p.method AS payment_method, p.status AS payment_status, p.external_id AS payment_external_id
            FROM donation d
            JOIN donor ON d.donor_id = donor.id
            JOIN payment p ON d.payment_id = p.id
            WHERE d.id = ?
            """;
        List<Donation> list = jdbc.query(sql, this::mapRowToDonation, donationId);
        return list.stream().findFirst();
    }
}
