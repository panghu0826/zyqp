package com.buding.db.model;

import java.io.Serializable;

public class RoomConfWithBLOBs extends RoomConf implements Serializable {
    private String fee;

    private String confJson;

    private static final long serialVersionUID = 1L;

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee == null ? null : fee.trim();
    }

    public String getConfJson() {
        return confJson;
    }

    public void setConfJson(String confJson) {
        this.confJson = confJson == null ? null : confJson.trim();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        RoomConfWithBLOBs other = (RoomConfWithBLOBs) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRoomId() == null ? other.getRoomId() == null : this.getRoomId().equals(other.getRoomId()))
            && (this.getRoomName() == null ? other.getRoomName() == null : this.getRoomName().equals(other.getRoomName()))
            && (this.getRoomType() == null ? other.getRoomType() == null : this.getRoomType().equals(other.getRoomType()))
            && (this.getMatchId() == null ? other.getMatchId() == null : this.getMatchId().equals(other.getMatchId()))
            && (this.getBaseScore() == null ? other.getBaseScore() == null : this.getBaseScore().equals(other.getBaseScore()))
            && (this.getMinCoinLimit() == null ? other.getMinCoinLimit() == null : this.getMinCoinLimit().equals(other.getMinCoinLimit()))
            && (this.getMaxCoinLimit() == null ? other.getMaxCoinLimit() == null : this.getMaxCoinLimit().equals(other.getMaxCoinLimit()))
            && (this.getIcon() == null ? other.getIcon() == null : this.getIcon().equals(other.getIcon()))
            && (this.getMatchClassFullName() == null ? other.getMatchClassFullName() == null : this.getMatchClassFullName().equals(other.getMatchClassFullName()))
            && (this.getGameParamOperTimeOut() == null ? other.getGameParamOperTimeOut() == null : this.getGameParamOperTimeOut().equals(other.getGameParamOperTimeOut()))
            && (this.getGameParamPengPlayMills() == null ? other.getGameParamPengPlayMills() == null : this.getGameParamPengPlayMills().equals(other.getGameParamPengPlayMills()))
            && (this.getGameParamChuPlayMills() == null ? other.getGameParamChuPlayMills() == null : this.getGameParamChuPlayMills().equals(other.getGameParamChuPlayMills()))
            && (this.getGameParamChiPengPlayMills() == null ? other.getGameParamChiPengPlayMills() == null : this.getGameParamChiPengPlayMills().equals(other.getGameParamChiPengPlayMills()))
            && (this.getGameParamAutoOperWhenTimeout() == null ? other.getGameParamAutoOperWhenTimeout() == null : this.getGameParamAutoOperWhenTimeout().equals(other.getGameParamAutoOperWhenTimeout()))
            && (this.getGameParamThinkMillsWhenAutoOper() == null ? other.getGameParamThinkMillsWhenAutoOper() == null : this.getGameParamThinkMillsWhenAutoOper().equals(other.getGameParamThinkMillsWhenAutoOper()))
            && (this.getSeatSizeLower() == null ? other.getSeatSizeLower() == null : this.getSeatSizeLower().equals(other.getSeatSizeLower()))
            && (this.getSeatSizeUpper() == null ? other.getSeatSizeUpper() == null : this.getSeatSizeUpper().equals(other.getSeatSizeUpper()))
            && (this.getAutoStartGame() == null ? other.getAutoStartGame() == null : this.getAutoStartGame().equals(other.getAutoStartGame()))
            && (this.getAutoChangeDesk() == null ? other.getAutoChangeDesk() == null : this.getAutoChangeDesk().equals(other.getAutoChangeDesk()))
            && (this.getGameClassFullName() == null ? other.getGameClassFullName() == null : this.getGameClassFullName().equals(other.getGameClassFullName()))
            && (this.getDeskClassFullName() == null ? other.getDeskClassFullName() == null : this.getDeskClassFullName().equals(other.getDeskClassFullName()))
            && (this.getRoomClassFullName() == null ? other.getRoomClassFullName() == null : this.getRoomClassFullName().equals(other.getRoomClassFullName()))
            && (this.getSupportRobot() == null ? other.getSupportRobot() == null : this.getSupportRobot().equals(other.getSupportRobot()))
            && (this.getSecondsAddFirstRobot() == null ? other.getSecondsAddFirstRobot() == null : this.getSecondsAddFirstRobot().equals(other.getSecondsAddFirstRobot()))
            && (this.getAddRobotRate() == null ? other.getAddRobotRate() == null : this.getAddRobotRate().equals(other.getAddRobotRate()))
            && (this.getAutoReady() == null ? other.getAutoReady() == null : this.getAutoReady().equals(other.getAutoReady()))
            && (this.getSecondsBeforKickout() == null ? other.getSecondsBeforKickout() == null : this.getSecondsBeforKickout().equals(other.getSecondsBeforKickout()))
            && (this.getSrvFee() == null ? other.getSrvFee() == null : this.getSrvFee().equals(other.getSrvFee()))
            && (this.getGameCountLow() == null ? other.getGameCountLow() == null : this.getGameCountLow().equals(other.getGameCountLow()))
            && (this.getGameCountHigh() == null ? other.getGameCountHigh() == null : this.getGameCountHigh().equals(other.getGameCountHigh()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getFee() == null ? other.getFee() == null : this.getFee().equals(other.getFee()))
            && (this.getConfJson() == null ? other.getConfJson() == null : this.getConfJson().equals(other.getConfJson()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRoomId() == null) ? 0 : getRoomId().hashCode());
        result = prime * result + ((getRoomName() == null) ? 0 : getRoomName().hashCode());
        result = prime * result + ((getRoomType() == null) ? 0 : getRoomType().hashCode());
        result = prime * result + ((getMatchId() == null) ? 0 : getMatchId().hashCode());
        result = prime * result + ((getBaseScore() == null) ? 0 : getBaseScore().hashCode());
        result = prime * result + ((getMinCoinLimit() == null) ? 0 : getMinCoinLimit().hashCode());
        result = prime * result + ((getMaxCoinLimit() == null) ? 0 : getMaxCoinLimit().hashCode());
        result = prime * result + ((getIcon() == null) ? 0 : getIcon().hashCode());
        result = prime * result + ((getMatchClassFullName() == null) ? 0 : getMatchClassFullName().hashCode());
        result = prime * result + ((getGameParamOperTimeOut() == null) ? 0 : getGameParamOperTimeOut().hashCode());
        result = prime * result + ((getGameParamPengPlayMills() == null) ? 0 : getGameParamPengPlayMills().hashCode());
        result = prime * result + ((getGameParamChuPlayMills() == null) ? 0 : getGameParamChuPlayMills().hashCode());
        result = prime * result + ((getGameParamChiPengPlayMills() == null) ? 0 : getGameParamChiPengPlayMills().hashCode());
        result = prime * result + ((getGameParamAutoOperWhenTimeout() == null) ? 0 : getGameParamAutoOperWhenTimeout().hashCode());
        result = prime * result + ((getGameParamThinkMillsWhenAutoOper() == null) ? 0 : getGameParamThinkMillsWhenAutoOper().hashCode());
        result = prime * result + ((getSeatSizeLower() == null) ? 0 : getSeatSizeLower().hashCode());
        result = prime * result + ((getSeatSizeUpper() == null) ? 0 : getSeatSizeUpper().hashCode());
        result = prime * result + ((getAutoStartGame() == null) ? 0 : getAutoStartGame().hashCode());
        result = prime * result + ((getAutoChangeDesk() == null) ? 0 : getAutoChangeDesk().hashCode());
        result = prime * result + ((getGameClassFullName() == null) ? 0 : getGameClassFullName().hashCode());
        result = prime * result + ((getDeskClassFullName() == null) ? 0 : getDeskClassFullName().hashCode());
        result = prime * result + ((getRoomClassFullName() == null) ? 0 : getRoomClassFullName().hashCode());
        result = prime * result + ((getSupportRobot() == null) ? 0 : getSupportRobot().hashCode());
        result = prime * result + ((getSecondsAddFirstRobot() == null) ? 0 : getSecondsAddFirstRobot().hashCode());
        result = prime * result + ((getAddRobotRate() == null) ? 0 : getAddRobotRate().hashCode());
        result = prime * result + ((getAutoReady() == null) ? 0 : getAutoReady().hashCode());
        result = prime * result + ((getSecondsBeforKickout() == null) ? 0 : getSecondsBeforKickout().hashCode());
        result = prime * result + ((getSrvFee() == null) ? 0 : getSrvFee().hashCode());
        result = prime * result + ((getGameCountLow() == null) ? 0 : getGameCountLow().hashCode());
        result = prime * result + ((getGameCountHigh() == null) ? 0 : getGameCountHigh().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getFee() == null) ? 0 : getFee().hashCode());
        result = prime * result + ((getConfJson() == null) ? 0 : getConfJson().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", fee=").append(fee);
        sb.append(", confJson=").append(confJson);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}