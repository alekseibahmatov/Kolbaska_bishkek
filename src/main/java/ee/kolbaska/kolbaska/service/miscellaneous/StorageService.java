package ee.kolbaska.kolbaska.service.miscellaneous;

import ee.kolbaska.kolbaska.model.file.File;
import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final FileRepository repository;

    @Value("${file-storage.basepath}")
    private String basePath;

    public File uploadFile(MultipartFile file, FileType type) throws Exception {

        String directory = "";

        if (type == FileType.PHOTO) {
            directory = "/photos/";
        }
        else if(type == FileType.CONTRACT) {
            directory = "/contracts/";
        }

        String[] splittedFileName = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");

        String fileName = "%s.%s".formatted(UUID.randomUUID(), splittedFileName[splittedFileName.length-1]);

        File newFile = File.builder()
                .fileType(type)
                .fileName(fileName)
                .build();

        newFile = repository.save(newFile);

        Path fullPath;

        if(basePath.equals("/")) fullPath = Paths.get(FileSystems.getDefault().getPath(".").toString()).toAbsolutePath().normalize().resolve("documents/%s/%s".formatted(directory, fileName));
        else fullPath = Paths.get("%s/document/%s/%s".formatted(basePath, directory, fileName)).toAbsolutePath().normalize();

        Files.copy(file.getInputStream(), fullPath);

        return newFile;
    }

    public byte[] getFile(String fileName, FileType type) throws Exception {
        String directory = "";

        if (type == FileType.PHOTO) {
            directory = "photos\\";
        }
        else if(type == FileType.CONTRACT) {
            directory = "contracts\\";
        }

        String fullPath = basePath + directory + fileName;

        return Files.readAllBytes(new java.io.File(fullPath).toPath());
    }
}
