package org.mineacademy.corearena.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.corearena.settings.Settings;
import org.mineacademy.fo.Common;
import org.mineacademy.fo.MathUtil;
import org.mineacademy.fo.SerializeUtilCore.Language;
import org.mineacademy.fo.TimeUtil;
import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.database.SimpleDatabase;
import org.mineacademy.fo.platform.Platform;

public abstract class MySQLDatabase extends SimpleDatabase {

	// ------------------------------------------------------------------------

	private static MySQLDatabase instance;

	public static final void setInstance(String line, String user, String password) {
		instance = new SimpleMySQLDatabase(line, user, password);
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

	public MySQLDatabase(String line, String user, String password) {
		this.addVariable("table", "CoreArena");

		this.connect(line, user, password);
	}

	@Override
	protected void onConnected() {
		this.updateUnsafe("CREATE TABLE IF NOT EXISTS {table} (UUID varchar(64), Data text)");
	}

	private final void loadIfStored(ArenaPlayer cache) {
		this.saveAfterLoad = false;

		if (!this.isConnected() || cache.getUniqueId() == null || !this.doingUpdates)
			return;

		final long now = System.currentTimeMillis();

		Platform.runTaskAsync(() -> {

			try (ResultSet resultSet = this.queryUnsafe("SELECT * FROM {table} WHERE UUID= '" + cache.getUniqueId() + "'")) {

				final String data = resultSet.next() ? resultSet.getString("Data") : "{}";
				final SerializedMap parsed = SerializedMap.fromObject(Language.JSON, data);

				Platform.runTask(MathUtil.range(Settings.MySQL.DELAY_TICKS - (int) ((System.currentTimeMillis() - now) / 50), 0, Integer.MAX_VALUE), () -> {
					// Always load even if not stored (due to saveAfterLoad flag)
					this.doingUpdates = false;
					this.loadData(parsed, cache);
					this.doingUpdates = true;

					if (this.saveAfterLoad) {
						this.saveAfterLoad = false;

						this.saveOrUpdate(cache, true);
					}
				});

			} catch (final Throwable throwable) {
				Common.error(throwable,
						"Error while loading MySQL data!",
						"Player: " + cache.getPlayerName(),
						"Error: {error}");
			}
		});
	}

	protected abstract void loadData(SerializedMap map, ArenaPlayer data);

	private final void saveOrUpdate(ArenaPlayer cache, boolean createIfNoData) {
		if (!this.isConnected() || !this.doingUpdates || cache.getUniqueId() == null)
			return;

		final UUID uniqueId = cache.getUniqueId();
		final SerializedMap data = this.saveData(cache);

		if (!data.isEmpty()) {
			this.doingUpdates = false;

			data.put("last-update", TimeUtil.getCurrentTimeSeconds());
			final String jsonData = data.toJson();

			Platform.runTaskAsync(() -> {
				try {
					if (!this.isPlayerStored(uniqueId)) {
						if (createIfNoData)
							this.updateUnsafe("INSERT INTO {table} (UUID, Data) VALUES ('" + uniqueId.toString() + "', '" + jsonData + "');");

					} else
						this.updateUnsafe("UPDATE {table} SET Data= '" + jsonData + "' WHERE UUID= '" + uniqueId.toString() + "';");
				} finally {
					this.doingUpdates = true;
				}
			});
		}

	}

	protected abstract SerializedMap saveData(ArenaPlayer data);

	private final boolean isPlayerStored(UUID uuid) {
		Valid.checkNotNull(uuid, "UUID cannot be null");

		try {
			try (ResultSet rs = this.queryUnsafe("SELECT * FROM {table} WHERE UUID= '" + uuid.toString() + "'")) {
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