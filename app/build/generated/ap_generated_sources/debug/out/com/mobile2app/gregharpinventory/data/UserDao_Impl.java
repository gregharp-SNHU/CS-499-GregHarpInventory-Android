package com.mobile2app.gregharpinventory.data;

import androidx.annotation.NonNull;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.mobile2app.gregharpinventory.model.User;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class UserDao_Impl implements UserDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<User> __insertAdapterOfUser;

  private final EntityDeleteOrUpdateAdapter<User> __updateAdapterOfUser;

  public UserDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfUser = new EntityInsertAdapter<User>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `users` (`userId`,`username`,`password`,`role`,`phone`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final User entity) {
        statement.bindLong(1, entity.getUserId());
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUsername());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getPassword());
        }
        if (entity.getRole() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getRole());
        }
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getPhone());
        }
      }
    };
    this.__updateAdapterOfUser = new EntityDeleteOrUpdateAdapter<User>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `users` SET `userId` = ?,`username` = ?,`password` = ?,`role` = ?,`phone` = ? WHERE `userId` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final User entity) {
        statement.bindLong(1, entity.getUserId());
        if (entity.getUsername() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getUsername());
        }
        if (entity.getPassword() == null) {
          statement.bindNull(3);
        } else {
          statement.bindText(3, entity.getPassword());
        }
        if (entity.getRole() == null) {
          statement.bindNull(4);
        } else {
          statement.bindText(4, entity.getRole());
        }
        if (entity.getPhone() == null) {
          statement.bindNull(5);
        } else {
          statement.bindText(5, entity.getPhone());
        }
        statement.bindLong(6, entity.getUserId());
      }
    };
  }

  @Override
  public long insertUser(final User user) {
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      return __insertAdapterOfUser.insertAndReturnId(_connection, user);
    });
  }

  @Override
  public void updateUser(final User user) {
    DBUtil.performBlocking(__db, false, true, (_connection) -> {
      __updateAdapterOfUser.handle(_connection, user);
      return null;
    });
  }

  @Override
  public User findByUsername(final String username) {
    final String _sql = "SELECT * FROM users WHERE username = ? LIMIT 1";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (username == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, username);
        }
        final int _columnIndexOfUserId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userId");
        final int _columnIndexOfUsername = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "username");
        final int _columnIndexOfPassword = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "password");
        final int _columnIndexOfRole = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "role");
        final int _columnIndexOfPhone = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "phone");
        final User _result;
        if (_stmt.step()) {
          _result = new User();
          final long _tmpUserId;
          _tmpUserId = _stmt.getLong(_columnIndexOfUserId);
          _result.setUserId(_tmpUserId);
          final String _tmpUsername;
          if (_stmt.isNull(_columnIndexOfUsername)) {
            _tmpUsername = null;
          } else {
            _tmpUsername = _stmt.getText(_columnIndexOfUsername);
          }
          _result.setUsername(_tmpUsername);
          final String _tmpPassword;
          if (_stmt.isNull(_columnIndexOfPassword)) {
            _tmpPassword = null;
          } else {
            _tmpPassword = _stmt.getText(_columnIndexOfPassword);
          }
          _result.setPassword(_tmpPassword);
          final String _tmpRole;
          if (_stmt.isNull(_columnIndexOfRole)) {
            _tmpRole = null;
          } else {
            _tmpRole = _stmt.getText(_columnIndexOfRole);
          }
          _result.setRole(_tmpRole);
          final String _tmpPhone;
          if (_stmt.isNull(_columnIndexOfPhone)) {
            _tmpPhone = null;
          } else {
            _tmpPhone = _stmt.getText(_columnIndexOfPhone);
          }
          _result.setPhone(_tmpPhone);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public User login(final String username, final String password) {
    final String _sql = "SELECT * FROM users WHERE username = ? AND password = ? LIMIT 1";
    return DBUtil.performBlocking(__db, true, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (username == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, username);
        }
        _argIndex = 2;
        if (password == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, password);
        }
        final int _columnIndexOfUserId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "userId");
        final int _columnIndexOfUsername = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "username");
        final int _columnIndexOfPassword = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "password");
        final int _columnIndexOfRole = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "role");
        final int _columnIndexOfPhone = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "phone");
        final User _result;
        if (_stmt.step()) {
          _result = new User();
          final long _tmpUserId;
          _tmpUserId = _stmt.getLong(_columnIndexOfUserId);
          _result.setUserId(_tmpUserId);
          final String _tmpUsername;
          if (_stmt.isNull(_columnIndexOfUsername)) {
            _tmpUsername = null;
          } else {
            _tmpUsername = _stmt.getText(_columnIndexOfUsername);
          }
          _result.setUsername(_tmpUsername);
          final String _tmpPassword;
          if (_stmt.isNull(_columnIndexOfPassword)) {
            _tmpPassword = null;
          } else {
            _tmpPassword = _stmt.getText(_columnIndexOfPassword);
          }
          _result.setPassword(_tmpPassword);
          final String _tmpRole;
          if (_stmt.isNull(_columnIndexOfRole)) {
            _tmpRole = null;
          } else {
            _tmpRole = _stmt.getText(_columnIndexOfRole);
          }
          _result.setRole(_tmpRole);
          final String _tmpPhone;
          if (_stmt.isNull(_columnIndexOfPhone)) {
            _tmpPhone = null;
          } else {
            _tmpPhone = _stmt.getText(_columnIndexOfPhone);
          }
          _result.setPhone(_tmpPhone);
        } else {
          _result = null;
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
