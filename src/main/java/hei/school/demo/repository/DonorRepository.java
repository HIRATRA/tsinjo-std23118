package hei.school.demo.repository;

import hei.school.demo.domain.Donor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;
import java.util.List;
import java.util.Optional;

@Repository
public class DonorRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Donor> mapper = (rs, i) ->
            Donor.builder()
                    .id(rs.getInt("id"))
                    .email(rs.getString("email"))
                    .fullName(rs.getString("full_name"))
                    .build();

    public DonorRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Donor save(Donor d) {
        jdbc.update("INSERT INTO donor(email, full_name) VALUES (?, ?)",
                d.getEmail(), d.getFullName());
        Integer id = jdbc.queryForObject("SELECT currval(pg_get_serial_sequence('donor','id'))", Integer.class);
        d.setId(id);
        return d;
    }

    public Optional<Donor> findById(int id){
        List<Donor> list = jdbc.query("SELECT * FROM donor WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    public List<Donor> findAll(){ return jdbc.query("SELECT * FROM donor ORDER BY id DESC", mapper); }
}
