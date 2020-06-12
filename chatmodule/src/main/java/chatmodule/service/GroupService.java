package chatmodule.service;

import chatmodule.mapper.GroupDao;
import chatmodule.bean.Group;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService {
    @Autowired
    GroupDao groupDao;
    public int createGroup(Group group) {
        System.out.println(group.toString());
        System.out.println(groupDao == null);
        return groupDao.createGroup(group);
    }

    public int deleteGroup(int grpId){
        return groupDao.deleteGroup(grpId);
    }

    public int modifyGroupInfo(Group group){
        return groupDao.modifyGroupInfo(group);
    }

    public List<Group>  searchByGroupNameLike(String groupName){
        String tmp = "%" + groupName + "%";
        groupName = tmp;
        return groupDao.indistinctiveSelectByName(groupName);
    }

    public List<Group> searchByGroupType(String groupType){
        return groupDao.selectByType(groupType);
    }
}
