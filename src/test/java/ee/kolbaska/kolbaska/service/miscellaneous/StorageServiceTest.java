package ee.kolbaska.kolbaska.service.miscellaneous;

import ee.kolbaska.kolbaska.model.file.File;
import ee.kolbaska.kolbaska.model.file.FileType;
import ee.kolbaska.kolbaska.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestPropertySource(value = "/tests.properties")
class StorageServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(storageService, "basePath", "/");
    }

    @Test
    void testUploadFile() throws Exception {
        // Arrange
        MultipartFile file = new MockMultipartFile("testfile.txt", "Hello, World!".getBytes());
        FileType type = FileType.CONTRACT;

        File newFile = File.builder()
                .id(UUID.randomUUID().toString())
                .fileType(type)
                .fileName("testfile.txt")
                .build();

        when(fileRepository.save(any(File.class))).thenReturn(newFile);

        // Act
        File uploadedFile = storageService.uploadFile(file, type);

        // Assert
        assertNotNull(uploadedFile);
        verify(fileRepository).save(any(File.class));
    }

    @Test
    void testGetFile() throws Exception {
        // Arrange
        String fileId = "file_id";
        String fileName = "testfile.txt";

        // create the necessary directories

        String currentDir = System.getProperty("user.dir");
        Path directoryPath = Paths.get(currentDir, "documents", "contracts");
        Path filePath = directoryPath.resolve(fileName);
        Files.createDirectories(directoryPath);

        // create the file
        File file = File.builder()
                .id(fileId)
                .fileType(FileType.CONTRACT)
                .fileName(fileName)
                .build();
        Files.write(filePath, "Test file content".getBytes());
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        // Act
        Map<String, Object> fileMap = storageService.getFile(fileId);

        // Assert
        assertNotNull(fileMap);
        assertNotNull(fileMap.get("file"));
        assertNotNull(fileMap.get("fileType"));
        verify(fileRepository).findById(fileId);

        // delete the file and directories
        Files.deleteIfExists(filePath);
    }



    @Test
    void testGetFileNotFound() {
        // Arrange
        String fileId = "file_id";

        when(fileRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(FileNotFoundException.class, () -> storageService.getFile(fileId));
        verify(fileRepository).findById(fileId);
    }
}
