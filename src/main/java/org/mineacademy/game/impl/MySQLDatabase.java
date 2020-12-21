package org.mineacademy.game.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.mineacademy.fo.Common;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.database.SimpleDatabase;
import org.mineacademy.fo.debug.Debugger;
import org.mineacademy.game.settings.Settings;

public abstract class MySQLDatabase extends SimpleDatabase {

	// ------------------------------------------------------------------------

	private static MySQLDatabase instance;

	public static final void setInstance(String table, String line, String user, String password) {
		instance = new SimpleMySQLDatabase(table, line, user, password);
	}

	public static final void load(ArenaPlayer cache) {
		if (instance != null && Settings.MySQL.ENABLED)
			instance.loadIfStored(cache);
	}

	public static final void save(ArenaPlayer cache, boolean createIfNoData) {
		if (instance != null && Settings.MySQL.ENABLED)
			instance.saveOrUpdate(cache, createIfNoData);
	}

	// ------------------------------------------------------------------------

	/**
	 * A flag indicating we should save to MySQL immediatelly after load.
	 */
	protected boolean saveAfterLoad = false;

	private boolean doingUpdates = true;

	public MySQLDatabase(String table, String line, String user, String password) {
		connect(line, user, password, table);
	}

	@Override
	protected void onConnected() {
		update("CREATE TABLE IF NOT EXISTS " + getTable() + "(UUID varchar(64), Data text)");
	}

	private final void loadIfStored(ArenaPlayer cache) {
		saveAfterLoad = false;

		if (!isLoaded() || cache.getUuid() == null || !doingUpdates)
			return;

		try {
			final long now = System.currentTimeMillis();

			final ResultSet rs = query("SELECT * FROM " + getTable() + " WHERE UUID= '" + cache.getUuid() + "'");
			final String data = rs.next() ? rs.getString("Data") : "{}";
			final SerializedMap parsed = SerializedMap.fromJson(data);

			// Always load even if not stored (due to saveAfterLoad flag)
			doingUpdates = false;
			loadData(parsed, cache);
			doingUpdates = true;

			if (saveAfterLoad) {
				saveAfterLoad = false;

				saveOrUpdate(cache, true);
			}

			rs.close();
			Debugger.debug("mysql", "Loaded MySQL data for " + cache.getName() + " took " + (System.currentTimeMillis() - now) + " ms");

		} catch (final Throwable e) {
			Common.error(e, "Error while loading MySQL data!", "Player: " + cache.getName(), "Error: %error");
		}
	}

	protected abstract void loadData(SerializedMap map, ArenaPlayer data);

	private final void saveOrUpdate(ArenaPlayer cache, boolean createIfNoData) {
		if (!isLoaded() || !doingUpdates || cache.getUuid() == null)
			return;

		doingUpdates = false;

		try {
			final long now = System.currentTimeMillis();
			final UUID uuid = cache.getUuid();
			final SerializedMap data = saveData(cache);

			if (!data.isEmpty()) {
				data.put("last-update", TimeUtil.currentTimeSeconds());

				if (!isPlayerStored(uuid)) {
					if (createIfNoData) {
						update("INSERT INTO " + getTable() + "(UUID, Data) VALUES ('" + uuid.toString() + "', '" + data.toJson() + "');");
						Debugger.debug("mysql", "&bMaking new MySQL data for " + cache.getName());

					} else
						Debugger.debug("mysql", "&cSkipping saving MySQL for " + cache.getName() + " due to no data");

				} else {
					Debugger.debug("mysql", "&aUpdating MySQL data for " + cache.getName() + " took " + (System.currentTimeMillis() - now) + " ms");
					update("UPDATE " + getTable() + " SET Data= '" + data.toJson() + "' WHERE UUID= '" + uuid.toString() + "';");
				}

			} else
				Debugger.debug("mysql", "&cSkipping save/update MySQL for " + cache.getName() + " as it's empty!");

		} finally {
			doingUpdates = true;
		}
	}

	protected abstract SerializedMap saveData(ArenaPlayer data);

	private final boolean isPlayerStored(UUID uuid) {
		Valid.checkNotNull(uuid, "UUID cannot be null");

		try {
			try (ResultSet rs = query("SELECT * FROM " + getTable() + " WHERE UUID= '" + uuid.toString() + "'")) {
				if (rs == null)
					return false;

				if (rs.next())
					return rs.getString("UUID") != null;
			}

		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return false;
	}
}