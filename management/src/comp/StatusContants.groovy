package comp

/**
 * Created by panjintao on 2016/11/7.
 */
class StatusContants {
    public static int STATUS_DELETED = 10//删除
    public static int STATUS_VALID = 1 //有效(若在需要审核的情况下，1也代表已审核)
    public static int STATUS_INVALID = 0//无效
    public static int STATUS_DRAFT = 3 //草稿 ，跟点进社区帖子草稿状态一致
    public static int STATUS_PENDING_REVIEW = 6 //待审核
}
