package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Profile;
import java.util.List;
import java.util.Optional;

public interface ProfileService {

    public List<Profile> findAll();

    public List<Profile> findTotalDoctor();

    public Optional<Profile> findById(String id);

    public Profile create(Profile entity);
}
