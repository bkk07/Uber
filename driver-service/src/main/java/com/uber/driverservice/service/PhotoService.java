package com.uber.driverservice.service;
import com.uber.driverservice.model.Driver;
import com.uber.driverservice.model.Photo;
import com.uber.driverservice.repository.DriverRepository;
import com.uber.driverservice.repository.PhotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final DriverRepository driverRepository;

    public PhotoService(PhotoRepository photoRepository, DriverRepository driverRepository) {
        this.photoRepository = photoRepository;
        this.driverRepository = driverRepository;
    }
    public String uploadProfilePhoto(Long driverId, MultipartFile file) throws Exception {
        Optional<Driver> driverOpt = driverRepository.findById(driverId);
        if (driverOpt.isEmpty()) {
            return "Driver not found";
        }

        Driver driver = driverOpt.get();
        Photo photo = driver.getProfilePhoto();

        if (photo == null) {
            // No photo exists, create new
            photo = new Photo();
            photo.setDriver(driver);
        }
        photo.setContent(file.getBytes());
        photo.setName(driver.getUsername());
        photoRepository.save(photo);
        driver.setProfilePhoto(photo);
        driverRepository.save(driver);

        return "Profile photo uploaded/updated successfully!";
    }

    // Get profile photo by driver ID
    public Optional<Photo> getProfilePhoto(Long driverId) {
        return driverRepository.findById(driverId)
                .map(Driver::getProfilePhoto);
    }
}
