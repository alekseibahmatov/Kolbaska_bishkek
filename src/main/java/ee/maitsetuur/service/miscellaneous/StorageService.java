package ee.maitsetuur.service.miscellaneous;

import ee.maitsetuur.model.file.File;
import ee.maitsetuur.model.file.FileType;
import ee.maitsetuur.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final FileRepository repository;

    @Value("${file-storage.basepath}")
    private String basePath;

    public File uploadFile(MultipartFile file, FileType type) throws Exception {
        String directory = switch (type) {
            case PHOTO -> "/photos/";
            case CONTRACT -> "/contracts/";
        };

        String[] splittedFileName = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");

        String fileName = "%s.%s".formatted(UUID.randomUUID(), splittedFileName[splittedFileName.length-1]);

        File newFile = File.builder()
                .fileType(type)
                .fileName(fileName)
                .build();

        newFile = repository.save(newFile);

        Path fullPath;

        if(basePath.equals("/")) {
            fullPath = Paths.get(FileSystems.getDefault().getPath(".").toString())
                    .toAbsolutePath()
                    .normalize()
                    .resolve("documents/%s/%s".formatted(directory, fileName));
        } else {
            fullPath = Paths.get("%s/document/%s/%s".formatted(basePath, directory, fileName))
                    .toAbsolutePath()
                    .normalize();
        }

        Files.copy(file.getInputStream(), fullPath);

        return newFile;
    }


    public Map<String, Object> getFile(String fileId) throws Exception {

        File file = repository.findById(UUID.fromString(fileId)).orElseThrow(
                () -> new FileNotFoundException("File with following id not found")
        );

        String directory = switch (file.getFileType()) {
            case PHOTO -> "/photos/";
            case CONTRACT -> "/contracts/";
        };

        Path fullPath;

        if(basePath.equals("/")) {
            fullPath = Paths.get(FileSystems.getDefault().getPath(".").toString())
                    .toAbsolutePath()
                    .normalize()
                    .resolve("documents/%s/%s".formatted(directory, file.getFileName()));
        } else {
            fullPath = Paths.get("%s/document/%s/%s".formatted(basePath, directory, file.getFileName()))
                    .toAbsolutePath()
                    .normalize();
        }

        Map<String, Object> response = new HashMap<>();

        response.put("file", new ByteArrayResource(Files.readAllBytes(fullPath)));
        response.put("fileType", file.getFileType());

        return response;
    }

}
