package com.quan.diabetes.service;

import com.quan.diabetes.entity.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {

    public List<Role> findAll();

    public Optional<Role> findById(String id);

    public Role create(Role entity);

    public Role update(String id, Role entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
