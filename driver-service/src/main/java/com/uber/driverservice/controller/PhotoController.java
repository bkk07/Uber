package com.uber.driverservice.controller;
import com.uber.driverservice.service.PhotoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/photo")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }
    @PostMapping("/upload/{driverId}")
    public ResponseEntity<String> uploadProfilePhoto(
            @PathVariable Long driverId,
            @RequestParam("photo") MultipartFile file) {

        try {
            String message = photoService.uploadProfilePhoto(driverId, file);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload photo");
        }
    }

    // Get profile photo
    @GetMapping("/{driverId}")
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable Long driverId) {
        return photoService.getProfilePhoto(driverId)
                .map(photo -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"" + photo.getName() + "\"")
                        .contentType(MediaType.IMAGE_JPEG) // adjust if using PNG
                        .body(photo.getContent()))
                .orElse(ResponseEntity.notFound().build());
    }
}
