package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.HashedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class HashedUserRepository {

    private static final Logger LOG = LoggerFactory.getLogger(HashedUserRepository.class);

    private final DataSource dataSource;

    public HashedUserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HashedUser findUser(String username) {
        String sqlQuery = "select passwordHash, salt, totpKey from hashedUsers where username = '" + username + "'";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {
            if (rs.next()) {
                String passwordHash = rs.getString(1);
                String salt = rs.getString(2);
                String totpKey = rs.getString(3);
                return new HashedUser(username, passwordHash, salt, totpKey);
            }
        } catch (SQLException e) {
            LOG.warn("User search failed for searchTerm " + username, e);
        }
        return null;
    }

    public void saveTotpKey(String username, String totpKey) {
        String sqlQuery = "update hashedUsers set totpKey = ? where username = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
            statement.setString(1, totpKey);
            statement.setString(2, username);

            statement.executeUpdate();
            LOG.debug("TOTP key saved for username ", username);
        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
    }
}
