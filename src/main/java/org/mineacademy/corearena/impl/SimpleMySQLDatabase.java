package org.mineacademy.corearena.impl;

import org.mineacademy.corearena.data.AllData.ArenaPlayer;
import org.mineacademy.fo.collection.SerializedMap;

public class SimpleMySQLDatabase extends MySQLDatabase {

	public SimpleMySQLDatabase(String line, String user, String password) {
		super(line, user, password);
	}

	@Override
	protected final void loadData(SerializedMap map, ArenaPlayer data) {
		final int nuggets = Double.valueOf(map.getString("Nuggets", "-1")).intValue();

		if (nuggets != -1)
			data.setNuggets(nuggets);
	}

	@Override
	protected final SerializedMap saveData(ArenaPlayer data) {
		final SerializedMap map = new SerializedMap();

		if (data.getNuggets() != -1)
			map.put("Nuggets", data.getNuggets());

		return map;
	}
}
