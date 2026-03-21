package com.mobile2app.gregharpinventory.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeleteOrUpdateAdapter;
import androidx.room.EntityInsertAdapter;
import androidx.room.RoomDatabase;
import androidx.room.util.DBUtil;
import androidx.room.util.SQLiteConnectionUtil;
import androidx.room.util.SQLiteStatementUtil;
import androidx.sqlite.SQLiteStatement;
import com.mobile2app.gregharpinventory.model.InventoryItem;
import com.mobile2app.gregharpinventory.model.ReportRow;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation", "removal"})
public final class ItemDao_Impl implements ItemDao {
  private final RoomDatabase __db;

  private final EntityInsertAdapter<InventoryItem> __insertAdapterOfInventoryItem;

  private final EntityDeleteOrUpdateAdapter<InventoryItem> __deleteAdapterOfInventoryItem;

  private final EntityDeleteOrUpdateAdapter<InventoryItem> __updateAdapterOfInventoryItem;

  public ItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertAdapterOfInventoryItem = new EntityInsertAdapter<InventoryItem>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `items` (`itemId`,`itemName`,`itemQuantity`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final InventoryItem entity) {
        statement.bindLong(1, entity.getItemId());
        if (entity.getItemName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getItemName());
        }
        statement.bindLong(3, entity.getItemQuantity());
      }
    };
    this.__deleteAdapterOfInventoryItem = new EntityDeleteOrUpdateAdapter<InventoryItem>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `items` WHERE `itemId` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final InventoryItem entity) {
        statement.bindLong(1, entity.getItemId());
      }
    };
    this.__updateAdapterOfInventoryItem = new EntityDeleteOrUpdateAdapter<InventoryItem>() {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `items` SET `itemId` = ?,`itemName` = ?,`itemQuantity` = ? WHERE `itemId` = ?";
      }

      @Override
      protected void bind(@NonNull final SQLiteStatement statement, final InventoryItem entity) {
        statement.bindLong(1, entity.getItemId());
        if (entity.getItemName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindText(2, entity.getItemName());
        }
        statement.bindLong(3, entity.getItemQuantity());
        statement.bindLong(4, entity.getItemId());
      }
    };
  }

  @Override
  public long insert(final InventoryItem item) {
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      return __insertAdapterOfInventoryItem.insertAndReturnId(_connection, item);
    });
  }

  @Override
  public int delete(final InventoryItem item) {
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __deleteAdapterOfInventoryItem.handle(_connection, item);
      return _result;
    });
  }

  @Override
  public int update(final InventoryItem item) {
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      int _result = 0;
      _result += __updateAdapterOfInventoryItem.handle(_connection, item);
      return _result;
    });
  }

  @Override
  public LiveData<List<InventoryItem>> getAllLive() {
    final String _sql = "SELECT * FROM items ORDER BY itemName COLLATE NOCASE";
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfItemId = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "itemId");
        final int _columnIndexOfItemName = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "itemName");
        final int _columnIndexOfItemQuantity = SQLiteStatementUtil.getColumnIndexOrThrow(_stmt, "itemQuantity");
        final List<InventoryItem> _result = new ArrayList<InventoryItem>();
        while (_stmt.step()) {
          final InventoryItem _item;
          _item = new InventoryItem();
          final long _tmpItemId;
          _tmpItemId = _stmt.getLong(_columnIndexOfItemId);
          _item.setItemId(_tmpItemId);
          final String _tmpItemName;
          if (_stmt.isNull(_columnIndexOfItemName)) {
            _tmpItemName = null;
          } else {
            _tmpItemName = _stmt.getText(_columnIndexOfItemName);
          }
          _item.setItemName(_tmpItemName);
          final int _tmpItemQuantity;
          _tmpItemQuantity = (int) (_stmt.getLong(_columnIndexOfItemQuantity));
          _item.setItemQuantity(_tmpItemQuantity);
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<ReportRow>> getAllForReport() {
    final String _sql = "SELECT itemId, itemName, itemQuantity, 0 AS isLow FROM items ORDER BY itemName COLLATE NOCASE";
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfItemId = 0;
        final int _columnIndexOfItemName = 1;
        final int _columnIndexOfItemQuantity = 2;
        final int _columnIndexOfIsLow = 3;
        final List<ReportRow> _result = new ArrayList<ReportRow>();
        while (_stmt.step()) {
          final ReportRow _item;
          _item = new ReportRow();
          _item.itemId = _stmt.getLong(_columnIndexOfItemId);
          if (_stmt.isNull(_columnIndexOfItemName)) {
            _item.itemName = null;
          } else {
            _item.itemName = _stmt.getText(_columnIndexOfItemName);
          }
          _item.itemQuantity = (int) (_stmt.getLong(_columnIndexOfItemQuantity));
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsLow));
          _item.isLow = _tmp != 0;
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<ReportRow>> getLowStockForReport(final int threshold) {
    final String _sql = "SELECT itemId, itemName, itemQuantity, 1 AS isLow FROM items WHERE itemQuantity <= ? ORDER BY itemQuantity ASC, itemName COLLATE NOCASE";
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, threshold);
        final int _columnIndexOfItemId = 0;
        final int _columnIndexOfItemName = 1;
        final int _columnIndexOfItemQuantity = 2;
        final int _columnIndexOfIsLow = 3;
        final List<ReportRow> _result = new ArrayList<ReportRow>();
        while (_stmt.step()) {
          final ReportRow _item;
          _item = new ReportRow();
          _item.itemId = _stmt.getLong(_columnIndexOfItemId);
          if (_stmt.isNull(_columnIndexOfItemName)) {
            _item.itemName = null;
          } else {
            _item.itemName = _stmt.getText(_columnIndexOfItemName);
          }
          _item.itemQuantity = (int) (_stmt.getLong(_columnIndexOfItemQuantity));
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsLow));
          _item.isLow = _tmp != 0;
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public LiveData<List<ReportRow>> getOutOfStockForReport() {
    final String _sql = "SELECT itemId, itemName, itemQuantity, (itemQuantity = 0) AS isLow FROM items WHERE itemQuantity = 0 ORDER BY itemName COLLATE NOCASE";
    return __db.getInvalidationTracker().createLiveData(new String[] {"items"}, false, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        final int _columnIndexOfItemId = 0;
        final int _columnIndexOfItemName = 1;
        final int _columnIndexOfItemQuantity = 2;
        final int _columnIndexOfIsLow = 3;
        final List<ReportRow> _result = new ArrayList<ReportRow>();
        while (_stmt.step()) {
          final ReportRow _item;
          _item = new ReportRow();
          _item.itemId = _stmt.getLong(_columnIndexOfItemId);
          if (_stmt.isNull(_columnIndexOfItemName)) {
            _item.itemName = null;
          } else {
            _item.itemName = _stmt.getText(_columnIndexOfItemName);
          }
          _item.itemQuantity = (int) (_stmt.getLong(_columnIndexOfItemQuantity));
          final int _tmp;
          _tmp = (int) (_stmt.getLong(_columnIndexOfIsLow));
          _item.isLow = _tmp != 0;
          _result.add(_item);
        }
        return _result;
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int deleteById(final long id) {
    final String _sql = "DELETE FROM items WHERE itemId = ?";
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return SQLiteConnectionUtil.getTotalChangedRows(_connection);
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int updateName(final long id, final String name) {
    final String _sql = "UPDATE items SET itemName = ? WHERE itemId = ?";
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        if (name == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindText(_argIndex, name);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return SQLiteConnectionUtil.getTotalChangedRows(_connection);
      } finally {
        _stmt.close();
      }
    });
  }

  @Override
  public int updateQuantity(final long id, final int qty) {
    final String _sql = "UPDATE items SET itemQuantity = ? WHERE itemId = ?";
    return DBUtil.performBlocking(__db, false, true, (_connection) -> {
      final SQLiteStatement _stmt = _connection.prepare(_sql);
      try {
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, qty);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        _stmt.step();
        return SQLiteConnectionUtil.getTotalChangedRows(_connection);
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
