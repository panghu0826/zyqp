package com.buding.db.model;

import com.buding.common.db.model.BaseModel;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class User extends BaseModel<Integer> implements Serializable {
    private Integer id;

    private Integer userType;

    private String userName;

    private String passwd;

    private String nickname;

    private Integer gender;

    private Date lastLogin;

    private Date lastOffline;

    private Integer continueLogin;

    private String phone;

    private String weixin;

    private Integer coin;

    private Integer fanka;

    private Integer diamond;

    private Integer integral;

    private String headImg;

    private String bindedMobile;

    private String bindedMatch;

    private String token;

    private String wxopenid;

    private String qqopenid;

    private String wxunionid;

    private String deviceId;

    private Integer deviceType;

    private Integer role;

    private Date ctime;

    private Date mtime;

    private Date authTime;

    private Integer hasInvitecode;

    private Integer signDay;

    private Date firstLogin;

    private Integer signWeek;

    private Integer signNums;

    private Integer lunpanNums;

    private Integer canSign;

    private Integer canLunpan;

    private Date lastSign;

    private Date signWeekFirstDay;

    private Integer shouchong1;

    private Integer shouchong2;

    private Integer shouchong3;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd == null ? null : passwd.trim();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname == null ? null : nickname.trim();
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLastOffline() {
        return lastOffline;
    }

    public void setLastOffline(Date lastOffline) {
        this.lastOffline = lastOffline;
    }

    public Integer getContinueLogin() {
        return continueLogin;
    }

    public void setContinueLogin(Integer continueLogin) {
        this.continueLogin = continueLogin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getWeixin() {
        return weixin;
    }

    public void setWeixin(String weixin) {
        this.weixin = weixin == null ? null : weixin.trim();
    }

    public Integer getCoin() {
        return coin;
    }

    public void setCoin(Integer coin) {
        this.coin = coin;
    }

    public Integer getFanka() {
        return fanka;
    }

    public void setFanka(Integer fanka) {
        this.fanka = fanka;
    }

    public Integer getIntegral() {
        return integral;
    }

    public void setIntegral(Integer integral) {
        this.integral = integral;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg == null ? null : headImg.trim();
    }

    public String getBindedMobile() {
        return bindedMobile;
    }

    public void setBindedMobile(String bindedMobile) {
        this.bindedMobile = bindedMobile == null ? null : bindedMobile.trim();
    }

    public String getBindedMatch() {
        return bindedMatch;
    }

    public void setBindedMatch(String bindedMatch) {
        this.bindedMatch = bindedMatch == null ? null : bindedMatch.trim();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    public String getWxopenid() {
        return wxopenid;
    }

    public void setWxopenid(String wxopenid) {
        this.wxopenid = wxopenid == null ? null : wxopenid.trim();
    }

    public String getQqopenid() {
        return qqopenid;
    }

    public void setQqopenid(String qqopenid) {
        this.qqopenid = qqopenid == null ? null : qqopenid.trim();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId == null ? null : deviceId.trim();
    }

    public Integer getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Integer deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public void setAuthTime(Date authTime) {
        this.authTime = authTime;
    }

    public Integer getDiamond() {
        return diamond;
    }

    public void setDiamond(Integer diamond) {
        this.diamond = diamond;
    }

    public String getWxunionid() {
        return wxunionid;
    }

    public void setWxunionid(String wxunionid) {
        this.wxunionid = wxunionid;
    }

    public Integer getHasInvitecode() {
        return hasInvitecode;
    }

    public void setHasInvitecode(Integer hasInvitecode) {
        this.hasInvitecode = hasInvitecode;
    }

    public Integer getSignDay() {
        return signDay;
    }

    public void setSignDay(Integer signDay) {
        this.signDay = signDay;
    }

    public Date getFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(Date firstLogin) {
        this.firstLogin = firstLogin;
    }

    public Integer getSignWeek() {
        return signWeek;
    }

    public void setSignWeek(Integer signWeek) {
        this.signWeek = signWeek;
    }

    public Integer getSignNums() {
        return signNums;
    }

    public void setSignNums(Integer signNums) {
        this.signNums = signNums;
    }

    public Integer getLunpanNums() {
        return lunpanNums;
    }

    public void setLunpanNums(Integer lunpanNums) {
        this.lunpanNums = lunpanNums;
    }

    public Integer getCanSign() {
        return canSign;
    }

    public void setCanSign(Integer canSign) {
        this.canSign = canSign;
    }

    public Integer getCanLunpan() {
        return canLunpan;
    }

    public void setCanLunpan(Integer canLunpan) {
        this.canLunpan = canLunpan;
    }

    public Date getLastSign() {
        return lastSign;
    }

    public void setLastSign(Date lastSign) {
        this.lastSign = lastSign;
    }

    public Integer getShouchong1() {
        return shouchong1;
    }

    public void setShouchong1(Integer shouchong1) {
        this.shouchong1 = shouchong1;
    }

    public Integer getShouchong2() {
        return shouchong2;
    }

    public void setShouchong2(Integer shouchong2) {
        this.shouchong2 = shouchong2;
    }

    public Integer getShouchong3() {
        return shouchong3;
    }

    public void setShouchong3(Integer shouchong3) {
        this.shouchong3 = shouchong3;
    }

    public Date getSignWeekFirstDay() {
        return signWeekFirstDay;
    }

    public void setSignWeekFirstDay(Date signWeekFirstDay) {
        this.signWeekFirstDay = signWeekFirstDay;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userType=" + userType +
                ", userName='" + userName + '\'' +
                ", passwd='" + passwd + '\'' +
                ", nickname='" + nickname + '\'' +
                ", gender=" + gender +
                ", lastLogin=" + lastLogin +
                ", lastOffline=" + lastOffline +
                ", continueLogin=" + continueLogin +
                ", phone='" + phone + '\'' +
                ", weixin='" + weixin + '\'' +
                ", coin=" + coin +
                ", fanka=" + fanka +
                ", diamond=" + diamond +
                ", integral=" + integral +
                ", headImg='" + headImg + '\'' +
                ", bindedMobile='" + bindedMobile + '\'' +
                ", bindedMatch='" + bindedMatch + '\'' +
                ", token='" + token + '\'' +
                ", wxopenid='" + wxopenid + '\'' +
                ", qqopenid='" + qqopenid + '\'' +
                ", wxunionid='" + wxunionid + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", deviceType=" + deviceType +
                ", role=" + role +
                ", ctime=" + ctime +
                ", mtime=" + mtime +
                ", authTime=" + authTime +
                ", hasInvitecode=" + hasInvitecode +
                ", signDay=" + signDay +
                ", firstLogin=" + firstLogin +
                ", signWeek=" + signWeek +
                ", signNums=" + signNums +
                ", lunpanNums=" + lunpanNums +
                ", canSign=" + canSign +
                ", canLunpan=" + canLunpan +
                ", lastSign=" + lastSign +
                ", signWeekFirstDay=" + signWeekFirstDay +
                ", shouchong1=" + shouchong1 +
                ", shouchong2=" + shouchong2 +
                ", shouchong3=" + shouchong3 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(userType, user.userType) &&
                Objects.equals(userName, user.userName) &&
                Objects.equals(passwd, user.passwd) &&
                Objects.equals(nickname, user.nickname) &&
                Objects.equals(gender, user.gender) &&
                Objects.equals(lastLogin, user.lastLogin) &&
                Objects.equals(lastOffline, user.lastOffline) &&
                Objects.equals(continueLogin, user.continueLogin) &&
                Objects.equals(phone, user.phone) &&
                Objects.equals(weixin, user.weixin) &&
                Objects.equals(coin, user.coin) &&
                Objects.equals(fanka, user.fanka) &&
                Objects.equals(diamond, user.diamond) &&
                Objects.equals(integral, user.integral) &&
                Objects.equals(headImg, user.headImg) &&
                Objects.equals(bindedMobile, user.bindedMobile) &&
                Objects.equals(bindedMatch, user.bindedMatch) &&
                Objects.equals(token, user.token) &&
                Objects.equals(wxopenid, user.wxopenid) &&
                Objects.equals(qqopenid, user.qqopenid) &&
                Objects.equals(wxunionid, user.wxunionid) &&
                Objects.equals(deviceId, user.deviceId) &&
                Objects.equals(deviceType, user.deviceType) &&
                Objects.equals(role, user.role) &&
                Objects.equals(ctime, user.ctime) &&
                Objects.equals(mtime, user.mtime) &&
                Objects.equals(authTime, user.authTime) &&
                Objects.equals(hasInvitecode, user.hasInvitecode) &&
                Objects.equals(signDay, user.signDay) &&
                Objects.equals(firstLogin, user.firstLogin) &&
                Objects.equals(signWeek, user.signWeek) &&
                Objects.equals(signNums, user.signNums) &&
                Objects.equals(lunpanNums, user.lunpanNums) &&
                Objects.equals(canSign, user.canSign) &&
                Objects.equals(canLunpan, user.canLunpan) &&
                Objects.equals(lastSign, user.lastSign) &&
                Objects.equals(signWeekFirstDay, user.signWeekFirstDay) &&
                Objects.equals(shouchong1, user.shouchong1) &&
                Objects.equals(shouchong2, user.shouchong2) &&
                Objects.equals(shouchong3, user.shouchong3);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, userType, userName, passwd, nickname, gender, lastLogin, lastOffline, continueLogin, phone, weixin, coin, fanka, diamond, integral, headImg, bindedMobile, bindedMatch, token, wxopenid, qqopenid, wxunionid, deviceId, deviceType, role, ctime, mtime, authTime, hasInvitecode, signDay, firstLogin, signWeek, signNums, lunpanNums, canSign, canLunpan, lastSign, signWeekFirstDay, shouchong1, shouchong2, shouchong3);
    }
}