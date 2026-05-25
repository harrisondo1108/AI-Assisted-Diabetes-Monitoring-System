package com.quan.diabetes.service.impl;

import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.repository.ProfileRepository;
import com.quan.diabetes.service.ProfileService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public List<Profile> findAll() {
        return profileRepository.findAll();
    }

    @Override
    public Optional<Profile> findById(String id) {
        return profileRepository.findById(id);
    }

    @Override
    public Profile create(Profile entity) {
        return profileRepository.save(entity);
    }

    @Override
    public Profile update(String id, Profile entity) {
        if (!profileRepository.existsById(id)) {
            throw new EntityNotFoundException("Profile not found with id: " + id);
        }
        return profileRepository.save(entity);
    }

    @Override
    public void deleteById(String id) {
        if (!profileRepository.existsById(id)) {
            throw new EntityNotFoundException("Profile not found with id: " + id);
        }
        profileRepository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return profileRepository.existsById(id);
    }
}
