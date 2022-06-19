package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomerRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerRepository.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(CustomerRepository.class);

    private DataSource dataSource;

    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Person createPersonFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String firstName = rs.getString(2);
        String lastName = rs.getString(3);
        String personalNumber = rs.getString(4);
        String address = rs.getString(5);
        return new Person(id, firstName, lastName, personalNumber, address);
    }

    public List<Customer> getCustomers() {
        List<com.zuehlke.securesoftwaredevelopment.domain.Customer> customers = new ArrayList<com.zuehlke.securesoftwaredevelopment.domain.Customer>();
        String query = "SELECT id, username FROM users";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                customers.add(createCustomer(rs));
            }

        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
        return customers;
    }

    private com.zuehlke.securesoftwaredevelopment.domain.Customer createCustomer(ResultSet rs) throws SQLException {
        return new com.zuehlke.securesoftwaredevelopment.domain.Customer(rs.getInt(1), rs.getString(2));
    }

    public List<Restaurant> getRestaurants() {
        List<Restaurant> restaurants = new ArrayList<Restaurant>();
        String query = "SELECT r.id, r.name, r.address, rt.name  FROM restaurant AS r JOIN restaurant_type AS rt ON r.typeId = rt.id ";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            while (rs.next()) {
                restaurants.add(createRestaurant(rs));
            }

        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
        return restaurants;
    }

    private Restaurant createRestaurant(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        String address = rs.getString(3);
        String type = rs.getString(4);
        return new Restaurant(id, name, address, type);
    }


    public Object getRestaurant(String id) {
        String query = "SELECT r.id, r.name, r.address, rt.name  FROM restaurant AS r JOIN restaurant_type AS rt ON r.typeId = rt.id WHERE r.id=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            if (rs.next()) {
                return createRestaurant(rs);
            }

        } catch (SQLException e) {
            LOG.warn("Get restaurant failed for id " + id, e);
        }
        return null;
    }

    public void deleteRestaurant(int id) {
        String query = "DELETE FROM restaurant WHERE id=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            auditLogger.audit("Restaurant id=" + id + " has been deleted");
        } catch (SQLException e) {
            LOG.warn("Delete restaurant failed for id " + id, e);
        }
    }

    public void updateRestaurant(RestaurantUpdate restaurantUpdate) {
        String query = "UPDATE restaurant SET name = '" + restaurantUpdate.getName() + "', address='" + restaurantUpdate.getAddress() + "', typeId =" + restaurantUpdate.getRestaurantType() + " WHERE id =" + restaurantUpdate.getId();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            LOG.debug("Restaurant updated", restaurantUpdate);
        } catch (SQLException e) {
            LOG.warn("update restaurant failed for restaurant id " + restaurantUpdate.getId(), e);
        }

    }

    public Customer getCustomer(String id) {
        String sqlQuery = "SELECT id, username, password FROM users WHERE id=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {

            if (rs.next()) {
                return createCustomerWithPassword(rs);
            }

        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
        return null;
    }

    private Customer createCustomerWithPassword(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String username = rs.getString(2);
        String password = rs.getString(3);
        return new Customer(id, username, password);
    }


    public void deleteCustomer(String id) {
        String query = "DELETE FROM users WHERE id=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            auditLogger.audit("Deleted customer where id=" + id);
        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
    }

    public void updateCustomer(CustomerUpdate customerUpdate) {
        String query = "UPDATE users SET username = '" + customerUpdate.getUsername() + "', password='" + customerUpdate.getPassword() + "' WHERE id =" + customerUpdate.getId();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            LOG.debug("Customer updated", customerUpdate);
        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
    }

    public List<Address> getAddresses(String id) {
        String sqlQuery = "SELECT id, name FROM address WHERE userId=" + id;
        List<Address> addresses = new ArrayList<Address>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {

            while (rs.next()) {
                addresses.add(createAddress(rs));
            }

        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
        return addresses;
    }

    private Address createAddress(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Address(id, name);
    }

    public void deleteCustomerAddress(int id) {
        String query = "DELETE FROM address WHERE id=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            auditLogger.audit("Deleted customer address where id=" + id);
        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
    }

    public void updateCustomerAddress(Address address) {
        String query = "UPDATE address SET name = '" + address.getName() + "' WHERE id =" + address.getId();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            LOG.debug("Address updated", address);
        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
    }

    public void putCustomerAddress(NewAddress newAddress) {
        String query = "INSERT INTO address (name, userId) VALUES ('"+newAddress.getName()+"' , "+newAddress.getUserId()+")";
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(query);
            LOG.debug("New address added " + newAddress.getName()+ " for user ", newAddress.getUserId());
        } catch (SQLException e) {
            LOG.error("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }
    }
}
