package chatmodule.controller;

import chatmodule.bean.*;
import chatmodule.service.GroupMemberService;
import chatmodule.service.GroupMessageService;
import chatmodule.service.GroupService;
import chatmodule.util.GroupJsonSerializer;
import chatmodule.util.SnowflakeIdWorker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    @Autowired
    GroupService groupService;
    @Autowired
    GroupMemberService groupMemberService;
    @Autowired
    GroupMessageService groupMessageService;

    private Gson gson;

    @Autowired
    public void setGson() {
        this.gson = new GsonBuilder().registerTypeAdapter(Group.class, new GroupJsonSerializer()).create();
    }

    private final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @GetMapping("/selectByGrpID")
    public Group selectByGrpID(int grpId) {
        logger.info("查询group_id=" + grpId);
        return groupService.selectByGrpID(grpId);
    }

    @GetMapping("/randomGroup")
    public String randomGroupIds(@RequestParam("need") int need) {
        List<Long> list = groupService.randomGroupIds(need);
        List<Group> groups = new LinkedList<>();
        for (Long aLong : list) {
            groups.add(groupService.selectByGrpID(aLong));
        }
        return gson.toJson(groups);
    }

    /**
     * 根据名称搜索群
     *
     * @param request 群名
     * @return json
     */
    @RequestMapping("/searchGroupByName")
    public List<Group> searchGroupByName(HttpServletRequest request) {
        String groupName = request.getParameter("groupName");
        return groupService.searchByGroupNameLike(groupName);
    }

    /**
     * 根据群的类别查询
     *
     * @param request 全部、同城群、名师课堂群、结伴备考群
     * @return json
     */
    @RequestMapping("/searchGroupByType")
    public String searchGroupByType(HttpServletRequest request) {
        String groupType = request.getParameter("groupType");
        if (groupType.equals("全部")) groupType = null;
        logger.info("查询群类型：" + groupType);
        return gson.toJson(groupService.searchByGroupType(groupType));
    }

    /**
     * 根据id查询群人数
     *
     * @param grpId 群id
     * @return 群人数
     */
    @GetMapping("/countMembers")
    public int countGroupMembers(long grpId) {
        logger.info("群id" + grpId + "查询群人数");
        return groupService.countGroupMembers(grpId);
    }

    @GetMapping("/groupJoined")
    public String getGroupJoined(@RequestParam("gmsUsername") String gmsUsername) {
        return gson.toJson(groupService.selectWhoJoin(gmsUsername, "member"));
    }

    @GetMapping("/groupManaged")
    public String getGroupManaged(@RequestParam("gmsUsername") String gmsUsername) {
        return gson.toJson(groupService.selectWhoJoin(gmsUsername, "manager"));
    }

    @RequestMapping("/searchGroupInfo")
    public GroupInfoQuery searchGroupInfo(HttpServletRequest request) {
        long grpId = Long.parseLong(request.getParameter("grpId"));
        logger.error("searchGroupInfo" + grpId);
        return groupMemberService.queryGroupInfo(grpId);
    }


    @RequestMapping("/searchMember")
    public List<MemberQuery> searchMember(HttpServletRequest request) {
        int grpId = Integer.parseInt(request.getParameter("grpId"));
        String gmpType = request.getParameter("gmpType");
        return groupMemberService.queryGroupByType(grpId, gmpType);
    }

    @RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
    public int sendMessage(@RequestParam("gmsGrpId") long gmsGrpId,
                           @RequestParam("gmsUsername") String gmsUsername,
                           @RequestParam("gmsContext") String gmsContext) {
        GroupMessage message = new GroupMessage();
        message.setGmsGrpId(gmsGrpId);
        message.setGmsUsername(gmsUsername);
        message.setGmsContext(gmsContext);
        message.setGmsCreateTime(new Timestamp(new Date().getTime()));
        return groupMessageService.addOneMessage(message);
    }

    @RequestMapping(value = "/getMessageNum")
    public int getMessageNum(HttpServletRequest request) {
        long grpId = Long.parseLong(request.getParameter("grpId"));
        return groupMessageService.calculateMessageNum(grpId);
    }

    @RequestMapping(value = "/getMessage")
    public String getMessage(HttpServletRequest request) {
        long grpId = Long.parseLong(request.getParameter("grpId"));
        int index = Integer.parseInt(request.getParameter("index"));
        return gson.toJson(groupMessageService.selectMessageLimitBy(grpId, index, index + 6));
    }

    /**
     * 创建一个新的群
     *
     * @param grpType 群类型
     * @param grpName 群名称
     * @param grpDescription 群描述
     * @param grpRule 群规则
     * @param grpPortrait 群头像
     * @param grpCreator 创建人
     * @return 1表示创建成功 其余为创建失败
     */
    @PostMapping("/createGroup")
    public int createGroup(@RequestParam("grpType") String grpType,
                           @RequestParam("grpName") String grpName,
                           @RequestParam("grpDescription") String grpDescription,
                           @RequestParam("grpRule") String grpRule,
                           @RequestParam("grpPortrait") String grpPortrait,
                           @RequestParam("grpCreator") String grpCreator) {
        long id = SnowflakeIdWorker.getInstance().nextId();
        Group group = new Group(id, grpName, new Timestamp(new Date().getTime()), grpDescription, grpRule, grpType, grpPortrait, grpCreator, "正常");
        GroupMember groupMember = new GroupMember(grpCreator, id, "manager");
        return groupService.createGroup(group) & groupMemberService.addMember(groupMember);
    }


    /*
    * 管理员获取群组信息
    * */
    @RequestMapping("/managerGetGroupInfo")
    public List<GroupByManagerQuery> managerGetGroupInfo(){
        return groupMemberService.managerQueryAll();
    }

    /**
     *根据名称和时间来查询
     */

    @RequestMapping("/queryGroupByNameAndDate")
    public List<GroupByManagerQuery> queryGroupByNameAndDate(HttpServletRequest request){
        String name = request.getParameter("grpName");
        String date = request.getParameter("grpDate");
        List<Long> idList = new ArrayList<>();
        List<Group> groupByManagerQueries = groupService.selectGroupByDateAndName(name, date);
        for(Group group: groupByManagerQueries){
            idList.add(group.getGrpId());
        }
        return groupMemberService.managerQueryGroupInfo(idList);
    }

}
