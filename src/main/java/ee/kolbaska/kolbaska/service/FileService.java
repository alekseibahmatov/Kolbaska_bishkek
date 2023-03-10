package ee.kolbaska.kolbaska.service;

import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.service.miscellaneous.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final StorageService storageService;

    public Resource downloadFile(String fileName, String type) throws Exception {
        return storageService.getFile(fileName, type.equals("photo") ? FileType.PHOTO : FileType.CONTRACT);
    }
}
