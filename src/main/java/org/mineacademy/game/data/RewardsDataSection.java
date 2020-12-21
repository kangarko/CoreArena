package org.mineacademy.game.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mineacademy.fo.Valid;
import org.mineacademy.fo.collection.StrictMap;
import org.mineacademy.fo.constants.FoConstants;
import org.mineacademy.fo.settings.YamlSectionConfig;
import org.mineacademy.game.impl.SimpleReward;
import org.mineacademy.game.model.Reward;
import org.mineacademy.game.type.RewardType;

public final class RewardsDataSection extends YamlSectionConfig {

	private final StrictMap<RewardType, List<Reward>> rewards = new StrictMap<>();

	public RewardsDataSection() {
		super("Rewards");

		loadConfiguration(NO_DEFAULT, FoConstants.File.DATA);
	}

	@Override
	protected void onLoadFinish() {
		loadRewards();
	}

	private void loadRewards() {
		rewards.clear();

		for (final RewardType type : RewardType.values())
			loadReward(type);
	}

	private void loadReward(RewardType type) {
		final List<Reward> loaded = new ArrayList<>();
		final List<?> rewardsRaw = getList(type.toString());

		if (rewardsRaw != null) {

			final ArrayList<HashMap<String, Object>> maps = (ArrayList<HashMap<String, Object>>) rewardsRaw;

			for (final HashMap<String, Object> map : maps)
				if (map != null && !map.isEmpty()) {
					final Reward reward = SimpleReward.deserialize(map);

					loaded.add(reward);
				} else
					loaded.add(null);
		}

		rewards.put(type, loaded);
	}

	public void setRewards(RewardType type, List<Reward> items) {
		Valid.checkNotNull(items, "Report / Rewards cannot be null!");

		rewards.override(type, items);
		save(type.toString(), getRewards(type));

		loadRewards();
	}

	public void updateReward(Reward reward) {
		save(reward.getType().toString(), getRewards(reward.getType()));
		loadRewards();
	}

	public List<Reward> getRewards(RewardType type) {
		return rewards.getOrPut(type, new ArrayList<>());
	}
}