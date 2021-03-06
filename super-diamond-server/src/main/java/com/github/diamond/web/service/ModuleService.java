/**
 * Copyright (c) 2013 by 苏州科大国创信息技术有限公司.
 */
package com.github.diamond.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.diamond.web.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Create on @2013-8-21 @下午8:18:44 
 * @author bsli@ustcinfo.com
 */
@Service
public class ModuleService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> queryModules(long projectId) {
        String sql = "SELECT * FROM CONF_PROJECT_MODULE a WHERE a.PROJ_ID = ? order by a.MODULE_ID";
        return jdbcTemplate.queryForList(sql, projectId);
    }

    @Transactional
    public long save(Long projectId, String name) {
        String sql = "SELECT MAX(MODULE_ID)+1 FROM CONF_PROJECT_MODULE";
        long id = 1;
        try {
            id = jdbcTemplate.queryForObject(sql, Long.class);
        } catch (NullPointerException e) {
            ;
        }
        sql = "INSERT INTO CONF_PROJECT_MODULE(MODULE_ID, PROJ_ID, MODULE_NAME) values(?, ?, ?)";
        jdbcTemplate.update(sql, id, projectId, name);
        return id;
    }

    public String findName(Long moduleId) {
        String sql = "SELECT MODULE_NAME FROM CONF_PROJECT_MODULE WHERE MODULE_ID=?";
        return jdbcTemplate.queryForObject(sql, String.class, moduleId);
    }

    @Transactional
    public boolean delete(long moduleId, long projectId) {
        String sql = "select count(*) from CONF_PROJECT_CONFIG where MODULE_ID = ? and PROJECT_ID = ? and DELETE_FLAG <> 1";

        int count = jdbcTemplate.queryForObject(sql, Integer.class, moduleId, projectId);
        if (count == 0) {
            sql = "delete from CONF_PROJECT_MODULE where MODULE_ID = ? and PROJ_ID = ?";
            jdbcTemplate.update(sql, moduleId, projectId);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public ModuleConfigId moduleConfigIdIsExist(String configName, String moduleName,Long projectId) {
        List<Long> moduleIds = null;
        boolean isExist = false;
        Long configId = null;
        Long moduleId = null;
        String sql = "select MODULE_ID from CONF_PROJECT_MODULE where MODULE_NAME=? and PROJ_ID=?";
        try {
            moduleIds = jdbcTemplate.queryForList(sql, Long.class, moduleName,projectId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (moduleIds.size() != 0) {
            moduleId = moduleIds.get(0);
            String enquerySql = "select CONFIG_ID from CONF_PROJECT_CONFIG where MODULE_ID=? and CONFIG_KEY=? and DELETE_FLAG=0";
            List<Long> configs = null;
            try {
                configs = jdbcTemplate.queryForList(enquerySql, Long.class, moduleId, configName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (configs.size() != 0) {
                isExist = true;
                configId = configs.get(0);
            }
            return new ModuleConfigId(isExist, configId, moduleId);
        } else
            return new ModuleConfigId(isExist, configId, moduleId);
    }


    @Transactional
    public ModuleIdExist moduleIdIsExist(String moduleName, long projectId) {
        boolean isExist = false;
        String sql = "select MODULE_ID from CONF_PROJECT_MODULE where MODULE_NAME=? and PROJ_ID=?";
        List<Long> moduleId = null;
        try {
            moduleId = jdbcTemplate.queryForList(sql, Long.class, moduleName, projectId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (moduleId.size() != 0) {
            isExist = true;
            return new ModuleIdExist(isExist, moduleId.get(0));
        }

        return new ModuleIdExist(isExist, 0);

    }

    @Transactional
    public List<Map<String, Object>> getModuleConfigData(long projectId, long[] moduleIds,String type) {
        List<Map<String, Object>> moduleConfigListData = new ArrayList<Map<String, Object>>();

        List<Long> moduleId = new ArrayList<Long>();
        for (int i = 0; i < moduleIds.length; i++) {
            moduleId.add(moduleIds[i]);
        }
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", projectId);
        paramMap.put("moduleIds", moduleId);
        String sql=null;
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        if("development".equals(type))
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.CONFIG_VALUE,b.CONFIG_DESC FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID AND b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID in (:moduleIds) ORDER BY a.MODULE_ID";

        else if("test".equals(type))
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.TEST_VALUE,b.CONFIG_DESC FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID AND b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID in (:moduleIds) ORDER BY a.MODULE_ID";

        else if("build".equals(type))
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.BUILD_VALUE,b.CONFIG_DESC FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID AND b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID in (:moduleIds) ORDER BY a.MODULE_ID";

        else
            sql = "select a.MODULE_ID,b.CONFIG_ID,a.MODULE_NAME,b.CONFIG_KEY,b.PRODUCTION_VALUE,b.CONFIG_DESC FROM CONF_PROJECT_MODULE a,CONF_PROJECT_CONFIG b  where a.PROJ_ID=b.PROJECT_ID AND b.PROJECT_ID=:id AND b.DELETE_FLAG=0 AND a.MODULE_ID=b.MODULE_ID AND a.MODULE_ID in (:moduleIds) ORDER BY a.MODULE_ID";

        moduleConfigListData = namedParameterJdbcTemplate.queryForList(sql, paramMap);
        return moduleConfigListData;
    }
}
