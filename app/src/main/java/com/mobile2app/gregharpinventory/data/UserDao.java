package com.mobile2app.gregharpinventory.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.mobile2app.gregharpinventory.model.User;

@Dao
public interface UserDao {
    // method to insert user into the database
    @Insert
    long insertUser(User user);

    // method to update user entry in the database
    @Update
    void updateUser(User user);

    // method to find user by username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User findByUsername(String username);

    // method to validate a login using the database
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);
}
