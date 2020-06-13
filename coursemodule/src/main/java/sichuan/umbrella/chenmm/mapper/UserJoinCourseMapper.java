package sichuan.umbrella.chenmm.mapper;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface UserJoinCourseMapper {
    /**
     * 计算课程的参加人数
     * @param ujcCosId 课程id
     * @return int类型的参加人数
     */
    int countCourseJoin(Integer ujcCosId);

    /**
     * 用户提交课程订单信息，插入一条未支付数据
     * @param ujcCosId 该课程的id
     * @param ujcUsrUsername 拟参加该课程的用户名
     */
    void insertUserUnpaidOrder(@Param("ujc_cos_id") Integer ujcCosId, @Param("ujc_usr_username") String ujcUsrUsername);

    /**
     * 当用户成功支付，更新用户支付状态为“已支付”
     * @param ujcCosId 课程id
     * @param ujcUsrUsername 用户名
     */
    void updateUserStatusToPaid(@Param("ujc_cos_id")Integer ujcCosId,@Param("ujc_usr_username")String ujcUsrUsername);
}
