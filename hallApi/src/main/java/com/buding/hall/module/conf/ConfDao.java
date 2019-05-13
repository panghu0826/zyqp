package com.buding.hall.module.conf;

import java.util.List;

import com.buding.db.model.MallConf;
import com.buding.db.model.RoomConf;

/**
 * @author jaime qq_1094086610
 * @Description:
 * 
 */
public interface ConfDao {
	public List<RoomConf> getRoomConfList();
	public List<MallConf> getMallConfList();
}
