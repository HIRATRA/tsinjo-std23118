package hei.school.demo.repository;

import hei.school.demo.domain.Beneficiary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BeneficiaryRepository {

    private final JdbcTemplate jdbc;
    private final RowMapper<Beneficiary> mapper = (rs, rowNum) ->
            Beneficiary.builder()
                    .id(rs.getInt("id"))
                    .email(rs.getString("email"))
                    .fullName(rs.getString("full_name"))
                    .build();

    public BeneficiaryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Save a new beneficiary and set its generated id on the object.
     */
    public Beneficiary save(Beneficiary b) {
        jdbc.update("INSERT INTO beneficiary(email, full_name) VALUES (?, ?)",
                b.getEmail(), b.getFullName());
        Integer id = jdbc.queryForObject("SELECT currval(pg_get_serial_sequence('beneficiary','id'))", Integer.class);
        b.setId(id);
        return b;
    }

    /**
     * Find by email.
     */
    public Optional<Beneficiary> findByEmail(String email) {
        List<Beneficiary> list = jdbc.query("SELECT * FROM beneficiary WHERE email = ?", mapper, email);
        return list.stream().findFirst();
    }

    public Optional<Beneficiary> findById(int id) {
        List<Beneficiary> list = jdbc.query("SELECT * FROM beneficiary WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public List<Beneficiary> findAll() {
        return jdbc.query("SELECT * FROM beneficiary ORDER BY id DESC", mapper);
    }
}
