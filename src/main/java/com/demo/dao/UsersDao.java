package com.demo.dao;

import com.demo.bean.User;
import org.springframework.stereotype.Repository;


@Repository
public interface UsersDao {

    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByRecord(User record);
}
