package org.mineacademy.corearena.manager;

import java.util.List;

import org.mineacademy.corearena.data.AllData;
import org.mineacademy.corearena.model.Reward;
import org.mineacademy.corearena.type.RewardType;
import org.mineacademy.fo.Common;

public class RewardsManager {

	public final void loadRewards() {
		final AllData rewards = AllData.getInstance();

		Common.log("Loading " + Common.removeNullAndEmpty(rewards.getRewards(RewardType.ITEM)).size() + " item, " +
				Common.removeNullAndEmpty(rewards.getRewards(RewardType.BLOCK)).size() + " block and " +
				Common.removeNullAndEmpty(rewards.getRewards(RewardType.PACK)).size() + " packs rewards.",
				" ");
	}

	public final List<Reward> getRewards(RewardType type) {
		return AllData.getInstance().getRewards(type);
	}

	public final void setRewards(RewardType type, List<Reward> items) {
		AllData.getInstance().setRewards(type, items);
	}

	public final void updateReward(Reward reward) {
		AllData.getInstance().updateReward(reward);
	}
}
