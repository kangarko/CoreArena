package org.mineacademy.game.impl;

import org.mineacademy.fo.collection.SerializedMap;
import org.mineacademy.fo.debug.Debugger;

public class SimpleMySQLDatabase extends MySQLDatabase {

	public SimpleMySQLDatabase(String table, String line, String user, String password) {
		super(table, line, user, password);
	}

	@Override
	protected final void loadData(SerializedMap map, ArenaPlayer data) {
		final int nuggets = Double.valueOf(map.getString("Nuggets", "-1")).intValue();

		if (nuggets != -1) {
			data.setNuggets(nuggets);

			Debugger.debug("mysql", "Nuggets from MySQL: " + data.getNuggets());
		}

		// MySQL hasnt nuggets but we do, so send it there
		// if (nuggets == -1 && data.getNuggets() != -1)
		//	saveAfterLoad = true;
	}

	@Override
	protected final SerializedMap saveData(ArenaPlayer data) {
		final SerializedMap map = new SerializedMap();

		if (data.getNuggets() != -1) {
			map.put("Nuggets", data.getNuggets());

			Debugger.debug("mysql", "Sending Nuggets to MySQL: " + data.getNuggets());
		}

		return map;
	}
}
