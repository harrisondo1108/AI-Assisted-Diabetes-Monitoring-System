package com.quan.diabetes.service;

import com.quan.diabetes.entity.Profile;
import java.util.List;
import java.util.Optional;

public interface ProfileService {

    public List<Profile> findAll();

    public Optional<Profile> findById(String id);

    public Profile create(Profile entity);

    public Profile update(String id, Profile entity);

    public void deleteById(String id);

    public boolean existsById(String id);
}
