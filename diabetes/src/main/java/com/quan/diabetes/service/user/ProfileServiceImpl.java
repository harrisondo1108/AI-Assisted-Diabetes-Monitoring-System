package com.quan.diabetes.service.user;

import com.quan.diabetes.entity.Profile;
import com.quan.diabetes.repository.ProfileRepository;
import com.quan.diabetes.service.user.ProfileService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List<Profile> findTotalDoctor() {
        List<Profile> profiles = this.findAll();
        List<Profile> doctors = new ArrayList<>();
        for (Profile profile : profiles) {
            if (profile != null && profile.getUser() != null && profile.getUser().getRole() != null && "DOC".equals(profile.getUser().getRole().getRoleId())) {
                doctors.add(profile);
            }
        }
        return doctors;
    }

    @Override
    public Optional<Profile> findById(String id) {
        return profileRepository.findById(id);
    }

    @Override
    public Profile create(Profile entity) {
        return profileRepository.save(entity);
    }
}
