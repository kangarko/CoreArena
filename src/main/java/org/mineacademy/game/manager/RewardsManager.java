package org.mineacademy.game.manager;

import java.util.List;

import org.mineacademy.fo.Common;
import org.mineacademy.game.data.RewardsDataSection;
import org.mineacademy.game.model.Reward;
import org.mineacademy.game.type.RewardType;

public class RewardsManager {

	private RewardsDataSection rewards;

	public final void loadRewards() {
		rewards = new RewardsDataSection();

		Common.log("Loading " + Common.removeNullAndEmpty(rewards.getRewards(RewardType.ITEM)).size() + " item, " +
				Common.removeNullAndEmpty(rewards.getRewards(RewardType.BLOCK)).size() + " block and " +
				Common.removeNullAndEmpty(rewards.getRewards(RewardType.PACK)).size() + " packs rewards.",
				" ");
	}

	public final List<Reward> getRewards(RewardType type) {
		return rewards.getRewards(type);
	}

	public final void setRewards(RewardType type, List<Reward> items) {
		rewards.setRewards(type, items);
	}

	public final void updateReward(Reward reward) {
		rewards.updateReward(reward);
	}
}
