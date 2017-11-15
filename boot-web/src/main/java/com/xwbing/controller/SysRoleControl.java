package com.xwbing.controller;

import com.alibaba.fastjson.JSONObject;
import com.xwbing.annotation.LogInfo;
import com.xwbing.entity.SysAuthority;
import com.xwbing.entity.SysRole;
import com.xwbing.entity.SysRoleAuthority;
import com.xwbing.service.SysAuthorityService;
import com.xwbing.service.SysRoleAuthorityService;
import com.xwbing.service.SysRoleService;
import com.xwbing.util.JSONObjResult;
import com.xwbing.util.RestMessage;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名称: boot-module-demo
 * 创建时间: 2017/11/14 10:41
 * 作者: xiangwb
 * 说明:
 */
@Api(tags = "sysRoleApi", description = "角色相关接口")
@RestController
@RequestMapping("/role/")
public class SysRoleControl {
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysAuthorityService sysAuthorityService;
    @Resource
    private SysRoleAuthorityService sysRoleAuthorityService;

    @LogInfo("添加角色")
    @PostMapping("save")
    public JSONObject save(@RequestBody SysRole sysRole) {
        RestMessage result = sysRoleService.save(sysRole);
        return JSONObjResult.toJSONObj(result);
    }

    @LogInfo("删除角色")
    @GetMapping("removeById")
    public JSONObject removeById(@RequestParam String id) {
        if (StringUtils.isEmpty(id))
            return JSONObjResult.toJSONObj("主键不能为空");
        RestMessage result = sysRoleService.removeById(id);
        return JSONObjResult.toJSONObj(result);
    }

    @LogInfo("修改角色")
    @PostMapping("update")
    public JSONObject update(@RequestBody SysRole sysRole) {
        if (StringUtils.isEmpty(sysRole.getId()))
            return JSONObjResult.toJSONObj("主键不能为空");
        RestMessage result = sysRoleService.update(sysRole);
        return JSONObjResult.toJSONObj(result);
    }

    @LogInfo("获取角色详情")
    @GetMapping("getById")
    public JSONObject getById(@RequestParam String id) {
        if (StringUtils.isEmpty(id))
            return JSONObjResult.toJSONObj("主键不能为空");
        SysRole sysRole = sysRoleService.getById(id);
        if (sysRole == null)
            return JSONObjResult.toJSONObj("该角色不存在");
        return JSONObjResult.toJSONObj(sysRole, true, "");
    }

    @LogInfo("根据角色主键查找权限")
    @PostMapping("listAuthorityByRoleId")
    public JSONObject listAuthorityByRoleId(@RequestParam String roleId, String enable) {
        if (StringUtils.isEmpty(roleId))
            return JSONObjResult.toJSONObj("角色主键不能为空");
        SysRole sysRole = sysRoleService.getById(roleId);
        if (sysRole == null)
            return JSONObjResult.toJSONObj("该角色不存在");
        List<SysAuthority> authoritys = sysAuthorityService.listByRoleIdEnable(roleId, enable);
        return JSONObjResult.toJSONObj(authoritys, true, "");
    }

    @LogInfo("保存角色权限")
    @PostMapping("saveAuthoritys")
    public JSONObject saveAuthoritys(@RequestParam String authorityIds, @RequestParam String roleId) {
        if (StringUtils.isEmpty(authorityIds))
            return JSONObjResult.toJSONObj("权限主键集合不能为空");
        if (StringUtils.isEmpty(roleId))
            return JSONObjResult.toJSONObj("角色主键不能为空");
        SysRole sysRole = sysRoleService.getById(roleId);
        if (sysRole == null)
            return JSONObjResult.toJSONObj("该角色不存在");
        String ids[] = authorityIds.split(",");
        List<SysRoleAuthority> list = new ArrayList<>();
        for (String id : ids) {
            SysRoleAuthority roleAuthority = new SysRoleAuthority();
            roleAuthority.setRoleId(roleId);
            roleAuthority.setAuthorityId(id);
            list.add(roleAuthority);
        }
        RestMessage restMessage = sysRoleAuthorityService.saveBatch(list, roleId);
        return JSONObjResult.toJSONObj(restMessage);
    }

    @LogInfo("根据是否启用查询所有角色")
    @GetMapping("listAllByEnable")
    public JSONObject listAllByEnable(@RequestParam String enable) {
        if (StringUtils.isEmpty(enable))
            return JSONObjResult.toJSONObj("是否启用不能为空");
        List<SysRole> sysRoles = sysRoleService.listAllByEnable(enable);
        return JSONObjResult.toJSONObj(sysRoles, true, "");
    }
}