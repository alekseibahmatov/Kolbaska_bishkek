package ee.kolbaska.kolbaska.service.miscellaneous;

import ee.kolbaska.kolbaska.exception.FileStorageException;
import ee.kolbaska.kolbaska.property.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageProperties fileStorageProperties;

    public String storeFile(MultipartFile file) throws FileStorageException {
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            if (fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            fileName = String.format("%s.jpeg", UUID.randomUUID());

            Path targetLocation = Path.of(Objects.equals(file.getContentType(), "image/jpeg") ? fileStorageProperties.getUploadPhotoDir() + fileName : fileStorageProperties.getUploadContractDir() + fileName);

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (Exception ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!");
        }
    }
}
