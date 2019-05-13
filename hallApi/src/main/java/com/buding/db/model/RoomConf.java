package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;

public class RoomConf extends BaseModel<Integer> implements Serializable {
    private Integer id;

    private String roomId;

    private String roomName;

    private String roomType;

    private String matchId;

    private Integer baseScore;

    private Integer minCoinLimit;

    private Integer maxCoinLimit;

    private String icon;

    private String matchClassFullName;

    private Integer gameParamOperTimeOut;

    private Integer gameParamPengPlayMills;

    private Integer gameParamChuPlayMills;

    private Integer gameParamChiPengPlayMills;

    private Boolean gameParamAutoOperWhenTimeout;

    private Integer gameParamThinkMillsWhenAutoOper;

    private Integer seatSizeLower;

    private Integer seatSizeUpper;

    private Boolean autoStartGame;

    private Boolean autoChangeDesk;

    private String gameClassFullName;

    private String deskClassFullName;

    private String roomClassFullName;

    private Boolean supportRobot;

    private Integer secondsAddFirstRobot;

    private Double addRobotRate;

    private Boolean autoReady;

    private Integer secondsBeforKickout;

    private Integer srvFee;

    private Integer gameCountLow;

    private Integer gameCountHigh;

    private Integer status;

    private String confJson;

    private String fee;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId == null ? null : roomId.trim();
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName == null ? null : roomName.trim();
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType == null ? null : roomType.trim();
    }

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId == null ? null : matchId.trim();
    }

    public Integer getBaseScore() {
        return baseScore;
    }

    public void setBaseScore(Integer baseScore) {
        this.baseScore = baseScore;
    }

    public Integer getMinCoinLimit() {
        return minCoinLimit;
    }

    public void setMinCoinLimit(Integer minCoinLimit) {
        this.minCoinLimit = minCoinLimit;
    }

    public Integer getMaxCoinLimit() {
        return maxCoinLimit;
    }

    public void setMaxCoinLimit(Integer maxCoinLimit) {
        this.maxCoinLimit = maxCoinLimit;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? null : icon.trim();
    }

    public String getMatchClassFullName() {
        return matchClassFullName;
    }

    public void setMatchClassFullName(String matchClassFullName) {
        this.matchClassFullName = matchClassFullName == null ? null : matchClassFullName.trim();
    }

    public Integer getGameParamOperTimeOut() {
        return gameParamOperTimeOut;
    }

    public void setGameParamOperTimeOut(Integer gameParamOperTimeOut) {
        this.gameParamOperTimeOut = gameParamOperTimeOut;
    }

    public Integer getGameParamPengPlayMills() {
        return gameParamPengPlayMills;
    }

    public void setGameParamPengPlayMills(Integer gameParamPengPlayMills) {
        this.gameParamPengPlayMills = gameParamPengPlayMills;
    }

    public Integer getGameParamChuPlayMills() {
        return gameParamChuPlayMills;
    }

    public void setGameParamChuPlayMills(Integer gameParamChuPlayMills) {
        this.gameParamChuPlayMills = gameParamChuPlayMills;
    }

    public Integer getGameParamChiPengPlayMills() {
        return gameParamChiPengPlayMills;
    }

    public void setGameParamChiPengPlayMills(Integer gameParamChiPengPlayMills) {
        this.gameParamChiPengPlayMills = gameParamChiPengPlayMills;
    }

    public Boolean getGameParamAutoOperWhenTimeout() {
        return gameParamAutoOperWhenTimeout;
    }

    public void setGameParamAutoOperWhenTimeout(Boolean gameParamAutoOperWhenTimeout) {
        this.gameParamAutoOperWhenTimeout = gameParamAutoOperWhenTimeout;
    }

    public Integer getGameParamThinkMillsWhenAutoOper() {
        return gameParamThinkMillsWhenAutoOper;
    }

    public void setGameParamThinkMillsWhenAutoOper(Integer gameParamThinkMillsWhenAutoOper) {
        this.gameParamThinkMillsWhenAutoOper = gameParamThinkMillsWhenAutoOper;
    }

    public Integer getSeatSizeLower() {
        return seatSizeLower;
    }

    public void setSeatSizeLower(Integer seatSizeLower) {
        this.seatSizeLower = seatSizeLower;
    }

    public Integer getSeatSizeUpper() {
        return seatSizeUpper;
    }

    public void setSeatSizeUpper(Integer seatSizeUpper) {
        this.seatSizeUpper = seatSizeUpper;
    }

    public Boolean getAutoStartGame() {
        return autoStartGame;
    }

    public void setAutoStartGame(Boolean autoStartGame) {
        this.autoStartGame = autoStartGame;
    }

    public Boolean getAutoChangeDesk() {
        return autoChangeDesk;
    }

    public void setAutoChangeDesk(Boolean autoChangeDesk) {
        this.autoChangeDesk = autoChangeDesk;
    }

    public String getGameClassFullName() {
        return gameClassFullName;
    }

    public void setGameClassFullName(String gameClassFullName) {
        this.gameClassFullName = gameClassFullName == null ? null : gameClassFullName.trim();
    }

    public String getDeskClassFullName() {
        return deskClassFullName;
    }

    public void setDeskClassFullName(String deskClassFullName) {
        this.deskClassFullName = deskClassFullName == null ? null : deskClassFullName.trim();
    }

    public String getRoomClassFullName() {
        return roomClassFullName;
    }

    public void setRoomClassFullName(String roomClassFullName) {
        this.roomClassFullName = roomClassFullName == null ? null : roomClassFullName.trim();
    }

    public Boolean getSupportRobot() {
        return supportRobot;
    }

    public void setSupportRobot(Boolean supportRobot) {
        this.supportRobot = supportRobot;
    }

    public Integer getSecondsAddFirstRobot() {
        return secondsAddFirstRobot;
    }

    public void setSecondsAddFirstRobot(Integer secondsAddFirstRobot) {
        this.secondsAddFirstRobot = secondsAddFirstRobot;
    }

    public Double getAddRobotRate() {
        return addRobotRate;
    }

    public void setAddRobotRate(Double addRobotRate) {
        this.addRobotRate = addRobotRate;
    }

    public Boolean getAutoReady() {
        return autoReady;
    }

    public void setAutoReady(Boolean autoReady) {
        this.autoReady = autoReady;
    }

    public Integer getSecondsBeforKickout() {
        return secondsBeforKickout;
    }

    public void setSecondsBeforKickout(Integer secondsBeforKickout) {
        this.secondsBeforKickout = secondsBeforKickout;
    }

    public Integer getSrvFee() {
        return srvFee;
    }

    public void setSrvFee(Integer srvFee) {
        this.srvFee = srvFee;
    }

    public Integer getGameCountLow() {
        return gameCountLow;
    }

    public void setGameCountLow(Integer gameCountLow) {
        this.gameCountLow = gameCountLow;
    }

    public Integer getGameCountHigh() {
        return gameCountHigh;
    }

    public void setGameCountHigh(Integer gameCountHigh) {
        this.gameCountHigh = gameCountHigh;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getConfJson() {
        return confJson;
    }

    public void setConfJson(String confJson) {
        this.confJson = confJson == null ? null : confJson.trim();
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee == null ? null : fee.trim();
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
        RoomConf other = (RoomConf) that;
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
            && (this.getConfJson() == null ? other.getConfJson() == null : this.getConfJson().equals(other.getConfJson()))
            && (this.getFee() == null ? other.getFee() == null : this.getFee().equals(other.getFee()));
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
        result = prime * result + ((getConfJson() == null) ? 0 : getConfJson().hashCode());
        result = prime * result + ((getFee() == null) ? 0 : getFee().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", roomId=").append(roomId);
        sb.append(", roomName=").append(roomName);
        sb.append(", roomType=").append(roomType);
        sb.append(", matchId=").append(matchId);
        sb.append(", baseScore=").append(baseScore);
        sb.append(", minCoinLimit=").append(minCoinLimit);
        sb.append(", maxCoinLimit=").append(maxCoinLimit);
        sb.append(", icon=").append(icon);
        sb.append(", matchClassFullName=").append(matchClassFullName);
        sb.append(", gameParamOperTimeOut=").append(gameParamOperTimeOut);
        sb.append(", gameParamPengPlayMills=").append(gameParamPengPlayMills);
        sb.append(", gameParamChuPlayMills=").append(gameParamChuPlayMills);
        sb.append(", gameParamChiPengPlayMills=").append(gameParamChiPengPlayMills);
        sb.append(", gameParamAutoOperWhenTimeout=").append(gameParamAutoOperWhenTimeout);
        sb.append(", gameParamThinkMillsWhenAutoOper=").append(gameParamThinkMillsWhenAutoOper);
        sb.append(", seatSizeLower=").append(seatSizeLower);
        sb.append(", seatSizeUpper=").append(seatSizeUpper);
        sb.append(", autoStartGame=").append(autoStartGame);
        sb.append(", autoChangeDesk=").append(autoChangeDesk);
        sb.append(", gameClassFullName=").append(gameClassFullName);
        sb.append(", deskClassFullName=").append(deskClassFullName);
        sb.append(", roomClassFullName=").append(roomClassFullName);
        sb.append(", supportRobot=").append(supportRobot);
        sb.append(", secondsAddFirstRobot=").append(secondsAddFirstRobot);
        sb.append(", addRobotRate=").append(addRobotRate);
        sb.append(", autoReady=").append(autoReady);
        sb.append(", secondsBeforKickout=").append(secondsBeforKickout);
        sb.append(", srvFee=").append(srvFee);
        sb.append(", gameCountLow=").append(gameCountLow);
        sb.append(", gameCountHigh=").append(gameCountHigh);
        sb.append(", status=").append(status);
        sb.append(", confJson=").append(confJson);
        sb.append(", fee=").append(fee);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}