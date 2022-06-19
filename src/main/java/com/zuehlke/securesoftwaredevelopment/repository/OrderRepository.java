package com.zuehlke.securesoftwaredevelopment.repository;

import com.zuehlke.securesoftwaredevelopment.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepository {

    private DataSource dataSource;

    private static final Logger LOG = LoggerFactory.getLogger(OrderRepository.class);

    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public List<Food> getMenu(int id) {
        List<Food> menu = new ArrayList<>();
        String sqlQuery = "SELECT id, name FROM food WHERE restaurantId=" + id;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {
            while (rs.next()) {
                menu.add(createFood(rs));
            }

        } catch (SQLException e) {
            LOG.warn("Get menu failed for id " + id, e);
        }

        return menu;
    }

    private Food createFood(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Food(id, name);
    }

    public void insertNewOrder(NewOrder newOrder, int userId) {
        LocalDate date = LocalDate.now();
        String sqlQuery = "INSERT INTO delivery (isDone, userId, restaurantId, addressId, date, comment) values (FALSE, ?, ?, ?, ?, ?)";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
            Statement statement = connection.createStatement();
        ) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, newOrder.getRestaurantId());
            preparedStatement.setInt(3, newOrder.getAddress());
            preparedStatement.setString(4, date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth());
            preparedStatement.setString(5, newOrder.getComment());

            preparedStatement.executeUpdate();
            LOG.info("New order created! Order: restaurant id=" + newOrder.getRestaurantId() + ", address=" + newOrder.getAddress() + ", date=" + date.getYear() + "-" + date.getMonthValue() + "-" + date.getDayOfMonth() + ", comment=" + newOrder.getComment());

            sqlQuery = "SELECT MAX(id) FROM delivery";
            ResultSet rs = statement.executeQuery(sqlQuery);

            if (rs.next()) {

                int deliveryId = rs.getInt(1);
                sqlQuery = "INSERT INTO delivery_item (amount, foodId, deliveryId)" +
                        "values";
                for (int i = 0; i < newOrder.getItems().length; i++) {
                    FoodItem item = newOrder.getItems()[i];
                    String deliveryItem = "";
                    if (i > 0) {
                        deliveryItem = ",";
                    }
                    deliveryItem += "(" + item.getAmount() + ", " + item.getFoodId() + ", " + deliveryId + ")";
                    sqlQuery += deliveryItem;
                }
                LOG.debug("SQL query being executed: " + sqlQuery);
                statement.executeUpdate(sqlQuery);
            }

        } catch (SQLException e) {
            LOG.warn("An exception with the error code " + e.getErrorCode() + " occurred." ,e);
        }


    }

    public Object getAddresses(int userId) {
        List<Address> addresses = new ArrayList<>();
        String sqlQuery = "SELECT id, name FROM address WHERE userId=" + userId;
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sqlQuery)) {
            while (rs.next()) {
                addresses.add(createAddress(rs));
            }

        } catch (SQLException e) {
            LOG.warn("Get address failed for user " + userId, e);
        }
        return addresses;
    }

    private Address createAddress(ResultSet rs) throws SQLException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        return new Address(id, name);

    }
}
